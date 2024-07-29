package cn.aixcyi.plugin.shebang.actions

import cn.aixcyi.plugin.shebang.Zoo.message
import cn.aixcyi.plugin.shebang.services.ShebangSettings
import cn.aixcyi.plugin.shebang.ui.ShebangConfigurable
import cn.aixcyi.plugin.shebang.utils.ShebangWrapper
import com.intellij.codeInsight.hint.HintManager
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileChooser.FileChooser
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.options.ShowSettingsUtil
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.roots.ProjectFileIndex
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.vfs.ReadonlyStatusHandler
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiFile
import com.intellij.refactoring.suggested.endOffset
import com.intellij.refactoring.suggested.startOffset
import net.aixcyi.utils.eval
import kotlin.io.path.Path
import kotlin.io.path.extension

/**
 * 为文件添加 Shebang 。
 *
 * @author <a href="https://github.com/aixcyi">砹小翼</a>
 * @see <a href="https://github.com/JetBrains/intellij-community/blob/232/platform/platform-api/src/com/intellij/ide/actions/QuickSwitchSchemeAction.java#L59">QuickSwitchSchemeAction#showPopup</a>
 * @see <a href="https://github.com/JetBrains/intellij-community/blob/232/platform/platform-impl/src/com/intellij/ide/actions/QuickChangeKeymapAction.java#L35">QuickChangeKeymapAction#fillActions</a>
 */
class InsertShebangAction : DumbAwareAction() {

    override fun getActionUpdateThread() = ActionUpdateThread.BGT

    override fun update(event: AnActionEvent) {
        val file = event.getData(CommonDataKeys.PSI_FILE)
        if (file == null) {
            event.presentation.isEnabled = false
            return
        }
        val suffixes = ShebangSettings.getInstance().state.getFileSuffixes()
        if (suffixes.isEmpty()) {
            event.presentation.isEnabled = true
            return
        }
        event.presentation.isEnabled = suffixes.contains(Path(file.name).extension)
    }

    override fun actionPerformed(event: AnActionEvent) {
        val file = event.getData(CommonDataKeys.PSI_FILE) ?: return
        val editor = event.getData(LangDataKeys.EDITOR_EVEN_IF_INACTIVE) ?: return
        val project = file.project

        val handler = ReadonlyStatusHandler.getInstance(project)
        val status = handler.ensureFilesWritable(listOf(file.virtualFile))
        if (status.hasReadonlyFiles()) {
            HintManager.getInstance().showErrorHint(editor, message("hint.EditorIsNotWritable.text"))
            return
        }

        val existedShebang = ShebangWrapper(eval { file.firstChild as PsiComment }?.text).data
        val state = ShebangSettings.getInstance().state
        val group = DefaultActionGroup(null as String?, true)
        for (text in state.myShebangs) {
            group.add(object : AnAction(text) {
                override fun actionPerformed(e: AnActionEvent) {
                    writeShebang(file, editor, ShebangWrapper(text))
                }
            })
        }
        group.addSeparator()
        group.add(object : AnAction(message("action.Shebang.Insert.FromRelativePath.text")) {
            override fun actionPerformed(e: AnActionEvent) {
                val profile = project.projectFile ?: return
                val root = ProjectFileIndex.getInstance(project).getContentRootForFile(profile) ?: return

                // 创建一个起点为当前项目根目录的文件选择器
                val descriptor = FileChooserDescriptorFactory.createSingleFileOrExecutableAppDescriptor()
                descriptor.title = e.presentation.text
                descriptor.setRoots(root)
                val chosen = FileChooser.chooseFile(descriptor, project, null) ?: return
                val path = eval { root.toNioPath().relativize(chosen.toNioPath()).toString() } ?: chosen.path
                writeShebang(file, editor, ShebangWrapper(path))
            }
        })
        group.add(object : AnAction(message("action.Shebang.Insert.FromAbsolutePath.text")) {
            override fun actionPerformed(e: AnActionEvent) {
                val descriptor = FileChooserDescriptorFactory.createSingleFileOrExecutableAppDescriptor()
                descriptor.title = e.presentation.text
                descriptor.setRoots()
                val chosen = FileChooser.chooseFile(descriptor, project, null) ?: return
                writeShebang(file, editor, ShebangWrapper(chosen.path))
            }
        })
        group.add(object : AnAction(message("action.Shebang.Insert.FromAnyPath.text")) {
            override fun actionPerformed(e: AnActionEvent) {
                val string = Messages.showInputDialog(
                    message("dialog.PresetShebang.NewOrEdit.message"),
                    message("dialog.PresetShebang.NewOrEdit.title"),
                    null
                )
                if (string.isNullOrEmpty())
                    return
                writeShebang(file, editor, ShebangWrapper(string))
            }
        })
        group.addSeparator()
        group.add(object : AnAction(message("action.GotoConfiguration.text")) {
            override fun actionPerformed(e: AnActionEvent) {
                ShowSettingsUtil.getInstance().showSettingsDialog(
                    event.project,
                    ShebangConfigurable::class.java,
                )
            }
        })
        val popup = JBPopupFactory.getInstance().createActionGroupPopup(
            message("popup.SelectOneShebang.title"),
            group,
            event.dataContext,
            JBPopupFactory.ActionSelectionAid.ALPHA_NUMBERING,
            true,
            null,
            -1,
            { action -> action.templatePresentation.text == existedShebang },
            null,
        )
        popup.showCenteredInCurrentWindow(project)
    }

    /**
     * 将 shebang 写入到文件第一行。
     *
     * 如果第一行是注释，且与传入的 [shebang] 完全一致，则只弹出泡泡提示，不进行改动，否则直接替换；
     * 如果第一行不是注释，则将 [shebang] 插入到第一行。
     *
     * @param file 文件。
     * @param editor 编辑器。
     * @param shebang 新的 shebang。
     */
    private fun writeShebang(file: PsiFile, editor: Editor, shebang: ShebangWrapper) {
        val hint = HintManager.getInstance()
        val firstElement = eval { file.firstChild as PsiComment }
        val runnable =
            if (firstElement?.text == shebang.text) {
                hint.showInformationHint(editor, message("hint.ShebangExisted.text"))
                return
            } else if (firstElement?.text?.startsWith(ShebangWrapper.HEAD) == true) {
                Runnable {
                    editor.document.replaceString(firstElement.startOffset, firstElement.endOffset, shebang.text)
                }
            } else {
                Runnable {
                    editor.document.insertString(0, "${shebang}\n")
                }
            }
        WriteCommandAction.runWriteCommandAction(
            file.project,
            message("command.InsertShebang"),
            null,
            runnable
        )
    }
}
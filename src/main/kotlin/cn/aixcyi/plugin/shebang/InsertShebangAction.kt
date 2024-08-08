package cn.aixcyi.plugin.shebang

import cn.aixcyi.plugin.shebang.I18nProvider.message
import com.intellij.codeInsight.hint.HintManager
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileChooser.FileChooser
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.options.ShowSettingsUtil
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectFileIndex
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.vfs.ReadonlyStatusHandler
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
        val suffixes = ShebangSettings.getInstance().state.myFileSuffixes.split(ShebangSettings.DELIMITER)
        if (suffixes.isEmpty()) {
            event.presentation.isEnabled = true
            return
        }
        event.presentation.isEnabled = Path(file.name).extension in suffixes
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

        val firstLineText = editor.document.getText(TextRange(0, editor.document.getLineEndOffset(0)))
        val existedShebang = Shebang(firstLineText).takeIf { it.isFromValidString }

        val settings = ShebangSettings.getInstance().state
        val group = DefaultActionGroup(null as String?, true)
        for (text in settings.myShebangs) {
            if (text == existedShebang?.data)
                group.add(object : AnAction(text, "", AllIcons.Actions.Forward) {
                    override fun actionPerformed(e: AnActionEvent) {
                        writeShebang(project, editor, existedShebang, Shebang(text))
                    }
                })
            else
                group.add(object : AnAction(text) {
                    override fun actionPerformed(e: AnActionEvent) {
                        writeShebang(project, editor, existedShebang, Shebang(text))
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
                writeShebang(project, editor, existedShebang, Shebang(path))
            }
        })
        group.add(object : AnAction(message("action.Shebang.Insert.FromAbsolutePath.text")) {
            override fun actionPerformed(e: AnActionEvent) {
                val descriptor = FileChooserDescriptorFactory.createSingleFileOrExecutableAppDescriptor()
                descriptor.title = e.presentation.text
                descriptor.setRoots()
                val chosen = FileChooser.chooseFile(descriptor, project, null) ?: return
                writeShebang(project, editor, existedShebang, Shebang(chosen.path))
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
                writeShebang(project, editor, existedShebang, Shebang(string))
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
        )
        popup.showCenteredInCurrentWindow(project)
    }

    /**
     * 将 shebang 写入到文件第一行。如果新旧 shebang 相同，则弹出提示。
     */
    private fun writeShebang(project: Project, editor: Editor, oldShebang: Shebang?, newShebang: Shebang) {
        val hint = HintManager.getInstance()
        val runnable =
            when (oldShebang) {
                null -> Runnable {
                    editor.document.insertString(0, "${newShebang}\n")
                }

                newShebang -> {
                    hint.showInformationHint(editor, message("hint.ShebangExisted.text"))
                    return
                }

                else -> Runnable {
                    editor.document.replaceString(
                        0,
                        editor.document.getLineEndOffset(0),
                        newShebang.text
                    )
                }
            }
        WriteCommandAction.runWriteCommandAction(
            project,
            message("command.InsertShebang"),
            null,
            runnable
        )
    }
}
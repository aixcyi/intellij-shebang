package cn.aixcyi.plugin.shebang

import cn.aixcyi.plugin.shebang.I18nProvider.message
import com.intellij.codeInsight.hint.HintManager
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileChooser.FileChooser
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.fileTypes.FileTypeManager
import com.intellij.openapi.options.ShowSettingsUtil
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectFileIndex
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.vfs.ReadonlyStatusHandler
import com.intellij.openapi.vfs.VirtualFileManager
import net.aixcyi.utils.eval
import java.nio.file.Path
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
        val file = event.getData(CommonDataKeys.PSI_FILE) ?: run {
            event.presentation.isEnabled = false
            return
        }
        val editor = event.getData(LangDataKeys.EDITOR_EVEN_IF_INACTIVE) ?: run {
            event.presentation.isEnabled = false
            return
        }
        val settings = ShebangSettings.getInstance().state
        val fileType = FileTypeManager.getInstance().getFileTypeByFile(file.virtualFile)
        val suffixes = settings.myFileSuffixes.split(ShebangSettings.DELIMITER)

        event.presentation.isEnabled = run {
            if (fileType.javaClass.name == ShebangSettings.FILETYPE_SHELL_SCRIPT)
                return@run true
            if (suffixes.isEmpty() || Path.of(file.name).extension in suffixes)
                return@run true
            if (editor.getShebang() != null)
                return@run true
            false
        }
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
        val existedShebang = editor.getShebang()
        val settings = ShebangSettings.getInstance().state
        val group = DefaultActionGroup(null as String?, true)
        for (text in settings.myShebangs) {
            if (text == existedShebang?.data)
                group.add(object : DumbAwareAction(text, "", AllIcons.Actions.Forward) {
                    override fun actionPerformed(e: AnActionEvent) {
                        writeShebang(project, editor, existedShebang, Shebang(text))
                    }
                })
            else
                group.add(object : DumbAwareAction(text) {
                    override fun actionPerformed(e: AnActionEvent) {
                        writeShebang(project, editor, existedShebang, Shebang(text))
                    }
                })
        }
        group.addSeparator()
        group.add(object : DumbAwareAction(message("action.Shebang.Insert.FromRelativePath.text")) {
            override fun actionPerformed(e: AnActionEvent) {
                val profile = project.projectFile ?: return
                val root = ProjectFileIndex.getInstance(project).getContentRootForFile(profile) ?: return
                val descriptor = FileChooserDescriptorFactory.createSingleFileOrExecutableAppDescriptor().apply {
                    title = e.presentation.text
                    setRoots(root)
                }
                val chosen = FileChooser.chooseFile(descriptor, project, null) ?: return
                val path = try {
                    // venv\Scripts\python.exe -> ./venv/Scripts/python.exe
                    val fp: Path = root.toNioPath().relativize(chosen.toNioPath())
                    val sb = StringBuilder(".")
                    for (i in 0 until fp.nameCount) {
                        sb.append("/").append(fp.getName(i))
                    }
                    sb.toString()
                } catch (e: Exception) {
                    chosen.path
                }
                writeShebang(project, editor, existedShebang, Shebang(path))
            }
        })
        group.add(object : DumbAwareAction(message("action.Shebang.Insert.FromAbsolutePath.text")) {
            override fun actionPerformed(e: AnActionEvent) {
                val descriptor = FileChooserDescriptorFactory.createSingleFileOrExecutableAppDescriptor().apply {
                    title = e.presentation.text
                    setRoots()
                }
                val preSelect = eval {
                    VirtualFileManager.getInstance().refreshAndFindFileByNioPath(
                        Path.of(settings.myAbsChooserBase)
                    )
                }
                val chosen = FileChooser.chooseFile(descriptor, project, preSelect) ?: return
                writeShebang(project, editor, existedShebang, Shebang(chosen.path))
            }
        })
        group.add(object : DumbAwareAction(message("action.Shebang.Insert.FromAnyPath.text")) {
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
        group.add(object : DumbAwareAction(message("action.GotoConfiguration.text")) {
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
        val runnable = when (oldShebang) {
            null -> Runnable {
                editor.document.insertString(0, "${newShebang}\n")
            }

            newShebang -> {
                HintManager.getInstance().showInformationHint(
                    editor, message("hint.ShebangExisted.text")
                )
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

    private fun Editor.getShebang(): Shebang? {
        val firstLineOffsetRange = TextRange(0, document.getLineEndOffset(0))
        return Shebang(document.getText(firstLineOffsetRange)).takeIf { it.isFromValidString }
    }
}
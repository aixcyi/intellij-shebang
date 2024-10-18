package cn.aixcyi.plugin.shebang

import cn.aixcyi.plugin.shebang.I18nProvider.message
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.editor.colors.EditorColorsManager
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.fileTypes.FileTypeManager
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.ui.Messages
import com.intellij.ui.CollectionListModel
import com.intellij.ui.ToolbarDecorator
import com.intellij.ui.components.JBList
import com.intellij.ui.components.JBTabbedPane
import com.intellij.ui.dsl.builder.RightGap
import com.intellij.ui.dsl.builder.TopGap
import com.intellij.ui.dsl.builder.bindText
import com.intellij.ui.dsl.builder.panel
import net.aixcyi.shim.AlignX
import net.aixcyi.shim.AlignY
import net.aixcyi.shim.align
import net.aixcyi.utils.*
import java.awt.Font


/**
 * 插件设置页面的组件。
 *
 * @author <a href="https://github.com/aixcyi">砹小翼</a>
 */
class ShebangComponent {

    private val settings = ShebangSettings.getInstance().state
    private val fileTypeList = FileTypeManager.getInstance().registeredFileTypes.map { it.javaClass.name }
    private val shebangModel = CollectionListModel(settings.myShebangs)
    private val shebangList = JBList(shebangModel)
    private val toolbarList = ToolbarDecorator.createDecorator(shebangList)
        .setAddAction {
            val string = Messages.showInputDialog(
                message("dialog.PresetShebang.NewOrEdit.message"),
                message("dialog.PresetShebang.NewOrEdit.title"),
                null
            )
            val shebang = Shebang(string)
            if (shebang.isBlank())
                return@setAddAction

            val index = shebangModel.getElementIndex(shebang.data)
            if (index > -1) {
                shebangList.selectionModel.leadSelectionIndex = index
            } else if (shebang.data.isNotBlank()) {
                shebangModel.add(shebang.data)
                shebangList.selectionModel.leadSelectionIndex = shebangModel.size - 1
            }
        }
        .setEditAction {
            val string = Messages.showInputDialog(
                message("dialog.PresetShebang.NewOrEdit.message"),
                message("dialog.PresetShebang.NewOrEdit.title"),
                null,
                shebangModel.getElementAt(shebangList.selectedIndex),
                null
            )
            val shebang = Shebang(string)
            if (shebang.isBlank())
                return@setEditAction
            shebangModel.setElementAt(shebang.data, shebangList.selectedIndex)
        }
        .setRemoveAction {
            shebangModel.remove(shebangList.selectedIndex)
            shebangList.selectionModel.leadSelectionIndex = shebangList.leadSelectionIndex
        }
        .addExtraAction(object : DumbAwareAction(message("action.MoveToTop.text"), null, AllIcons.Actions.Upload) {

            override fun getActionUpdateThread() = ActionUpdateThread.EDT

            override fun update(e: AnActionEvent) {
                e.presentation.isEnabled = !shebangList.isAllSelectionsOnTop()
            }

            override fun actionPerformed(e: AnActionEvent) {
                shebangList.moveSelectionsToTop()
            }
        })
        .addExtraAction(object : DumbAwareAction(message("action.MoveToBottom.text"), null, AllIcons.Actions.Download) {

            override fun getActionUpdateThread() = ActionUpdateThread.EDT

            override fun update(e: AnActionEvent) {
                e.presentation.isEnabled = !shebangList.isAllSelectionsOnBottom()
            }

            override fun actionPerformed(e: AnActionEvent) {
                shebangList.moveSelectionsToBottom()
            }
        })
        .addExtraAction(object : DumbAwareAction(message("action.Sorting.text"), null, AllIcons.ObjectBrowser.Sorted) {

            private var desc = false

            override fun actionPerformed(e: AnActionEvent) {
                if (desc)
                    shebangModel.replaceAll(shebangModel.toList().sortedDescending())
                else
                    shebangModel.replaceAll(shebangModel.toList().sorted())
                desc = !desc
            }
        })
        .addExtraAction(object :
            DumbAwareAction(message("action.EditShebangList.text"), null, AllIcons.Actions.EditScheme) {
            override fun actionPerformed(e: AnActionEvent) {
                MultilineInputDialog()
                    .apply {
                        title = message("action.EditShebangList.text")
                        shebangModel.toList().let {
                            if (it.isNotEmpty())
                                content = it.joinToString("\n", postfix = "\n")
                        }
                    }
                    .showThenGet()
                    ?.let { result ->
                        shebangModel.replaceAll(result.split("\n").filter { it.isNotBlank() }.distinct())
                    }
            }
        })
        .addExtraAction(object :
            DumbAwareAction(message("action.RestoreToDefault.text"), null, AllIcons.General.Reset) {

            override fun getActionUpdateThread() = ActionUpdateThread.EDT

            override fun update(e: AnActionEvent) {
                e.presentation.isEnabled = shebangModel.toList() != ShebangSettings.PRESET_SHEBANGS
            }

            override fun actionPerformed(e: AnActionEvent) {
                shebangModel.replaceAll(ShebangSettings.PRESET_SHEBANGS)
            }
        })
        .createPanel()

    private val editorFont = run {
        val preferences = EditorColorsManager.getInstance().schemeForCurrentUITheme.fontPreferences
        val fontFamily = preferences.fontFamily
        Font(fontFamily, Font.PLAIN, preferences.getSize(fontFamily))
    }
    private val settingsTab = panel {
        group(message("label.FileAssociation.text")) {
            row {
                textFieldWithResetButton(ShebangSettings.PRESET_FILE_SUFFIXES)
                    .comment(message("label.SupportSuffixes.text"))
                    .resizableColumn()
                    .align(AlignX.FILL)
                    .gap(RightGap.SMALL)
                    .bindText(settings::myFileSuffixes)
                    .applyToComponent { textField.font = editorFont }
            }
            if (ShebangSettings.FILETYPE_SHELL_SCRIPT !in fileTypeList) {
                row {
                    text(message("label.ShellScriptUnsupported.text"))
                }
            }
        }
        group(message("label.PathChooserOptions.text")) {
            row {
                textFieldWithResetButton("")
                    .comment(message("label.ChooserSuffixes.text"))
                    .resizableColumn()
                    .align(AlignX.FILL)
                    .bindText(settings::myChooserSuffixes)
                    .applyToComponent { textField.font = editorFont }
            }
            row {
                topGap(TopGap.SMALL)
                val descriptor = FileChooserDescriptorFactory.createSingleFolderDescriptor()
                textFieldWithBrowseButton(fileChooserDescriptor = descriptor)
                    .comment(message("label.AbsChooserInitialFolder.text"))
                    .resizableColumn()
                    .align(AlignX.FILL)
                    .bindText(settings::myAbsChooserBase)
            }
        }
    }

    private val shebangsTab = panel {
        row {
            resizableRow()
            cell(toolbarList)
                .align(AlignX.FILL, AlignY.FILL)
                .onIsModified {
                    settings.myShebangs != shebangModel.toList()
                }
                .onApply {
                    settings.myShebangs = shebangModel.toList()
                }
                .onReset {
                    shebangModel.replaceAll(settings.myShebangs)
                }
        }
    }

    val rootPanel = JBTabbedPane().apply {
        addTab(message("label.PresetShebangList.text"), shebangsTab)
        addTab(message("label.ExtraSettings.text"), settingsTab)
    }

    fun isModified() = shebangsTab.isModified() || settingsTab.isModified()

    fun apply() {
        shebangsTab.apply()
        settingsTab.apply()
    }

    fun reset() {
        shebangsTab.reset()
        settingsTab.reset()
    }
}
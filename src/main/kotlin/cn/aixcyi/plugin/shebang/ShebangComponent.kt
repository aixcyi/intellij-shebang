package cn.aixcyi.plugin.shebang

import cn.aixcyi.plugin.shebang.Zoo.message
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.editor.colors.EditorColorsManager
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.ui.Messages
import com.intellij.ui.CollectionListModel
import com.intellij.ui.ToolbarDecorator
import com.intellij.ui.components.JBList
import com.intellij.ui.dsl.builder.LabelPosition
import com.intellij.ui.dsl.builder.RightGap
import com.intellij.ui.dsl.builder.bindText
import com.intellij.ui.dsl.builder.panel
import net.aixcyi.utils.*
import java.awt.Font
import javax.swing.ListSelectionModel

class ShebangComponent {

    private val state = ShebangSettings.getInstance().state
    private val shebangModel = CollectionListModel(state.myShebangs)
    private val shebangList = JBList(shebangModel).apply { selectionMode = ListSelectionModel.SINGLE_SELECTION }
    private val toolbarList = ToolbarDecorator
        .createDecorator(shebangList)
        .setAddAction {
            val string = Messages.showInputDialog(
                message("dialog.PresetShebang.NewOrEdit.message"),
                message("dialog.PresetShebang.NewOrEdit.title"),
                null
            )
            val shebang = ShebangWrapper(string)
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
            val shebang = ShebangWrapper(string)
            if (shebang.isBlank())
                return@setEditAction
            shebangModel.setElementAt(shebang.data, shebangList.selectedIndex)
        }
        .setRemoveAction {
            shebangModel.remove(shebangList.selectedIndex)
            shebangList.selectionModel.leadSelectionIndex = shebangList.leadSelectionIndex
        }
        .putExtraAction(object :
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

    val panel = panel {
        row {
            val suffixField = textField()
                .label(message("label.SupportSuffixes.text"))
                .comment(message("label.SupportSuffixes.comment"))
                .resizableColumn()
                .align(AlignX.FILL)
                .gap(RightGap.SMALL)
                .bindText(state::myFileSuffixes)
                .applyToComponent {
                    val preferences = EditorColorsManager.getInstance().schemeForCurrentUITheme.fontPreferences
                    val fontFamily = preferences.fontFamily
                    font = Font(fontFamily, Font.PLAIN, preferences.getSize(fontFamily))
                }
                .component
            anActionsButton(object :
                DumbAwareAction(message("action.RestoreToDefault.text"), null, AllIcons.General.Reset) {

                override fun getActionUpdateThread() = ActionUpdateThread.EDT

                override fun update(e: AnActionEvent) {
                    e.presentation.isEnabled = suffixField.text != ShebangSettings.PRESET_FILE_SUFFIXES
                }

                override fun actionPerformed(e: AnActionEvent) {
                    suffixField.text = ShebangSettings.PRESET_FILE_SUFFIXES
                }
            })
        }
        row {
            resizableRow()
            cell(toolbarList)
                .label(message("label.PresetShebangList.text"), LabelPosition.TOP)
                .align(AlignX.FILL, AlignY.FILL)
                .onIsModified {
                    state.myShebangs != shebangModel.toList()
                }
                .onApply {
                    state.myShebangs = shebangModel.toList()
                }
                .onReset {
                    shebangModel.replaceAll(state.myShebangs)
                }
        }
    }
}
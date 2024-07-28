package cn.aixcyi.plugin.shebang.ui

import cn.aixcyi.plugin.shebang.Zoo.message
import cn.aixcyi.plugin.shebang.services.ShebangSettings
import cn.aixcyi.plugin.shebang.utils.ShebangWrapper
import cn.aixcyi.plugin.shebang.utils.hFill
import cn.aixcyi.plugin.shebang.utils.vFill
import com.intellij.openapi.editor.colors.EditorColorsManager
import com.intellij.openapi.options.SearchableConfigurable
import com.intellij.openapi.ui.Messages
import com.intellij.ui.CollectionListModel
import com.intellij.ui.ToolbarDecorator
import com.intellij.ui.components.JBList
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.LabelPosition
import com.intellij.ui.dsl.builder.panel
import java.awt.Font
import javax.swing.ListSelectionModel

/**
 * 插件设置页面。
 *
 * @author <a href="https://github.com/aixcyi">砹小翼</a>
 */
class ShebangConfigurable : SearchableConfigurable {

    companion object {
        const val ID = "HooTool.ShebangConfigurable"
    }

    private val state = ShebangSettings.getInstance().state
    private val shebangModel = CollectionListModel(state.myShebangs)
    private val shebangList = JBList(shebangModel).apply { selectionMode = ListSelectionModel.SINGLE_SELECTION }
    private lateinit var suffixField: JBTextField

    override fun getId() = ID

    override fun getDisplayName() = message("configurable.Shebang.display_name")

    override fun createComponent() = panel {
        row {
            textField()
                .label(message("label.SupportSuffixes.text"))
                .comment(message("label.SupportSuffixes.comment"))
                .hFill()
                .apply { suffixField = this.component }
                .apply {
                    val preferences = EditorColorsManager.getInstance().schemeForCurrentUITheme.fontPreferences
                    val fontFamily = preferences.fontFamily
                    this.component.font = Font(fontFamily, Font.PLAIN, preferences.getSize(fontFamily))
                }
        }
        row {
            resizableRow()
            cell(createToolbarList())
                .label(message("label.PresetShebangList.text"), LabelPosition.TOP)
                .hFill()
                .vFill()
        }
    }

    private fun createToolbarList() = ToolbarDecorator.createDecorator(shebangList)
        .setAddAction {
            val string = Messages.showInputDialog(
                message("dialog.PresetShebang.NewOrEdit.message"),
                message("dialog.PresetShebang.NewOrEdit.title"),
                null
            )
            val shebang = ShebangWrapper(string)
            if (shebang.isBlank())
                return@setAddAction
            shebangModel.add(shebang.data)
            shebangList.selectionModel.leadSelectionIndex = shebangModel.size - 1
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
        .createPanel()

    override fun enableSearch(option: String?): Runnable? {
        // TODO<FUTURE>: 实现搜索
        return null
    }

    override fun isModified() =
        shebangModel.toList() != state.myShebangs
                || suffixField.text != state.myFileSuffixes

    override fun apply() {
        with(state) {
            myShebangs = shebangModel.toList()
            myFileSuffixes = suffixField.text
        }
    }

    override fun reset() {
        shebangModel.removeAll()
        shebangModel.addAll(0, state.myShebangs)
        suffixField.text = state.myFileSuffixes
    }

    override fun disposeUIResources() {
    }
}
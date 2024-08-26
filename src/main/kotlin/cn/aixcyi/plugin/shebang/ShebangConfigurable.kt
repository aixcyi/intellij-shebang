package cn.aixcyi.plugin.shebang

import cn.aixcyi.plugin.shebang.I18nProvider.message
import com.intellij.openapi.options.SearchableConfigurable
import javax.swing.JComponent


/**
 * 插件设置页面。
 *
 * @author <a href="https://github.com/aixcyi">砹小翼</a>
 */
class ShebangConfigurable : SearchableConfigurable {

    companion object {
        const val ID = "HooTool.ShebangConfigurable"
    }

    private var component: ShebangComponent? = null

    override fun getId() = ID

    override fun getDisplayName() = message("configurable.Shebang.display_name")

    override fun createComponent(): JComponent {
        component = ShebangComponent()
        return component!!.rootPanel
    }

    override fun enableSearch(option: String?): Runnable? {
        // FUTURE: 实现搜索
        return null
    }

    override fun isModified() = component?.isModified() ?: false

    override fun apply() {
        component?.apply()
    }

    override fun reset() {
        component?.reset()
    }

    override fun disposeUIResources() {
        component = null
    }
}
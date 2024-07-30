package cn.aixcyi.plugin.shebang

import cn.aixcyi.plugin.shebang.Zoo.message
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
        return component!!.panel
    }

    override fun enableSearch(option: String?): Runnable? {
        // TODO<FUTURE>: 实现搜索
        return null
    }

    override fun isModified() = component?.panel?.isModified() ?: false

    override fun apply() {
        component?.panel?.apply()
    }

    override fun reset() {
        component?.panel?.reset()
    }

    override fun disposeUIResources() {
        component = null
    }
}
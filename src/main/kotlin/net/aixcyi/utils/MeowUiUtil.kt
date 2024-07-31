@file:Suppress("unused")

package net.aixcyi.utils

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.actionSystem.ActionToolbar
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.impl.ActionButton
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.util.text.TextWithMnemonic
import com.intellij.ui.IdeBorderFactory
import com.intellij.ui.ToolbarDecorator
import com.intellij.ui.dsl.builder.Cell
import com.intellij.ui.dsl.builder.Row
import javax.swing.*
import kotlin.reflect.full.functions

/**
 * 图形界面相关工具。
 *
 * @author <a href="https://github.com/aixcyi">砹小翼</a>
 */
data object MeowUiUtil {
    val LOGGER = thisLogger()

    /**
     * 检测是否启用了 [NewUI](https://www.jetbrains.com/help/idea/new-ui.html) 。
     *
     * - 兼容 232.5150+ 的 `com.intellij.ui.NewUI.isEnabled()`
     * - 兼容 213.2094+ 的 `com.intellij.ui.ExperimentalUI.isNewUI()`
     * - 兼容其它平台。
     */
    @JvmStatic
    fun isUsingNewUI(): Boolean = eval {
        Class.forName("com.intellij.ui.NewUI")
            .getMethod("isEnabled")
            .invoke(null) as Boolean
    } ?: eval {
        Class.forName("com.intellij.ui.ExperimentalUI")
            .getMethod("isNewUI")
            .invoke(null) as Boolean
    } ?: false

    /**
     * 创建一个带有标题和分割线的 [JPanel] ，常用于设置界面创建分组。
     *
     * @param title 标题。提供 `null` 的话会绘制一条从左到右的分割线，提供字符串则从字符串末尾开始绘制。
     * @return 被修饰过的 [JPanel] ，放置在里面的组件会自动添加左侧缩进。
     */
    @JvmStatic
    fun createTitledPanel(title: String?) = JPanel().apply {
        layout = BoxLayout(this, BoxLayout.Y_AXIS)
        border = IdeBorderFactory.createTitledBorder(title)
    }
}

/**
 * 自动解析 Kotlin UI DSL [Cell] 包装的组件的文本，并设置助记键。
 *
 * @author <a href="https://github.com/aixcyi">砹小翼</a>
 */
fun <T : JComponent> Cell<T>.mnemonic(): Cell<T> {
    when (val component = this.component) {
        // 按钮、单选框、复选框
        is AbstractButton -> {
            val text = TextWithMnemonic.parse(component.text)
            if (text.hasMnemonic()) {
                component.mnemonic = text.mnemonicCode
                component.text = text.text
            }
        }
        // 标签
        is JLabel -> {
            val text = TextWithMnemonic.parse(component.text)
            if (text.hasMnemonic()) {
                component.setDisplayedMnemonic(text.mnemonicChar)
                component.text = text.text
            }
        }
        // 其它组件
        else -> {}
    }
    return this
}

/**
 * 同时设置 [Cell] 的横向及纵向对齐方式。
 */
fun <T : JComponent> Cell<T>.align(vararg aligns: Alignment): Cell<T> {
    for (align in aligns) {
        when (align) {
            is AlignX -> xAlign(align)
            is AlignY -> yAlign(align)
            else -> {}
        }
    }
    return this
}

/**
 * 设置 [Cell] 的横向对齐方式。
 *
 * - 兼容 223.7126+ 的 `Cell<*>.align(com.intellij.ui.dsl.builder.AlignX)`
 * - 兼容 213.3714+ 的 `Cell<*>.horizontalAlign(com.intellij.ui.dsl.gridLayout.HorizontalAlign)`
 */
fun <T : JComponent> Cell<T>.xAlign(align: AlignX): Cell<T> {
    exec {
        val klass = Class.forName("com.intellij.ui.dsl.builder.Align")
        val param = Class.forName("com.intellij.ui.dsl.builder.AlignX")
            .kotlin.sealedSubclasses.first { it.simpleName == align.name }
            .objectInstance
        javaClass.getMethod("align", klass).invoke(this, param)
    }?.exec {
        val klass = Class.forName("com.intellij.ui.dsl.gridLayout.HorizontalAlign")
        val param = klass.enumConstants.map { it as Enum<*> }.first { it.name == align.name }
        javaClass.getMethod("horizontalAlign", klass).invoke(this, param)
    }?.run {
        MeowUiUtil.LOGGER.warn("Cell<*>.horizontalAlign() was not found!")
    }
    return this
}

/**
 * 设置 [Cell] 的纵向对齐方式。
 *
 * - 兼容 223.7126+ 的 `Cell<*>.align(com.intellij.ui.dsl.builder.AlignY)`
 * - 兼容 213.3714+ 的 `Cell<*>.verticalAlign(com.intellij.ui.dsl.gridLayout.VerticalAlign)`
 */
fun <T : JComponent> Cell<T>.yAlign(align: AlignY): Cell<T> {
    exec {
        val klass = Class.forName("com.intellij.ui.dsl.builder.Align")
        val param = Class.forName("com.intellij.ui.dsl.builder.AlignY")
            .kotlin.sealedSubclasses.first { it.simpleName == align.name }
            .objectInstance
        javaClass.getMethod("align", klass).invoke(this, param)
    }?.exec {
        val klass = Class.forName("com.intellij.ui.dsl.gridLayout.VerticalAlign")
        val param = klass.enumConstants.map { it as Enum<*> }.first { it.name == align.name }
        javaClass.getMethod("verticalAlign", klass).invoke(this, param)
    }?.run {
        MeowUiUtil.LOGGER.warn("Cell<*>.verticalAlign() was not found!")
    }
    return this
}

/**
 * 添加一个按钮到工具条上。
 *
 * - 兼容 231.4840+ 的 `ToolbarDecorator.addExtraAction(AnAction)`
 * - 兼容 173.4674+ 的 `ToolbarDecorator.addExtraAction(AnActionButton)`
 *
 * @see [ToolbarDecorator.addExtraAction]
 */
fun ToolbarDecorator.putExtraAction(action: AnAction): ToolbarDecorator {
    exec {
        javaClass.getMethod("addExtraAction", AnAction::class.java).invoke(this, action)
    }?.exec {
        val klass = Class.forName("com.intellij.ui.AnActionButton")
        val param = klass.getMethod("fromAction", AnAction::class.java).invoke(null, action)
        javaClass.getMethod("addExtraAction", klass).invoke(this, param)
    }?.run {
        MeowUiUtil.LOGGER.warn("ToolbarDecorator.addExtraAction(AnActionButton) was not found!")
    }
    return this
}

/**
 * 生成一个 [ActionButton] 。
 *
 * - 兼容 233.6745+ 的 `com.intellij.ui.dsl.builder.ExtensionsKt.actionButton()`
 * - 兼容 213.5281+ 的 `com.intellij.ui.dsl.builder.Row.actionsButton()`
 * - 兼容更旧的版本。
 */
fun Row.anActionButton(action: AnAction, actionPlace: String): Cell<ActionButton> {
    val component = ActionButton(
        action,
        action.templatePresentation.clone(),
        actionPlace,
        ActionToolbar.DEFAULT_MINIMUM_BUTTON_SIZE
    )
    return cell(component)
}

/**
 * 生成一个带下拉菜单的 [ActionButton] 。
 *
 * - 兼容 233.6745+ 的 `com.intellij.ui.dsl.builder.ExtensionsKt.actionButton()`
 * - 兼容 213.5281+ 的 `com.intellij.ui.dsl.builder.Row.actionsButton()`
 */
fun Row.anActionsButton(
    vararg actions: AnAction,
    icon: Icon = AllIcons.General.GearPlain,
    actionPlace: String = ActionPlaces.UNKNOWN,
): Cell<ActionButton> {
    @Suppress("UNCHECKED_CAST")
    return this::class.functions
        .first { it.name == "actionsButton" }
        .call(this, actions, actionPlace, icon) as Cell<ActionButton>
}
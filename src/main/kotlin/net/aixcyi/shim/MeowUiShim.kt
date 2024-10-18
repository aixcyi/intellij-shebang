package net.aixcyi.shim

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.actionSystem.ActionToolbar
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.impl.ActionButton
import com.intellij.ui.ToolbarDecorator
import com.intellij.ui.dsl.builder.Cell
import com.intellij.ui.dsl.builder.Row
import net.aixcyi.utils.MeowUiUtil
import net.aixcyi.utils.exec
import javax.swing.Icon
import javax.swing.JComponent
import kotlin.reflect.full.functions


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
 * - 在其它版本或平台会抛出警告。
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
 * - 兼容 223.7571 之后的 `addExtraAction(AnAction)`
 * - 兼容 223.7571 以前的 `addExtraAction(AnActionButton)`
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
fun Row.anActionButton(action: AnAction, actionPlace: String = ActionPlaces.UNKNOWN): Cell<ActionButton> {
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
@file:Suppress("unused")

package net.aixcyi.utils

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.util.text.TextWithMnemonic
import com.intellij.ui.ToolbarDecorator
import com.intellij.ui.dsl.builder.Cell
import javax.swing.AbstractButton
import javax.swing.JComponent
import javax.swing.JLabel

/**
 * 图形界面相关工具。
 *
 * @author <a href="https://github.com/aixcyi">砹小翼</a>
 */
object MeowUiUtil {
    val LOGGER = thisLogger()
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
 * 让 [Cell] 横向填满当前单元格。用于兼容以下两种写法：
 *
 * - `Cell<*>.align(com.intellij.ui.dsl.builder.AlignX.FILL)`
 * - `Cell<*>.horizontalAlign(com.intellij.ui.dsl.gridLayout.HorizontalAlign.FILL)`
 *
 * @author <a href="https://github.com/aixcyi">砹小翼</a>
 */
fun <T : JComponent> Cell<T>.xFill(): Cell<T> {
    exec {
        val klass = Class.forName("com.intellij.ui.dsl.builder.Align")
        val param = Class.forName("com.intellij.ui.dsl.builder.AlignX")
            .kotlin.sealedSubclasses.first { it.simpleName == "FILL" }
            .objectInstance
        javaClass.getMethod("align", klass).invoke(this, param)
    }?.exec {
        val klass = Class.forName("com.intellij.ui.dsl.gridLayout.HorizontalAlign")
        val param = klass.enumConstants.map { it as Enum<*> }.first { it.name == "FILL" }
        javaClass.getMethod("horizontalAlign", klass).invoke(this, param)
    }?.run {
        MeowUiUtil.LOGGER.warn("Cell<*>.horizontalAlign() was not found!")
    }
    return this
}

/**
 * 让 [Cell] 纵向填满当前单元格。用于兼容以下两种写法：
 *
 * - `Cell<*>.align(com.intellij.ui.dsl.builder.AlignY.FILL)`
 * - `Cell<*>.verticalAlign(com.intellij.ui.dsl.gridLayout.VerticalAlign.FILL)`
 *
 * @author <a href="https://github.com/aixcyi">砹小翼</a>
 */
fun <T : JComponent> Cell<T>.yFill(): Cell<T> {
    exec {
        val klass = Class.forName("com.intellij.ui.dsl.builder.Align")
        val param = Class.forName("com.intellij.ui.dsl.builder.AlignY")
            .kotlin.sealedSubclasses.first { it.simpleName == "FILL" }
            .objectInstance
        javaClass.getMethod("align", klass).invoke(this, param)
    }?.exec {
        val klass = Class.forName("com.intellij.ui.dsl.gridLayout.VerticalAlign")
        val param = klass.enumConstants.map { it as Enum<*> }.first { it.name == "FILL" }
        javaClass.getMethod("verticalAlign", klass).invoke(this, param)
    }?.run {
        MeowUiUtil.LOGGER.warn("Cell<*>.verticalAlign() was not found!")
    }
    return this
}

/**
 * 添加一个按钮到工具条上。兼容
 *
 * - 旧版本的 `ToolbarDecorator.addExtraAction(AnActionButton)`
 * - 新版本的 `ToolbarDecorator.addExtraAction(AnAction)`
 *
 * @see [ToolbarDecorator.addExtraAction]
 * @author <a href="https://github.com/aixcyi">砹小翼</a>
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
package cn.aixcyi.plugin.shebang.utils

import com.intellij.ui.dsl.builder.Cell
import javax.swing.JComponent

/**
 * 让 [Cell] 横向填满当前单元格。用于兼容以下两种写法：
 *
 * - `align(com.intellij.ui.dsl.builder.AlignX.FILL)`
 * - `horizontalAlign(com.intellij.ui.dsl.gridLayout.HorizontalAlign.FILL)`
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
    }
    return this
}

/**
 * 让 [Cell] 纵向填满当前单元格。用于兼容以下两种写法：
 *
 * - `align(com.intellij.ui.dsl.builder.AlignY.FILL)`
 * - `verticalAlign(com.intellij.ui.dsl.gridLayout.VerticalAlign.FILL)`
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
    }
    return this
}
@file:Suppress("unused")

package net.aixcyi.utils

import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.util.text.TextWithMnemonic
import com.intellij.ui.CollectionListModel
import com.intellij.ui.IdeBorderFactory
import com.intellij.ui.components.JBList
import com.intellij.ui.dsl.builder.Cell
import javax.swing.*

/**
 * 图形界面相关工具。
 *
 * @author <a href="https://github.com/aixcyi/">砹小翼</a>
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
        Class.forName("com.intellij.ui.NewUI").getMethod("isEnabled").invoke(null) as Boolean
    } ?: eval {
        Class.forName("com.intellij.ui.ExperimentalUI").getMethod("isNewUI").invoke(null) as Boolean
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
 * @author <a href="https://github.com/aixcyi/">砹小翼</a>
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

/** 检查所有选中项是否在底部连续排列。没有选中任何条目也会返回 `true` 。 */
fun JBList<*>.isAllSelectionsOnBottom(): Boolean {
    return this.selectionModel.selectedIndices.let { indices ->
        indices.isEmpty()
                || indices.last() == this.itemsCount - 1
                && indices.last() - indices.first() == indices.size - 1
    }
}

/** 将选中项移到列表顶部。若没有选中任何条目，将不会执行任何操作。仅面向 [CollectionListModel] 。 */
fun <T : Comparable<T>> JBList<T>.moveSelectionsToTop() {
    val indices = selectionModel.selectedIndices
    if (indices.isEmpty())
        return

    // 替换列表
    val model = this.model as CollectionListModel
    // @formatter:off
    model.replaceAll(model.toList().withIndex().let { list -> (
            list.filter { it.index in indices }.map { it.value }
            + list.filterNot { it.index in indices }.map { it.value }
    )})
    // @formatter:on

    // 选中顶部
    selectionModel.setSelectionInterval(
        0,
        indices.size - 1,
    )
}

/** 将选中项移到列表底部。若没有选中任何条目，将不会执行任何操作。仅面向 [CollectionListModel] 。 */
fun <T : Comparable<T>> JBList<T>.moveSelectionsToBottom() {
    val indices = selectionModel.selectedIndices
    if (indices.isEmpty())
        return

    // 替换列表
    val model = this.model as CollectionListModel
    // @formatter:off
    model.replaceAll(model.toList().withIndex().let { list -> (
            list.filterNot { it.index in indices }.map { it.value }
            + list.filter { it.index in indices }.map { it.value }
    )})
    // @formatter:on

    // 选中顶部
    selectionModel.setSelectionInterval(
        itemsCount - indices.size,
        itemsCount - 1,
    )
}
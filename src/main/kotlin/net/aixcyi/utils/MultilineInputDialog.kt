@file:Suppress("unused")

package net.aixcyi.utils

import com.intellij.CommonBundle
import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CustomShortcutSet
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.EditorTextField
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTextArea
import java.awt.event.KeyEvent
import javax.swing.JComponent
import javax.swing.KeyStroke
import javax.swing.ScrollPaneConstants


/**
 * 多行文本编辑对话框。
 *
 * ```kotlin
 * dialog = MultilineInputDialog()
 * dialog.content = "初始内容\n Initial Content\n"
 * dialog.showThenGet()?.let { content ->
 *     content.split("\n")
 *         .filter { it.isNotBlank() }
 *         .distinct()
 *         .forEach { println(it) }
 * }
 * ```
 *
 * 注意：Enter 键已被绑定到确认保存，换行需要按下 Shift+Enter 键。
 *
 * @author <a href="https://github.com/aixcyi/">砹小翼</a>
 */
class MultilineInputDialog : DialogWrapper(true) {

    private val area = JBTextArea()

    /** 对话框的内容。 */
    var content: String
        get() = area.text
        set(value) {
            area.text = value
        }

    /** 对话框的所有行。写入时自动添加一个空行，读取时自动去除最后一行（如果是空行的话）。 */
    var lines: List<String>
        get() = area.text.split("\n").let {
            if (it.last().isEmpty())
                it.dropLast(1)
            else
                it
        }
        set(value) {
            area.text = value.joinToString("\n", postfix = "\n")
        }

    init {
        isResizable = true
        setOKButtonText(CommonBundle.getOkButtonText())
        setCancelButtonText(CommonBundle.getCancelButtonText())
        super.init()
        installShiftEnterHook(contentPanel, disposable)
        copyStyle()
    }

    override fun createCenterPanel() = JBScrollPane(
        area,
        ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
        ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED,
    )

    override fun getPreferredFocusedComponent(): JComponent = area

    fun showThenGet(): String? {
        return if (showAndGet()) area.text else null
    }

    private fun copyStyle() {
        val field = EditorTextField()
        area.font = field.font
        area.border = field.border
        area.background = field.background
    }

    private fun installShiftEnterHook(root: JComponent, disposable: Disposable) {
        object : DumbAwareAction() {
            override fun actionPerformed(e: AnActionEvent) {
                area.insert("\n", area.caretPosition)
            }
        }.registerCustomShortcutSet(
            CustomShortcutSet(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, KeyEvent.SHIFT_DOWN_MASK)),
            root,
            disposable,
        )
    }
}
package cn.aixcyi.plugin.shebang

import cn.aixcyi.plugin.shebang.I18nProvider.message
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.ui.ComponentWithBrowseButton
import com.intellij.ui.ClientProperty
import com.intellij.ui.TextAccessor
import com.intellij.ui.components.JBTextField
import com.intellij.ui.components.fields.ExtendableTextField
import com.intellij.ui.dsl.builder.*
import com.intellij.util.ui.SwingUndoUtil
import javax.swing.Icon
import javax.swing.JTextField
import kotlin.reflect.KMutableProperty0


class TextFieldWithResetButton(private val defaultText: String) :
    ComponentWithBrowseButton<JTextField>(ExtendableTextField(10), null), TextAccessor {

    init {
        ClientProperty.get(textField, AnAction.ACTIONS_KEY)?.removeAll { true }  // 移除父类构造时绑定的快捷键
        if (textField !is JBTextField) {
            SwingUndoUtil.addUndoRedoActions(textField)
        }
        textField.addActionListener {
            // TODO: 无法触发
            setButtonEnabled(textField.text != defaultText)
        }
        addActionListener {
            reset()
        }
    }

    val textField
        get() = childComponent

    override fun setText(text: String?) {
        textField.text = text
    }

    override fun getText(): String {
        return textField.text
    }

    override fun getDefaultIcon(): Icon {
        return AllIcons.General.Reset
    }

    override fun getHoveredIcon(): Icon {
        return AllIcons.General.Reset
    }

    override fun getIconTooltip(): String {
        return message("action.RestoreToDefault.text")
    }

    fun reset(): String? {
        val text = textField.text
        textField.text = defaultText
        return text
    }
}

fun Cell<TextFieldWithResetButton>.bindText(prop: KMutableProperty0<String>): Cell<TextFieldWithResetButton> {
    return bindText(prop.toMutableProperty())
}

private fun Cell<TextFieldWithResetButton>.bindText(prop: MutableProperty<String>): Cell<TextFieldWithResetButton> {
    return bind(TextFieldWithResetButton::getText, TextFieldWithResetButton::setText, prop)
}

fun Cell<TextFieldWithResetButton>.columns(columns: Int): Cell<TextFieldWithResetButton> {
    component.textField.columns = columns
    return this
}

fun Row.textFieldWithResetButton(defaultText: String): Cell<TextFieldWithResetButton> {
    return cell(TextFieldWithResetButton(defaultText)).apply {
        columns(COLUMNS_SHORT)
    }
}

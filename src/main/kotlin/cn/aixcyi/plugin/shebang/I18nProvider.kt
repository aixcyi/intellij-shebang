package cn.aixcyi.plugin.shebang

import com.intellij.DynamicBundle
import org.jetbrains.annotations.PropertyKey
import java.util.function.Supplier

// Internationalization - IntelliJ Platform Plugin SDK
// https://plugins.jetbrains.com/docs/intellij/internationalization.html

/**
 * 国际化文本工具类。
 *
 * @author <a href="https://github.com/aixcyi/">砹小翼</a>
 */
@Suppress("unused")
object I18nProvider {

    private val BUNDLE = DynamicBundle(Zoo::class.java, Zoo.BUNDLE_NAME)

    @JvmStatic
    fun message(@PropertyKey(resourceBundle = Zoo.BUNDLE_NAME) key: String, vararg params: Any): String {
        return BUNDLE.getMessage(key, *params)
    }

    @JvmStatic
    fun lazyMessage(@PropertyKey(resourceBundle = Zoo.BUNDLE_NAME) key: String, vararg params: Any): Supplier<String> {
        return BUNDLE.getLazyMessage(key, *params)
    }
}
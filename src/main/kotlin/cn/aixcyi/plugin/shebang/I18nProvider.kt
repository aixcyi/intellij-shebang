package cn.aixcyi.plugin.shebang

import com.intellij.DynamicBundle
import org.jetbrains.annotations.Nls
import org.jetbrains.annotations.PropertyKey
import java.util.function.Supplier

// Internationalization - IntelliJ Platform Plugin SDK
// https://plugins.jetbrains.com/docs/intellij/internationalization.html

/**
 * 国际化文本工具类。
 *
 * @author <a href="https://github.com/aixcyi/">砹小翼</a>
 */
@Suppress("unused", "MemberVisibilityCanBePrivate")
object I18nProvider {

    private val BUNDLE = DynamicBundle(Zoo::class.java, Zoo.BUNDLE_NAME)

    @JvmStatic
    fun message(key: @PropertyKey(resourceBundle = Zoo.BUNDLE_NAME) String, vararg params: Any): @Nls String {
        return BUNDLE.getMessage(key, *params)
    }

    // @formatter:off
    @JvmStatic
    fun lazyMessage(@PropertyKey(resourceBundle = Zoo.BUNDLE_NAME) key: String, vararg params: Any): Supplier<@Nls String> {
        return BUNDLE.getLazyMessage(key, *params)
    }
    // @formatter:on
}
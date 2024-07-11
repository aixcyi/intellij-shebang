package cn.aixcyi.plugin.shebang.utils

// 此类用于规范输入输出。

/**
 * @see <a href="https://en.wikipedia.org/wiki/Shebang_(Unix)">Shebang (Unix) - Wikipedia</a>
 * @author <a href="https://github.com/aixcyi">砹小翼</a>
 */
class Shebang(string: String?) {

    companion object {
        const val HEAD = "#!"
    }

    /** Shebang 的内容。不包含 `#!` 。 */
    val data: String =
        if (string.isNullOrBlank())
            ""
        else if (string.startsWith(HEAD))
            string.substring(2)
        else
            string

    /** Shebang 整体，包含 `#!` 。等同于 [Shebang.toString] 的返回值。 */
    val text: String = "${HEAD}${data}"

    /** Shebang 内容是否为空（即是否除了 `#!` 没有其它字符）。 */
    fun isBlank(): Boolean = text == HEAD

    override fun toString(): String = text

    operator fun component1(): String = HEAD
    operator fun component2(): String = data
}
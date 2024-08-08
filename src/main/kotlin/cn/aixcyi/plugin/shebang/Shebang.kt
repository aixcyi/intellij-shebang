package cn.aixcyi.plugin.shebang

/**
 * _此类用于规范输入输出。_
 *
 * @see <a href="https://en.wikipedia.org/wiki/Shebang_(Unix)">Shebang (Unix) - Wikipedia</a>
 * @author <a href="https://github.com/aixcyi">砹小翼</a>
 */
class Shebang(string: String?) {

    companion object {
        const val HEAD = "#!"
    }

    /** 是否构造自一个以 `#!` 开头的字符串。 */
    var isFromValidString = false

    /** Shebang 的内容。不包含 `#!` 。 */
    val data: String =
        if (string.isNullOrBlank()) {
            isFromValidString = false
            ""
        } else if (string.startsWith(HEAD)) {
            isFromValidString = true
            string.substring(2)
        } else {
            isFromValidString = false
            string
        }

    /** Shebang 整体，包含 `#!` 。等同于 [Shebang.toString] 的返回值。 */
    val text: String = "$HEAD$data"

    /** Shebang 内容是否为空（即是否除了 `#!` 没有其它字符）。 */
    fun isBlank(): Boolean = text == HEAD

    override fun toString(): String = text

    operator fun component1(): String = HEAD
    operator fun component2(): String = data

    override fun equals(other: Any?): Boolean =
        if (other is Shebang)
            this.text == other.text
        else
            false

    override fun hashCode(): Int {
        return text.hashCode()
    }
}
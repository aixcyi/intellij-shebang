package net.aixcyi.utils


/** 获取 [obj] 的自述字符串。 */
fun repr(obj: Any?): String {
    return when (obj) {
        is String -> "\"$obj\""
        is Set<*> -> obj.joinToString(",", "{", "}") { repr(it) }
        is List<*> -> obj.joinToString(",", "[", "]") { repr(it) }
        is Map<*, *> -> obj.entries.joinToString(",", "{", "}") { e -> "${repr(e.key)}: ${repr(e.value)}" }
        is Class<*> -> obj.simpleName
        else -> "$obj"
    }
}

/** 使用 [sep] 分隔，以 [ends] 作为换行，打印多个对象。 */
fun print(vararg values: Any, sep: String = ", ", ends: String = "\n") {
    val builder = StringBuilder().append(values)
    for (value in values) {
        builder.append(repr(value)).append(sep)
    }
    kotlin.io.print(builder.append(ends).toString())
}
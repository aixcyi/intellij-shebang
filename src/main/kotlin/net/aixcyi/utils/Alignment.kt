@file:Suppress("unused")

package net.aixcyi.utils

interface Alignment

/**
 * 水平（横向）对齐方式。
 */
enum class AlignX : Alignment {
    /** 左对齐。 */
    LEFT,

    /** 居中对齐。 */
    CENTER,

    /** 右对齐。 */
    RIGHT,

    /** 填充（满）水平方向。 */
    FILL
}

/**
 * 垂直（纵向）对齐方式。
 */
enum class AlignY : Alignment {
    /** 顶部对齐。 */
    TOP,

    /** 居中对齐。 */
    CENTER,

    /** 底部对齐。 */
    BOTTOM,

    /** 填充（满）垂直方向。 */
    FILL
}
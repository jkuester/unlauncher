package com.sduduzog.slimlauncher.adapters

/**
 * Corresponding to the resulting gravity, not the option key
 */
enum class Alignment (val value: Int) {
    LEFT(3),
    RIGHT(5),
    CENTER(1)
}

fun fromGravity(alignment: Int): Alignment {
    return when (alignment) {
        2 -> Alignment.RIGHT
        1 -> Alignment.CENTER
        else -> Alignment.LEFT
    }
}
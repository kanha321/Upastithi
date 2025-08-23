package com.kanhaji.basics.extensions

fun String.toTitleCase(): String {
    return lowercase()
        .split(" ")
        .joinToString(" ") {
            it.replaceFirstChar { char ->
                char.uppercase()
            }
        }
}
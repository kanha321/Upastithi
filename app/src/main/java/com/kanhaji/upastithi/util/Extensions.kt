package com.kanhaji.upastithi.util

import com.kanhaji.upastithi.features.home.data.TimeTable
import com.kanhaji.upastithi.features.home.domain.model.ClassEntity
import kotlinx.datetime.DayOfWeek

fun DayOfWeek.getClasses(): List<ClassEntity> {
    return when (this) {
        DayOfWeek.MONDAY -> TimeTable.MONDAY
        DayOfWeek.TUESDAY -> TimeTable.TUESDAY
        DayOfWeek.WEDNESDAY -> TimeTable.WEDNESDAY
        DayOfWeek.THURSDAY -> TimeTable.THURSDAY
        DayOfWeek.FRIDAY -> TimeTable.FRIDAY
        DayOfWeek.SATURDAY -> TimeTable.SATURDAY
        DayOfWeek.SUNDAY -> TimeTable.SUNDAY
    }
}

fun String.toTitleCase(): String {
    return lowercase().split(" ")
        .joinToString(" ") {
            it.replaceFirstChar { char ->
                char.uppercase()
            }
        }
}

fun Double.roundTo(decimals: Int): Double {
    return "%.${decimals}f".format(this).toDouble()
}

fun Long.formatTransferSpeed(): String {
    return when {
        this >= 1_073_741_824 -> "%.2f GB/s".format(this / 1_073_741_824.0)
        this >= 1_048_576 -> "%.2f MB/s".format(this / 1_048_576.0)
        this >= 1024 -> "%.2f KB/s".format(this / 1024.0)
        else -> "$this B/s"
    }
}

fun Long.formatTime(): String {
    return when {
        this >= 3600 -> "${this / 3600}h ${(this % 3600) / 60}m"
        this >= 60 -> "${this / 60}m ${this % 60}s"
        else -> "${this}s"
    }
}
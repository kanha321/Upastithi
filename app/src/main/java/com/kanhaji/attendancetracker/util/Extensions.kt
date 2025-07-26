package com.kanhaji.attendancetracker.util

import com.kanhaji.attendancetracker.data.TimeTable
import com.kanhaji.attendancetracker.entity.ClassEntity
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
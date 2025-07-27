package com.kanhaji.upastithi.data

import com.kanhaji.upastithi.entity.ClassEntity
import java.time.DayOfWeek

object TimeTable {
    val MONDAY = listOf(
        ClassEntity(
            subject = Subject.OPERATING_SYSTEMS,
            time = "9:00 - 10:00",
            dayOfWeek = DayOfWeek.MONDAY,
            attendanceStatus = null
        ),
        ClassEntity(
            subject = Subject.OBJECT_BASED_MODELING,
            time = "10:00 - 11:00",
            dayOfWeek = DayOfWeek.MONDAY,
            attendanceStatus = null
        ),
        ClassEntity(
            subject = Subject.ANALYSIS_OF_ALGORITHMS,
            time = "13:00 - 14:00",
            dayOfWeek = DayOfWeek.MONDAY,
            attendanceStatus = null
        ),
        ClassEntity(
            subject = Subject.ANALYSIS_OF_ALGORITHMS,
            time = "14:00 - 15:00",
            dayOfWeek = DayOfWeek.MONDAY,
            attendanceStatus = null
        ),
        ClassEntity(
            subject = Subject.SOFT_COMPUTING,
            time = "15:00 - 16:00",
            dayOfWeek = DayOfWeek.MONDAY,
            attendanceStatus = null
        ),
    )

    val TUESDAY = listOf(
        ClassEntity(
            subject = Subject.ANALYSIS_OF_ALGORITHMS_LAB,
            time = "9:00 - 12:00",
            dayOfWeek = DayOfWeek.TUESDAY,
            attendanceStatus = null
        ),
        ClassEntity(
            subject = Subject.OPERATING_SYSTEMS,
            time = "14:00 - 15:00",
            dayOfWeek = DayOfWeek.TUESDAY,
            attendanceStatus = null
        ),
        ClassEntity(
            subject = Subject.OPERATING_SYSTEMS,
            time = "15:00 - 16:00",
            dayOfWeek = DayOfWeek.TUESDAY,
            attendanceStatus = null
        ),
    )

    val WEDNESDAY = listOf(
        ClassEntity(
            subject = Subject.DATABASE_MANAGEMENT_SYSTEMS,
            time = "9:00 - 10:00",
            dayOfWeek = DayOfWeek.WEDNESDAY,
            attendanceStatus = null
        ),
        ClassEntity(
            subject = Subject.DATABASE_MANAGEMENT_SYSTEMS,
            time = "10:00 - 11:00",
            dayOfWeek = DayOfWeek.WEDNESDAY,
            attendanceStatus = null
        ),
        ClassEntity(
            subject = Subject.ANALYSIS_OF_ALGORITHMS,
            time = "11:00 - 12:00",
            dayOfWeek = DayOfWeek.WEDNESDAY,
            attendanceStatus = null
        ),
        ClassEntity(
            subject = Subject.WEB_PROGRAMMING_LAB,
            time = "14:00 - 17:00",
            dayOfWeek = DayOfWeek.WEDNESDAY,
            attendanceStatus = null
        ),
    )

    val THURSDAY = listOf(
        ClassEntity(
            subject = Subject.SOFT_COMPUTING,
            time = "8:00 - 9:00",
            dayOfWeek = DayOfWeek.THURSDAY,
            attendanceStatus = null
        ),
        ClassEntity(
            subject = Subject.SOFT_COMPUTING,
            time = "9:00 - 10:00",
            dayOfWeek = DayOfWeek.THURSDAY,
            attendanceStatus = null
        ),
        ClassEntity(
            subject = Subject.OBJECT_BASED_MODELING,
            time = "10:00 - 11:00",
            dayOfWeek = DayOfWeek.THURSDAY,
            attendanceStatus = null
        ),
        ClassEntity(
            subject = Subject.OBJECT_BASED_MODELING,
            time = "11:00 - 12:00",
            dayOfWeek = DayOfWeek.THURSDAY,
            attendanceStatus = null
        ),
        ClassEntity(
            subject = Subject.DATABASE_MANAGEMENT_SYSTEMS_LAB,
            time = "14:00 - 17:00",
            dayOfWeek = DayOfWeek.THURSDAY,
            attendanceStatus = null
        ),
    )


    val FRIDAY = listOf(
        ClassEntity(
            subject = Subject.DATABASE_MANAGEMENT_SYSTEMS,
            time = "10:00 - 11:00",
            dayOfWeek = DayOfWeek.FRIDAY,
            attendanceStatus = null
        ),
        ClassEntity(
            subject = Subject.DATABASE_MANAGEMENT_SYSTEMS,
            time = "11:00 - 12:00",
            dayOfWeek = DayOfWeek.FRIDAY,
            attendanceStatus = null
        ),
        ClassEntity(
            subject = Subject.OPERATING_SYSTEMS,
            time = "13:00 - 14:00",
            dayOfWeek = DayOfWeek.FRIDAY,
            attendanceStatus = null
        ),
        ClassEntity(
            subject = Subject.OPERATING_SYSTEMS_LAB,
            time = "14:00 - 17:00",
            dayOfWeek = DayOfWeek.FRIDAY,
            attendanceStatus = null
        ),
    )

    val SATURDAY = emptyList<ClassEntity>()
    val SUNDAY = emptyList<ClassEntity>()
}

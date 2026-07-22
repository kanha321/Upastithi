package com.kanhaji.upastithi.features.home.data

import com.kanhaji.upastithi.features.home.domain.model.ClassEntity
import kotlinx.datetime.DayOfWeek

object TimeTable {

    val MONDAY: List<ClassEntity> = listOf(
        ClassEntity(
            subject = Subject.COMPUTER_GRAPHICS,
            time = "15:00 - 16:00",
            dayOfWeek = DayOfWeek.MONDAY,
            roomNo = "NLH1",
            attendanceStatus = null
        ),
        ClassEntity(
            subject = Subject.SOFTWARE_ENGINEERING,
            time = "16:00 - 17:00",
            dayOfWeek = DayOfWeek.MONDAY,
            roomNo = "NLH1",
            attendanceStatus = null
        ),
        ClassEntity(
            subject = Subject.IMAGE_PROCESSING,
            time = "17:00 - 18:00",
            dayOfWeek = DayOfWeek.MONDAY,
            roomNo = "NLH1",
            attendanceStatus = null
        ),
    )

    val TUESDAY: List<ClassEntity> = listOf(
        ClassEntity(
            subject = Subject.COMPUTER_GRAPHICS_LAB,
            time = "09:00 - 12:00",
            dayOfWeek = DayOfWeek.TUESDAY,
            roomNo = "L3 Lab",
            attendanceStatus = null
        ),
        ClassEntity(
            subject = Subject.SOFTWARE_ENGINEERING,
            time = "14:00 - 15:00",
            dayOfWeek = DayOfWeek.TUESDAY,
            roomNo = "NLH2",
            attendanceStatus = null
        ),
        ClassEntity(
            subject = Subject.SOFTWARE_ENGINEERING,
            time = "15:00 - 16:00",
            dayOfWeek = DayOfWeek.TUESDAY,
            roomNo = "NLH2",
            attendanceStatus = null
        ),
        ClassEntity(
            subject = Subject.COMPUTER_GRAPHICS,
            time = "17:00 - 18:00",
            dayOfWeek = DayOfWeek.TUESDAY,
            roomNo = "NLH2",
            attendanceStatus = null
        ),
    )

    val WEDNESDAY: List<ClassEntity> = listOf(
        ClassEntity(
            subject = Subject.COMPUTER_NETWORK_LAB,
            time = "10:00 - 13:00",
            dayOfWeek = DayOfWeek.WEDNESDAY,
            roomNo = "L3 Lab",
            attendanceStatus = null
        ),
        ClassEntity(
            subject = Subject.DATA_MINING,
            time = "15:00 - 16:00",
            dayOfWeek = DayOfWeek.WEDNESDAY,
            roomNo = "NLH1",
            attendanceStatus = null
        ),
        ClassEntity(
            subject = Subject.DATA_MINING,
            time = "16:00 - 17:00",
            dayOfWeek = DayOfWeek.WEDNESDAY,
            roomNo = "NLH1",
            attendanceStatus = null
        ),
    )

    val THURSDAY: List<ClassEntity> = listOf(
        ClassEntity(
            subject = Subject.COMPUTER_NETWORK,
            time = "14:00 - 15:00",
            dayOfWeek = DayOfWeek.THURSDAY,
            roomNo = "NLH1",
            attendanceStatus = null
        ),
        ClassEntity(
            subject = Subject.DATA_MINING,
            time = "15:00 - 16:00",
            dayOfWeek = DayOfWeek.THURSDAY,
            roomNo = "NLH1",
            attendanceStatus = null
        ),
        ClassEntity(
            subject = Subject.COMPUTER_GRAPHICS,
            time = "16:00 - 17:00",
            dayOfWeek = DayOfWeek.THURSDAY,
            roomNo = "NLH1",
            attendanceStatus = null
        ),
        ClassEntity(
            subject = Subject.COMPUTER_GRAPHICS,
            time = "17:00 - 18:00",
            dayOfWeek = DayOfWeek.THURSDAY,
            roomNo = "NLH1",
            attendanceStatus = null
        ),
    )

    val FRIDAY: List<ClassEntity> = listOf(
        ClassEntity(
            subject = Subject.SOFTWARE_ENGINEERING,
            time = "09:00 - 10:00",
            dayOfWeek = DayOfWeek.FRIDAY,
            roomNo = "GS8",
            attendanceStatus = null
        ),
        ClassEntity(
            subject = Subject.COMPUTER_NETWORK,
            time = "11:00 - 12:00",
            dayOfWeek = DayOfWeek.FRIDAY,
            roomNo = "NLH1",
            attendanceStatus = null
        ),
        ClassEntity(
            subject = Subject.COMPUTER_NETWORK,
            time = "12:00 - 13:00",
            dayOfWeek = DayOfWeek.FRIDAY,
            roomNo = "NLH1",
            attendanceStatus = null
        ),
    )

    val SATURDAY: List<ClassEntity> = emptyList()
    val SUNDAY: List<ClassEntity> = emptyList()
}

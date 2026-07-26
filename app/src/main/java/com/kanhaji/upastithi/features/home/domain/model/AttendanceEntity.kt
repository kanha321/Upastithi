package com.kanhaji.upastithi.features.home.domain.model

import com.kanhaji.upastithi.features.home.data.AttendanceStatus
import com.kanhaji.upastithi.features.home.data.Subject
import kotlinx.datetime.LocalDate
import java.util.UUID

data class AttendanceEntity(
    val attendanceId: UUID = UUID.randomUUID(),
    val timetableId: String = "",
    val date: LocalDate,
    val time: String,
    val subject: Subject,
    val attendanceStatus: AttendanceStatus?,
)

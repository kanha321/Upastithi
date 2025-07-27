package com.kanhaji.upastithi.entity

import com.kanhaji.upastithi.data.Subject
import com.kanhaji.upastithi.data.attendance.AttendanceStatus
import kotlinx.datetime.DayOfWeek
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class ClassEntity(
    val classId: String = UUID.randomUUID().toString(),
    val dayOfWeek: DayOfWeek,
    val time: String,
    val subject: Subject,
    val attendanceStatus: AttendanceStatus?,
    val startTime: Int = time.split(" - ")[0].split(":")[0].toInt(10), // Extracting start time as an Int
)


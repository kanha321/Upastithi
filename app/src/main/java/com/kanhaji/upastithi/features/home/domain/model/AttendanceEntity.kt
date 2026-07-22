package com.kanhaji.upastithi.features.home.domain.model

import com.kanhaji.upastithi.features.home.data.AttendanceStatus
import com.kanhaji.upastithi.features.home.data.Subject
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import java.util.UUID

data class AttendanceEntity(
    val attendanceId: UUID = UUID.randomUUID(),
    val date: LocalDate,
    val time: String,
    val subject: Subject,
    val attendanceStatus: AttendanceStatus?,
)

@Serializable
data class AttendanceEntitySerialized(
    val attendanceId: String = UUID.randomUUID().toString(),
    val date: String,
    val time: String,
    val subject: Subject,
    val attendanceStatus: AttendanceStatus?,
)

fun AttendanceEntity.toSerialized(): AttendanceEntitySerialized {
    return AttendanceEntitySerialized(
        attendanceId = attendanceId.toString(),
        date = date.toString(),
        time = time,
        subject = subject,
        attendanceStatus = attendanceStatus
    )
}

fun AttendanceEntitySerialized.toEntity(): AttendanceEntity {
    return AttendanceEntity(
        date = LocalDate.parse(date),
        time = time,
        subject = subject,
        attendanceStatus = attendanceStatus
    )
}

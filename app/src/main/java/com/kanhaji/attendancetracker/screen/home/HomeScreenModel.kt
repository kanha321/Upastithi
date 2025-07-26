package com.kanhaji.attendancetracker.screen.home

import cafe.adriel.voyager.core.model.ScreenModel
import com.kanhaji.attendancetracker.AndroidContext
import com.kanhaji.attendancetracker.data.attendance.AttendanceStatus
import com.kanhaji.attendancetracker.data.attendance.AttendanceStorage
import com.kanhaji.attendancetracker.entity.AttendanceEntity
import com.kanhaji.attendancetracker.entity.ClassEntity
import kotlinx.datetime.LocalDate
import java.time.DayOfWeek

/*

data class ClassEntity(
    val classId: Int = 0,
    val time: String,
    val subject: Subject,
    val attendanceStatus: AttendanceStatus?,
    val startTime: Int = time.split(" - ")[0].split(":")[0].toInt(10), // Extracting start time as an Int
)

data class AttendanceEntity (
    val date: LocalDate,
    val subject: Subject,
    val attendanceStatus: AttendanceStatus,
)

 */

class HomeScreenModel : ScreenModel {
    fun saveAttendance(
        classEntity: ClassEntity,
        attendanceStatus: AttendanceStatus?,
        date: LocalDate,
        dayOfWeek: DayOfWeek
    ) {
        val attendanceEntity = AttendanceEntity(
            date = date,
            time = classEntity.time,
            subject = classEntity.subject,
            attendanceStatus = attendanceStatus
        )
        AttendanceStorage.addAttendance(AndroidContext.appContext, attendanceEntity)
    }

    fun getAttendanceForDate(date: LocalDate): List<AttendanceEntity> {
        return AttendanceStorage.getAttendanceForDate(date)
    }

    fun getAttendanceForClass(
        date: LocalDate,
        time: String
    ): AttendanceEntity? {
        return AttendanceStorage.getAttendanceForTime(
            date = date,
            time = time
        )
    }
}
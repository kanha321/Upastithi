package com.kanhaji.upastithi.screen.home

import cafe.adriel.voyager.core.model.ScreenModel
import com.kanhaji.upastithi.AndroidContext
import com.kanhaji.upastithi.data.Subject
import com.kanhaji.upastithi.data.attendance.AttendanceStatus
import com.kanhaji.upastithi.data.attendance.AttendanceStorage
import com.kanhaji.upastithi.entity.AttendanceEntity
import com.kanhaji.upastithi.entity.ClassEntity
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate

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

    fun getAttendancesForSubject(
        subject: Subject
    ): Pair<String, Double> {
        val attendances = AttendanceStorage.getAttendancesForSubject(subject)
        // return a pair of strings representing two things first attended Classes/Total Classes and second the percentage of attendance
        // Example: "5/10", 50.0
        // To calculate the total classes held count all the items excluding those having attendanceStatus as leave holiday or cancelled
        // To calculate the attended classes count all the items having attendanceStatus as present or proxy
        val totalClasses = attendances.filter {
            it.attendanceStatus != AttendanceStatus.LEAVE && it.attendanceStatus != AttendanceStatus.HOLIDAY && it.attendanceStatus != AttendanceStatus.CANCELLED
        }.size
        val attendedClasses = attendances.filter {
            it.attendanceStatus == AttendanceStatus.PRESENT || it.attendanceStatus == AttendanceStatus.PROXY
        }.size
        val percentage = if (totalClasses > 0) {
            (attendedClasses.toDouble() / totalClasses * 100).toDouble()
        } else {
            0.0
        }
        return Pair("$attendedClasses/$totalClasses", percentage)
    }
}
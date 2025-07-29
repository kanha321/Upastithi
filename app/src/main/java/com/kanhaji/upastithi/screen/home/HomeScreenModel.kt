package com.kanhaji.upastithi.screen.home

import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.ui.graphics.Color
import cafe.adriel.voyager.core.model.ScreenModel
import com.kanhaji.upastithi.AndroidContext
import com.kanhaji.upastithi.data.Subject
import com.kanhaji.upastithi.data.attendance.AttendanceStatus
import com.kanhaji.upastithi.data.attendance.AttendanceStorage
import com.kanhaji.upastithi.entity.AttendanceEntity
import com.kanhaji.upastithi.entity.ClassEntity
import com.kanhaji.upastithi.util.roundTo
import io.github.boguszpawlowski.composecalendar.kotlinxDateTime.now
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
    // Make this reactive
    val attendanceByDate = mutableStateMapOf<LocalDate, List<AttendanceEntity>>()

    init {
        // Load initial data
        val initial = AttendanceStorage.getAttendanceGroupedByDate(AndroidContext.appContext)
        attendanceByDate.putAll(initial)
    }

    fun saveAttendance(
        classEntity: ClassEntity,
        attendanceStatus: AttendanceStatus?,
        date: LocalDate
    ) {
        val attendanceEntity = AttendanceEntity(
            date = date,
            time = classEntity.time,
            subject = classEntity.subject,
            attendanceStatus = attendanceStatus
        )

        // Update JSON file
        AttendanceStorage.addAttendance(AndroidContext.appContext, attendanceEntity)

        // Update state map after modification
        val updatedList = AttendanceStorage.getAttendanceForDate(date)
        attendanceByDate[date] = updatedList
    }

    fun getAttendanceForDate(date: LocalDate): List<AttendanceEntity> {
        return attendanceByDate[date] ?: emptyList()
    }

    fun getAttendanceForClass(date: LocalDate, time: String): AttendanceEntity? {
        return getAttendanceForDate(date).firstOrNull { it.time == time }
    }

    fun getAttendancesForSubject(subject: Subject): Pair<String, Double> {
        val attendances = AttendanceStorage.getAttendancesForSubject(subject)
        val totalClasses = attendances.count {
            it.attendanceStatus != AttendanceStatus.LEAVE &&
                    it.attendanceStatus != AttendanceStatus.HOLIDAY &&
                    it.attendanceStatus != AttendanceStatus.CANCELLED
        }
        val attendedClasses = attendances.count {
            it.attendanceStatus == AttendanceStatus.PRESENT ||
                    it.attendanceStatus == AttendanceStatus.PROXY
        }
        val percentage = if (totalClasses > 0) {
            (attendedClasses.toDouble() / totalClasses * 100).roundTo(2)
        } else {
            0.0
        }
        return Pair("$attendedClasses/$totalClasses", percentage)
    }

    fun getAttendanceColor(
        classEntity: ClassEntity,
        attendancesForDate: List<AttendanceEntity>,
        defaultColor: Color
    ): Color {
        return attendancesForDate.firstOrNull { it.time == classEntity.time }
            ?.attendanceStatus?.color ?: defaultColor
    }

    fun getAttendanceDotsForDate(date: LocalDate, allAttendances: List<AttendanceEntity>): List<Color> {
        return allAttendances
            .filter { it.date == date && it.attendanceStatus != null }
            .map { it.attendanceStatus!!.color }
    }
}

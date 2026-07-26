package com.kanhaji.upastithi.features.home.data.repository

import android.content.Context
import com.kanhaji.upastithi.AndroidContext
import com.kanhaji.upastithi.features.home.data.AttendanceStatus
import com.kanhaji.upastithi.features.home.data.AttendanceStorage
import com.kanhaji.upastithi.features.home.data.Subject
import com.kanhaji.upastithi.features.home.domain.model.AttendanceEntity
import com.kanhaji.upastithi.features.home.domain.model.ClassEntity
import com.kanhaji.upastithi.features.home.domain.repository.AttendanceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

class AttendanceRepositoryImpl(
    private val context: Context = AndroidContext.appContext
) : AttendanceRepository {

    override fun getAttendanceGroupedByDate(): Flow<Map<LocalDate, List<AttendanceEntity>>> {
        return AttendanceStorage.getAttendanceFlow(context)
    }

    override fun getAttendanceForDate(date: LocalDate): List<AttendanceEntity> {
        return AttendanceStorage.getAttendanceForDate(date, context)
    }

    override fun getAttendanceForClass(date: LocalDate, time: String): AttendanceEntity? {
        return AttendanceStorage.getAttendanceForTime(date, time)
    }

    override fun getSubjectStats(subject: Subject): Pair<String, Double> {
        val attendances = AttendanceStorage.getAttendancesForSubject(subject, context)
            .filter { it.attendanceStatus != null }

        if (attendances.isEmpty()) {
            return Pair("0/0", 0.0)
        }

        val totalClasses = attendances.count {
            it.attendanceStatus != AttendanceStatus.LEAVE &&
            it.attendanceStatus != AttendanceStatus.HOLIDAY &&
            it.attendanceStatus != AttendanceStatus.CANCELLED
        }

        if (totalClasses == 0) {
            return Pair("0/0", 0.0)
        }

        val attendedClasses = attendances.count {
            it.attendanceStatus == AttendanceStatus.PRESENT ||
            it.attendanceStatus == AttendanceStatus.PROXY
        }
        val percentage = (attendedClasses.toDouble() / totalClasses.toDouble()) * 100

        return Pair("$attendedClasses/$totalClasses", percentage)
    }

    override suspend fun saveAttendance(
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
        AttendanceStorage.addAttendance(context, attendanceEntity)
    }
}

package com.kanhaji.upastithi.features.home.domain.repository

import com.kanhaji.upastithi.features.home.data.AttendanceStatus
import com.kanhaji.upastithi.features.home.data.Subject
import com.kanhaji.upastithi.features.home.domain.model.AttendanceEntity
import com.kanhaji.upastithi.features.home.domain.model.ClassEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

interface AttendanceRepository {
    fun getAttendanceGroupedByDate(): Flow<Map<LocalDate, List<AttendanceEntity>>>
    fun getAttendanceForDate(date: LocalDate): List<AttendanceEntity>
    fun getAttendanceForClass(date: LocalDate, time: String): AttendanceEntity?
    fun getSubjectStats(subject: Subject): Pair<String, Double>
    suspend fun saveAttendance(classEntity: ClassEntity, attendanceStatus: AttendanceStatus?, date: LocalDate)
}

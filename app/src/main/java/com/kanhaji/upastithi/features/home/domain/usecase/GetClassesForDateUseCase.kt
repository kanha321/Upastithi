package com.kanhaji.upastithi.features.home.domain.usecase

import com.kanhaji.upastithi.data.TimeTableManager
import com.kanhaji.upastithi.features.home.data.repository.AttendanceRepositoryImpl
import com.kanhaji.upastithi.features.home.domain.model.ClassEntity
import com.kanhaji.upastithi.features.home.domain.repository.AttendanceRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate

class GetClassesForDateUseCase(
    private val attendanceRepository: AttendanceRepository = AttendanceRepositoryImpl()
) {
    suspend operator fun invoke(date: LocalDate): List<ClassEntity> = withContext(Dispatchers.IO) {
        // Single source of truth: TimeTableManager handles base schedule + shift overrides
        val classes = TimeTableManager.getClasses(date)

        val markedAttendances = attendanceRepository.getAttendanceForDate(date)

        classes.map { classItem ->
            val status = markedAttendances.find { it.time == classItem.time }?.attendanceStatus
            classItem.copy(attendanceStatus = status)
        }.sortedBy { it.startTime }
    }
}

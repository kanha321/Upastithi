package com.kanhaji.upastithi.features.home.domain.usecase

import com.kanhaji.upastithi.features.home.data.repository.AttendanceRepositoryImpl
import com.kanhaji.upastithi.features.home.data.repository.TimetableRepositoryImpl
import com.kanhaji.upastithi.features.home.domain.model.ClassEntity
import com.kanhaji.upastithi.features.home.domain.repository.AttendanceRepository
import com.kanhaji.upastithi.features.home.domain.repository.TimetableRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate

class GetClassesForDateUseCase(
    private val timetableRepository: TimetableRepository = TimetableRepositoryImpl(),
    private val attendanceRepository: AttendanceRepository = AttendanceRepositoryImpl()
) {
    suspend operator fun invoke(date: LocalDate): List<ClassEntity> = withContext(Dispatchers.IO) {
        val baseClasses = timetableRepository.getClassesForDayDirect(date.dayOfWeek)
        val markedAttendances = attendanceRepository.getAttendanceForDate(date)

        baseClasses.map { classItem ->
            val status = markedAttendances.find { it.time == classItem.time }?.attendanceStatus
            classItem.copy(attendanceStatus = status)
        }
    }
}

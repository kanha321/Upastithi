package com.kanhaji.upastithi.features.home.domain.usecase

import com.kanhaji.upastithi.features.home.data.AttendanceStorage
import com.kanhaji.upastithi.features.home.domain.model.ClassEntity
import com.kanhaji.upastithi.util.getClasses
import kotlinx.datetime.LocalDate

class GetClassesForDateUseCase(
    private val attendanceStorage: AttendanceStorage = AttendanceStorage
) {
    operator fun invoke(date: LocalDate): List<ClassEntity> {
        val baseClasses = date.dayOfWeek.getClasses()
        val markedAttendances = attendanceStorage.getAttendanceForDate(date)

        return baseClasses.map { classItem ->
            val status = markedAttendances.find { it.time == classItem.time }?.attendanceStatus
            classItem.copy(attendanceStatus = status)
        }
    }
}

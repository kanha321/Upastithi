package com.kanhaji.upasthiti.features.home.domain.usecase

import com.kanhaji.upasthiti.features.home.data.AttendanceStatus
import com.kanhaji.upasthiti.features.home.data.repository.AttendanceRepositoryImpl
import com.kanhaji.upasthiti.features.home.domain.model.ClassEntity
import com.kanhaji.upasthiti.features.home.domain.repository.AttendanceRepository
import kotlinx.datetime.LocalDate

class SaveAttendanceUseCase(
    private val attendanceRepository: AttendanceRepository = AttendanceRepositoryImpl()
) {
    suspend operator fun invoke(
        classEntity: ClassEntity,
        attendanceStatus: AttendanceStatus?,
        date: LocalDate
    ) {
        attendanceRepository.saveAttendance(
            classEntity = classEntity,
            attendanceStatus = attendanceStatus,
            date = date
        )
    }
}

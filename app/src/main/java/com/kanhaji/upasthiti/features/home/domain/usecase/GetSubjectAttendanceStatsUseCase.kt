package com.kanhaji.upasthiti.features.home.domain.usecase

import com.kanhaji.upasthiti.features.home.data.Subject
import com.kanhaji.upasthiti.features.home.data.repository.AttendanceRepositoryImpl
import com.kanhaji.upasthiti.features.home.domain.repository.AttendanceRepository

class GetSubjectAttendanceStatsUseCase(
    private val attendanceRepository: AttendanceRepository = AttendanceRepositoryImpl()
) {
    operator fun invoke(subject: Subject): Pair<String, Double> {
        return attendanceRepository.getSubjectStats(subject)
    }
}

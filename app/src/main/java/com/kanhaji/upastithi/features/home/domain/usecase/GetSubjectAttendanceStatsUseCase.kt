package com.kanhaji.upastithi.features.home.domain.usecase

import com.kanhaji.upastithi.features.home.data.AttendanceStatus
import com.kanhaji.upastithi.features.home.data.AttendanceStorage
import com.kanhaji.upastithi.features.home.data.Subject
import com.kanhaji.upastithi.util.roundTo

class GetSubjectAttendanceStatsUseCase(
    private val attendanceStorage: AttendanceStorage = AttendanceStorage
) {
    operator fun invoke(subject: Subject): Pair<String, Double> {
        val attendances = attendanceStorage.getAttendancesForSubject(subject)
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
}

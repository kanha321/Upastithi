package com.kanhaji.upastithi.features.home.domain.usecase

import com.kanhaji.upastithi.AndroidContext
import com.kanhaji.upastithi.features.home.data.AttendanceStatus
import com.kanhaji.upastithi.features.home.data.AttendanceStorage
import com.kanhaji.upastithi.features.home.domain.model.AttendanceEntity
import com.kanhaji.upastithi.features.home.domain.model.ClassEntity
import kotlinx.datetime.LocalDate

class SaveAttendanceUseCase(
    private val attendanceStorage: AttendanceStorage = AttendanceStorage
) {
    operator fun invoke(
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
        attendanceStorage.addAttendance(AndroidContext.appContext, attendanceEntity)
    }
}

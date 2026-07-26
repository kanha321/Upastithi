package com.kanhaji.upastithi.features.home.ui

import com.kanhaji.upastithi.features.home.data.Subject
import com.kanhaji.upastithi.features.home.data.local.entity.TimetableMetadataEntity
import com.kanhaji.upastithi.features.home.domain.model.AttendanceEntity
import kotlinx.datetime.LocalDate

data class HomeScreenUiState(
    val activeTimetable: TimetableMetadataEntity? = null,
    val attendanceByDate: Map<LocalDate, List<AttendanceEntity>> = emptyMap(),
    val subjects: List<Subject> = emptyList(),
    val isUpdateAvailable: Boolean = false,
    val isLoading: Boolean = false
)

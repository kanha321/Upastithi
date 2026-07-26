package com.kanhaji.upastithi.features.home.domain.repository

import com.kanhaji.upastithi.features.home.data.Subject
import com.kanhaji.upastithi.features.home.data.local.entity.ScheduleEventEntity
import com.kanhaji.upastithi.features.home.data.local.entity.TimetableMetadataEntity
import com.kanhaji.upastithi.features.home.domain.model.ClassEntity
import com.kanhaji.upastithi.features.home.domain.model.ScheduleEvent
import com.kanhaji.upastithi.features.home.domain.model.TimetableData
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.DayOfWeek

interface TimetableRepository {
    fun getActiveTimetable(): Flow<TimetableMetadataEntity?>
    fun getClassesForDay(dayOfWeek: DayOfWeek): Flow<List<ClassEntity>>
    suspend fun getClassesForDayDirect(dayOfWeek: DayOfWeek): List<ClassEntity>
    fun getSubjects(): Flow<List<Subject>>
    suspend fun getSubjectsDirect(): List<Subject>
    suspend fun setParsedTimetable(data: TimetableData, name: String, pageIndex: Int)
    suspend fun saveCustomEvent(event: ScheduleEvent, courseName: String?)
    suspend fun updateCustomEvent(originalEvent: ScheduleEvent, updatedEvent: ScheduleEvent, courseName: String?)
    suspend fun deleteCustomEvent(event: ScheduleEvent)
    suspend fun resetToOriginalPdf(originalData: TimetableData?)
    fun findCollidingEvent(day: String, startTimeStr: String, endTimeStr: String, excludeEventId: String? = null): ScheduleEvent?
    fun getCourseName(courseCode: String): String
}

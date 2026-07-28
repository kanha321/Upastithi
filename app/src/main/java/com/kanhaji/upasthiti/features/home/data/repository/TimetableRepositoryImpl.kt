package com.kanhaji.upasthiti.features.home.data.repository

import android.content.Context
import android.util.Log
import com.kanhaji.upasthiti.AndroidContext
import com.kanhaji.upasthiti.data.TimeTableManager
import com.kanhaji.upasthiti.features.home.data.Subject
import com.kanhaji.upasthiti.features.home.data.local.UpasthitiDatabase
import com.kanhaji.upasthiti.features.home.data.local.entity.ClassShiftOverrideEntity
import com.kanhaji.upasthiti.features.home.data.local.entity.ScheduleEventEntity
import com.kanhaji.upasthiti.features.home.data.local.entity.SubjectEntity
import com.kanhaji.upasthiti.features.home.data.local.entity.TimetableMetadataEntity
import com.kanhaji.upasthiti.features.home.domain.model.ClassEntity
import com.kanhaji.upasthiti.features.home.domain.model.CourseInfo
import com.kanhaji.upasthiti.features.home.domain.model.ScheduleEvent
import com.kanhaji.upasthiti.features.home.domain.model.TimetableData
import com.kanhaji.upasthiti.features.home.domain.repository.TimetableRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.datetime.DayOfWeek

class TimetableRepositoryImpl(
    private val context: Context = AndroidContext.appContext
) : TimetableRepository {

    private val db = UpasthitiDatabase.getInstance(context)
    private val metadataDao = db.timetableMetadataDao()
    private val eventDao = db.scheduleEventDao()
    private val subjectDao = db.subjectDao()
    private val shiftOverrideDao = db.classShiftOverrideDao()

    override fun getActiveTimetable(): Flow<TimetableMetadataEntity?> {
        return metadataDao.getActiveTimetable()
    }

    override suspend fun getActiveTimetableDirect(): TimetableMetadataEntity? = withContext(Dispatchers.IO) {
        metadataDao.getActiveTimetableDirect()
    }

    override fun getClassesForDay(dayOfWeek: DayOfWeek): Flow<List<ClassEntity>> {
        val dayName = dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() }
        return getActiveTimetable().flatMapLatest { meta ->
            val activeId = meta?.id ?: TimeTableManager.getTimetableId()
            eventDao.getEventsForDay(activeId, dayName).map { eventEntities ->
                if (eventEntities.isNotEmpty()) {
                    eventEntities.map { it.toClassEntity(dayOfWeek) }
                } else {
                    TimeTableManager.getClasses(dayOfWeek)
                }
            }
        }
    }

    override suspend fun getClassesForDayDirect(dayOfWeek: DayOfWeek): List<ClassEntity> = withContext(Dispatchers.IO) {
        val activeMeta = metadataDao.getActiveTimetableDirect()
        val activeId = activeMeta?.id ?: TimeTableManager.getTimetableId()
        val dayName = dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() }
        val eventEntities = eventDao.getEventsForDayDirect(activeId, dayName)
        if (eventEntities.isNotEmpty()) {
            eventEntities.map { it.toClassEntity(dayOfWeek) }
        } else {
            TimeTableManager.getClasses(dayOfWeek)
        }
    }

    override fun getSubjects(): Flow<List<Subject>> {
        return getActiveTimetable().flatMapLatest { meta ->
            val activeId = meta?.id ?: TimeTableManager.getTimetableId()
            subjectDao.getAllSubjects(activeId).map { entities ->
                entities.map { it.toDomainSubject() }
            }
        }
    }

    override suspend fun getSubjectsDirect(): List<Subject> = withContext(Dispatchers.IO) {
        val activeMeta = metadataDao.getActiveTimetableDirect()
        val activeId = activeMeta?.id ?: TimeTableManager.getTimetableId()
        subjectDao.getAllSubjectsDirect(activeId).map { it.toDomainSubject() }
    }

    override suspend fun setParsedTimetable(data: TimetableData, name: String, pageIndex: Int) {
        withContext(Dispatchers.IO) {
            val timetableId = TimeTableManager.getTimetableId(data)
            metadataDao.deactivateAll()

            // Automatically migrate any legacy unassigned attendance records to this active timetable ID
            db.attendanceDao().migrateUnassignedAttendances(timetableId)

            val metadata = TimetableMetadataEntity(
                id = timetableId,
                name = name,
                pageIndex = pageIndex,
                semester = data.semester,
                isCustomized = false,
                isActive = true
            )
            metadataDao.insertOrUpdate(metadata)

            val existingEvents = eventDao.getAllEventsForTimetableDirect(timetableId)
            val finalSchedule: List<ScheduleEvent> = if (existingEvents.isNotEmpty()) {
                existingEvents.map { entity ->
                    ScheduleEvent(
                        id = entity.id,
                        day = entity.dayOfWeek,
                        time = entity.time,
                        start_time = entity.startTime,
                        end_time = entity.endTime,
                        course_code = entity.courseCode,
                        type = entity.type,
                        location = entity.location,
                        group = entity.group,
                        faculty_abbr = entity.facultyAbbr,
                        faculty_name = entity.facultyName
                    )
                }
            } else {
                val eventEntities = data.schedule.map { event ->
                    val startMins = parseTimeToMinutes(event.time.substringBefore(" - "))
                    val endMins = parseTimeToMinutes(event.time.substringAfter(" - "))
                    ScheduleEventEntity(
                        id = event.id,
                        timetableId = timetableId,
                        dayOfWeek = event.day,
                        time = event.time,
                        startTime = event.start_time,
                        endTime = event.end_time,
                        startMinutes = startMins,
                        endMinutes = endMins,
                        courseCode = event.course_code,
                        type = event.type,
                        location = event.location,
                        group = event.group,
                        facultyAbbr = event.faculty_abbr,
                        facultyName = event.faculty_name
                    )
                }
                eventDao.insertAll(eventEntities)
                data.schedule
            }

            // Register subjects into Room DB
            val uniqueCodes = finalSchedule.map { it.course_code }.distinct()
            val subjectEntities = uniqueCodes.mapNotNull { code ->
                val courseInfo = data.courses.find { it.code.equals(code, ignoreCase = true) }
                val displayName = courseInfo?.name?.ifEmpty { null } ?: code
                val isPractical = finalSchedule.filter { it.course_code.equals(code, ignoreCase = true) }
                    .any { it.type.equals("Practical", ignoreCase = true) || it.type.equals("P", ignoreCase = true) }

                val finalName = if (isPractical && !displayName.startsWith("(Lab)")) "(Lab) $displayName" else displayName
                val facultyName = finalSchedule.firstOrNull { it.course_code.equals(code, ignoreCase = true) }?.faculty_name ?: ""
                val facultyAbbr = finalSchedule.firstOrNull { it.course_code.equals(code, ignoreCase = true) }?.faculty_abbr ?: ""

                SubjectEntity(
                    subjectId = code,
                    displayName = finalName,
                    teacher = facultyName,
                    teacherInitials = facultyAbbr,
                    timetableId = timetableId
                )
            }
            subjectDao.deleteByTimetableId(timetableId)
            subjectDao.insertAll(subjectEntities)

            // Sync with TimeTableManager
            val updatedData = data.copy(schedule = finalSchedule)
            TimeTableManager.setParsedTimetable(updatedData, context)
        }
    }

    override suspend fun saveCustomEvent(event: ScheduleEvent, courseName: String?) {
        withContext(Dispatchers.IO) {
            var activeMeta = metadataDao.getActiveTimetableDirect()
            if (activeMeta == null) {
                val fallbackId = TimeTableManager.getTimetableId()
                activeMeta = TimetableMetadataEntity(
                    id = fallbackId,
                    name = "Default Timetable",
                    pageIndex = 0,
                    semester = TimeTableManager.activeTimetableData?.semester ?: "",
                    isCustomized = true,
                    isActive = true
                )
                metadataDao.insertOrUpdate(activeMeta)
            }
            val timetableId = activeMeta.id

            val startMins = parseTimeToMinutes(event.time.substringBefore(" - "))
            val endMins = parseTimeToMinutes(event.time.substringAfter(" - "))

            val entity = ScheduleEventEntity(
                id = event.id,
                timetableId = timetableId,
                dayOfWeek = event.day,
                time = event.time,
                startTime = event.start_time,
                endTime = event.end_time,
                startMinutes = startMins,
                endMinutes = endMins,
                courseCode = event.course_code,
                type = event.type,
                location = event.location,
                group = event.group,
                facultyAbbr = event.faculty_abbr,
                facultyName = event.faculty_name
            )
            eventDao.insertOrUpdate(entity)
            TimeTableManager.addEvent(event, courseName, context)
        }
    }

    override suspend fun updateCustomEvent(
        originalEvent: ScheduleEvent,
        updatedEvent: ScheduleEvent,
        courseName: String?
    ) {
        withContext(Dispatchers.IO) {
            var activeMeta = metadataDao.getActiveTimetableDirect()
            if (activeMeta == null) {
                val fallbackId = TimeTableManager.getTimetableId()
                activeMeta = TimetableMetadataEntity(
                    id = fallbackId,
                    name = "Default Timetable",
                    pageIndex = 0,
                    semester = TimeTableManager.activeTimetableData?.semester ?: "",
                    isCustomized = true,
                    isActive = true
                )
                metadataDao.insertOrUpdate(activeMeta)
            }
            val timetableId = activeMeta.id

            val startMins = parseTimeToMinutes(updatedEvent.time.substringBefore(" - "))
            val endMins = parseTimeToMinutes(updatedEvent.time.substringAfter(" - "))

            val entity = ScheduleEventEntity(
                id = updatedEvent.id,
                timetableId = timetableId,
                dayOfWeek = updatedEvent.day,
                time = updatedEvent.time,
                startTime = updatedEvent.start_time,
                endTime = updatedEvent.end_time,
                startMinutes = startMins,
                endMinutes = endMins,
                courseCode = updatedEvent.course_code,
                type = updatedEvent.type,
                location = updatedEvent.location,
                group = updatedEvent.group,
                facultyAbbr = updatedEvent.faculty_abbr,
                facultyName = updatedEvent.faculty_name
            )
            eventDao.insertOrUpdate(entity)
            TimeTableManager.updateEvent(originalEvent, updatedEvent, courseName, context)
        }
    }

    override suspend fun deleteCustomEvent(event: ScheduleEvent) {
        withContext(Dispatchers.IO) {
            eventDao.deleteById(event.id)
            TimeTableManager.deleteEvent(event, context)
        }
    }

    override suspend fun resetToOriginalPdf(originalData: TimetableData?) {
        withContext(Dispatchers.IO) {
            val activeMeta = getActiveTimetableDirect()
            val timetableId = activeMeta?.id ?: TimeTableManager.getTimetableId()
            val overrides = shiftOverrideDao.getOverridesForTimetableDirect(timetableId)
            val attendanceDao = db.attendanceDao()
            overrides.forEach { override ->
                attendanceDao.updateShiftedAttendanceTime(
                    timetableId = timetableId,
                    subjectId = override.courseCode,
                    originalTime = override.newTime,
                    newTime = override.originalTime,
                    effectiveDate = override.effectiveDate
                )
            }
            shiftOverrideDao.deleteByTimetableId(timetableId)
            eventDao.deleteByTimetableId(timetableId)
            if (activeMeta != null) {
                metadataDao.insertOrUpdate(activeMeta.copy(isCustomized = false))
            }
            TimeTableManager.resetToOriginalPdf(context, originalData)
            if (originalData != null) {
                setParsedTimetable(originalData, activeMeta?.name ?: "Original.pdf", activeMeta?.pageIndex ?: 0)
            }
        }
    }

    override fun findCollidingEvent(
        day: String,
        startTimeStr: String,
        endTimeStr: String,
        excludeEventId: String?
    ): ScheduleEvent? {
        return TimeTableManager.findCollidingEvent(day, startTimeStr, endTimeStr, excludeEventId)
    }

    override fun getCourseName(courseCode: String): String {
        return TimeTableManager.getCourseName(courseCode)
    }

    override suspend fun saveClassShiftOverride(override: ClassShiftOverrideEntity) {
        withContext(Dispatchers.IO) {
            shiftOverrideDao.insertOrUpdate(override)
        }
    }

    override suspend fun getClassShiftOverridesDirect(): List<ClassShiftOverrideEntity> = withContext(Dispatchers.IO) {
        val activeMeta = metadataDao.getActiveTimetableDirect()
        val activeId = activeMeta?.id ?: TimeTableManager.getTimetableId()
        shiftOverrideDao.getOverridesForTimetableDirect(activeId)
    }

    private fun parseTimeToMinutes(timeStr: String): Int {
        val clean = timeStr.trim().take(5)
        val parts = clean.split(":").mapNotNull { it.toIntOrNull() }
        if (parts.size >= 2) {
            return parts[0] * 60 + parts[1]
        }
        return 0
    }
}

fun ScheduleEventEntity.toClassEntity(dayOfWeek: DayOfWeek): ClassEntity {
    val subject = Subject(
        displayName = courseCode,
        subjectId = courseCode,
        teacher = facultyName ?: "",
        teacherInitials = facultyAbbr ?: ""
    )
    return ClassEntity(
        subject = subject,
        time = time,
        dayOfWeek = dayOfWeek,
        roomNo = location ?: "N/A",
        attendanceStatus = null,
        group = group
    )
}

fun SubjectEntity.toDomainSubject(): Subject {
    return Subject(
        displayName = displayName,
        subjectId = subjectId,
        teacher = teacher,
        teacherInitials = teacherInitials
    )
}

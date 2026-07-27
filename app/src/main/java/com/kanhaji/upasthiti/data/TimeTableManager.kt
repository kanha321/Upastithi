package com.kanhaji.upasthiti.data

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.kanhaji.upasthiti.features.home.data.Subject
import com.kanhaji.upasthiti.features.home.data.TimeTable
import com.kanhaji.upasthiti.features.home.domain.model.ClassEntity
import com.kanhaji.upasthiti.features.home.domain.model.CourseInfo
import com.kanhaji.upasthiti.features.home.domain.model.ScheduleEvent
import com.kanhaji.upasthiti.features.home.domain.model.TimetableData
import com.kanhaji.upasthiti.features.home.domain.model.TimetableSource
import androidx.compose.runtime.mutableStateListOf
import com.kanhaji.upasthiti.features.home.data.local.entity.ClassShiftOverrideEntity
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.coroutines.launch
import java.io.File

object TimeTableManager {
    var activeTimetableData by mutableStateOf<TimetableData?>(null)
        private set

    private var hasManualEdits by mutableStateOf(false)

    var isCustomized by mutableStateOf(false)
        private set

    private fun checkCustomized() {
        isCustomized = hasManualEdits || classShiftOverrides.isNotEmpty()
    }

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    fun getTimetableId(): String {
        val data = activeTimetableData ?: return "default"
        val scheduleStr = data.schedule.joinToString("|") { "${it.day}_${it.time}_${it.course_code}" }
        val coursesStr = data.courses.joinToString("|") { "${it.code}_${it.name}" }
        return (scheduleStr + coursesStr).hashCode().toString()
    }

    fun saveOriginalTimetableJson(data: TimetableData, context: Context) {
        try {
            val file = File(context.filesDir, "original_timetable.json")
            val baseData = data.copy(
                source = data.source,
                is_modified = false
            )
            val jsonString = json.encodeToString(baseData)
            file.writeText(jsonString)
        } catch (e: Exception) {
            Log.e("TimeTableManager", "Failed to save original_timetable.json: ${e.message}")
        }
    }

    fun loadOriginalTimetableJson(context: Context): TimetableData? {
        try {
            val file = File(context.filesDir, "original_timetable.json")
            if (file.exists()) {
                val jsonString = file.readText()
                return json.decodeFromString<TimetableData>(jsonString)
            }
        } catch (e: Exception) {
            Log.e("TimeTableManager", "Failed to load original_timetable.json: ${e.message}")
        }
        return null
    }

    fun saveModifiedTimetableJson(data: TimetableData, context: Context) {
        try {
            val file = File(context.filesDir, "modified_timetable.json")
            val modifiedData = data.copy(is_modified = true)
            val jsonString = json.encodeToString(modifiedData)
            file.writeText(jsonString)
        } catch (e: Exception) {
            Log.e("TimeTableManager", "Failed to save modified_timetable.json: ${e.message}")
        }
    }

    fun loadModifiedTimetableJson(context: Context): TimetableData? {
        try {
            val file = File(context.filesDir, "modified_timetable.json")
            if (file.exists()) {
                val jsonString = file.readText()
                return json.decodeFromString<TimetableData>(jsonString)
            }
        } catch (e: Exception) {
            Log.e("TimeTableManager", "Failed to load modified_timetable.json: ${e.message}")
        }
        return null
    }

    fun importTimetableJson(jsonString: String, context: Context): TimetableData {
        val imported = json.decodeFromString<TimetableData>(jsonString)
        val wasModifiedByAuthor = imported.is_modified || imported.modified_by_author
        val importedBase = imported.copy(
            source = TimetableSource.IMPORTED,
            is_modified = false,
            modified_by_author = wasModifiedByAuthor
        )
        clearModifiedTimetableJson(context)
        saveOriginalTimetableJson(importedBase, context)
        setParsedTimetable(importedBase, context)
        return importedBase
    }

    fun clearModifiedTimetableJson(context: Context) {
        try {
            val file = File(context.filesDir, "modified_timetable.json")
            if (file.exists()) {
                file.delete()
            }
            val legacyFile = File(context.filesDir, "custom_edited_timetable.json")
            if (legacyFile.exists()) {
                legacyFile.delete()
            }
        } catch (e: Exception) {
            Log.e("TimeTableManager", "Failed to clear modified_timetable.json: ${e.message}")
        }
    }

    fun getOriginalTimetableFile(context: Context): File {
        return File(context.filesDir, "original_timetable.json")
    }

    fun getModifiedTimetableFile(context: Context): File {
        return File(context.filesDir, "modified_timetable.json")
    }

    fun setParsedTimetable(data: TimetableData?, context: Context? = null) {
        if (data != null && context != null) {
            saveOriginalTimetableJson(data, context)
        }
        val modifiedData = context?.let { loadModifiedTimetableJson(it) }
        if (modifiedData != null) {
            activeTimetableData = modifiedData
            hasManualEdits = true
        } else {
            activeTimetableData = data
            hasManualEdits = false
        }
        classShiftOverrides.clear()
        checkCustomized()
        val active = activeTimetableData
        if (active != null) {
            registerDynamicSubjects(active)
        } else {
            Subject.clearDynamicSubjects()
        }
    }

    fun updateEvent(
        originalEvent: ScheduleEvent,
        updatedEvent: ScheduleEvent,
        updatedCourseName: String?,
        context: Context
    ) {
        val current = activeTimetableData ?: return

        // 1. Remove any conflicting shift override for this course code
        classShiftOverrides.removeAll { override ->
            override.courseCode.equals(originalEvent.course_code, ignoreCase = true) ||
                    override.courseCode.equals(updatedEvent.course_code, ignoreCase = true)
        }

        // 2. Match event by ID or course_code + day
        val newSchedule = current.schedule.map { event ->
            if (event.id == originalEvent.id || (event.course_code.equals(originalEvent.course_code, ignoreCase = true) && event.day.equals(originalEvent.day, ignoreCase = true))) {
                updatedEvent
            } else {
                event
            }
        }.sortedWith(compareBy({ it.day }, { parseTimeToMinutes(it.time) }))

        val updatedCourses = updateCourseInfoList(current.courses, updatedEvent.course_code, updatedCourseName)
        val updatedData = current.copy(
            schedule = newSchedule,
            courses = updatedCourses
        )

        saveModifiedTimetableJson(updatedData, context)

        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main.immediate).launch {
            activeTimetableData = updatedData
            hasManualEdits = true
            checkCustomized()
            registerDynamicSubjects(updatedData)
        }
    }

    fun addEvent(
        newEvent: ScheduleEvent,
        courseName: String?,
        context: Context
    ) {
        val current = activeTimetableData ?: return
        val newSchedule = (current.schedule + newEvent)
            .sortedWith(compareBy({ it.day }, { parseTimeToMinutes(it.time) }))
        val updatedCourses = updateCourseInfoList(current.courses, newEvent.course_code, courseName)

        val updatedData = current.copy(
            schedule = newSchedule,
            courses = updatedCourses
        )

        saveModifiedTimetableJson(updatedData, context)

        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main.immediate).launch {
            activeTimetableData = updatedData
            hasManualEdits = true
            checkCustomized()
            registerDynamicSubjects(updatedData)
        }
    }

    fun deleteEvent(
        eventToDelete: ScheduleEvent,
        context: Context
    ) {
        val current = activeTimetableData ?: return

        classShiftOverrides.removeAll { override ->
            override.courseCode.equals(eventToDelete.course_code, ignoreCase = true) &&
                    override.originalTime.equals(eventToDelete.time, ignoreCase = true)
        }

        val newSchedule = current.schedule.filter { event ->
            if (eventToDelete.id.isNotBlank()) {
                event.id != eventToDelete.id
            } else {
                !(event.course_code.equals(eventToDelete.course_code, ignoreCase = true) &&
                        event.day.equals(eventToDelete.day, ignoreCase = true) &&
                        event.time.equals(eventToDelete.time, ignoreCase = true))
            }
        }
        val updatedData = current.copy(schedule = newSchedule)

        saveModifiedTimetableJson(updatedData, context)

        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main.immediate).launch {
            activeTimetableData = updatedData
            hasManualEdits = true
            checkCustomized()
            registerDynamicSubjects(updatedData)
        }
    }

    fun resetToOriginalPdf(context: Context, originalPdfData: TimetableData? = null) {
        clearModifiedTimetableJson(context)
        hasManualEdits = false
        classShiftOverrides.clear()
        checkCustomized()
        val restoredData = loadOriginalTimetableJson(context) ?: originalPdfData
        activeTimetableData = restoredData
        if (restoredData != null) {
            registerDynamicSubjects(restoredData)
        } else {
            Subject.clearDynamicSubjects()
        }
    }

    fun parseTimeToMinutes(timeStr: String): Int {
        val startTime = timeStr.split("-").firstOrNull()?.trim() ?: timeStr.trim()
        val clean = startTime.take(5)
        val parts = clean.split(":").mapNotNull { it.toIntOrNull() }
        if (parts.size >= 2) {
            return parts[0] * 60 + parts[1]
        }
        return 0
    }

    fun findCollidingEvent(
        day: String,
        startTimeStr: String,
        endTimeStr: String,
        excludeEventId: String? = null
    ): ScheduleEvent? {
        val schedule = getScheduleEventsForDay(day)
        val targetStart = parseTimeToMinutes(startTimeStr)
        val targetEnd = parseTimeToMinutes(endTimeStr)

        if (targetStart >= targetEnd) return null

        return schedule.find { event ->
            if (excludeEventId != null && event.id == excludeEventId) {
                false
            } else {
                val evStart = parseTimeToMinutes(event.start_time.ifEmpty { event.time.split("-").getOrNull(0) ?: "" })
                val evEnd = parseTimeToMinutes(event.end_time.ifEmpty { event.time.split("-").getOrNull(1) ?: "" })
                val maxStart = maxOf(targetStart, evStart)
                val minEnd = minOf(targetEnd, evEnd)
                maxStart < minEnd
            }
        }
    }

    fun getCourseName(courseCode: String): String {
        val data = activeTimetableData ?: return courseCode
        val courseInfo = data.courses.find { it.code.equals(courseCode, ignoreCase = true) }
        return courseInfo?.name?.ifEmpty { null } ?: courseCode
    }

    fun getCourseCode(courseName: String): String? {
        val data = activeTimetableData ?: return null
        val cleanSearch = courseName
            .replace(Regex("(?i)^\\(lab\\)\\s*"), "")
            .replace(Regex("(?i)\\s*\\(lab\\)$"), "")
            .trim()
        val courseInfo = data.courses.find {
            it.name.equals(cleanSearch, ignoreCase = true) ||
                    it.name.equals(courseName, ignoreCase = true)
        }
        return courseInfo?.code
    }

    private fun updateCourseInfoList(
        courses: List<CourseInfo>,
        courseCode: String,
        courseName: String?
    ): List<CourseInfo> {
        val nameToUse = courseName ?: courseCode
        val existingIndex = courses.indexOfFirst { it.code.equals(courseCode, ignoreCase = true) }
        return if (existingIndex != -1) {
            courses.mapIndexed { idx, info ->
                if (idx == existingIndex) info.copy(name = nameToUse) else info
            }
        } else {
            courses + CourseInfo(code = courseCode, name = nameToUse, details = "")
        }
    }



    private fun registerDynamicSubjects(data: TimetableData) {
        val uniqueCodes = data.schedule.map { it.course_code }.distinct()
        val subjects = uniqueCodes.mapNotNull { code ->
            val courseInfo = data.courses.find { it.code.equals(code, ignoreCase = true) }
            val displayName = courseInfo?.name?.ifEmpty { null } ?: code

            val events = data.schedule.filter { it.course_code.equals(code, ignoreCase = true) }
            val isPractical = events.any { it.type.equals("Practical", ignoreCase = true) || it.type.equals("P", ignoreCase = true) }

            val facultyName = events.firstOrNull { !it.faculty_name.isNullOrBlank() }?.faculty_name ?: ""
            val facultyAbbr = events.firstOrNull { !it.faculty_abbr.isNullOrBlank() }?.faculty_abbr
                ?: data.faculty.entries.firstOrNull { it.value.equals(facultyName, ignoreCase = true) }?.key
                ?: ""

            val finalName = if (isPractical && !displayName.startsWith("(Lab)")) {
                "(Lab) $displayName"
            } else {
                displayName
            }

            Subject(
                displayName = finalName,
                subjectId = code,
                teacher = facultyName,
                teacherInitials = facultyAbbr
            )
        }
        Subject.registerSubjects(subjects)
    }

    val classShiftOverrides = mutableStateListOf<ClassShiftOverrideEntity>()

    fun setClassShiftOverrides(overrides: List<ClassShiftOverrideEntity>) {
        classShiftOverrides.clear()
        classShiftOverrides.addAll(overrides)
        checkCustomized()
    }

    fun addClassShiftOverride(override: ClassShiftOverrideEntity) {
        classShiftOverrides.removeAll {
            it.courseCode.equals(override.courseCode, ignoreCase = true) &&
                    it.originalDayOfWeek.equals(override.originalDayOfWeek, ignoreCase = true) &&
                    it.originalTime.equals(override.originalTime, ignoreCase = true)
        }
        classShiftOverrides.add(override)
        checkCustomized()
    }

    fun getClasses(date: LocalDate): List<ClassEntity> {
        val dayOfWeek = date.dayOfWeek
        val baseClasses = getClasses(dayOfWeek)
        if (classShiftOverrides.isEmpty()) return baseClasses

        val dateIso = date.toString()
        val activeOverrides = classShiftOverrides.filter { dateIso >= it.effectiveDate }
        if (activeOverrides.isEmpty()) return baseClasses

        val classes = baseClasses.toMutableList()

        // Remove classes shifted away from this dayOfWeek for date >= effectiveDate
        activeOverrides.forEach { override ->
            if (override.originalDayOfWeek.equals(dayOfWeek.name, ignoreCase = true)) {
                classes.removeAll {
                    it.subject.subjectId.equals(override.courseCode, ignoreCase = true) &&
                            it.time.equals(override.originalTime, ignoreCase = true)
                }
            }
        }

        // Add classes shifted into this dayOfWeek for date >= effectiveDate
        activeOverrides.forEach { override ->
            if (override.newDayOfWeek.equals(dayOfWeek.name, ignoreCase = true)) {
                val subjectName = getCourseName(override.courseCode)
                val subject = Subject(
                    displayName = if (subjectName.isNotBlank()) subjectName else override.courseCode,
                    subjectId = override.courseCode,
                    teacher = "",
                    teacherInitials = ""
                )
                val shiftedClass = ClassEntity(
                    classId = override.id.toString(),
                    subject = subject,
                    time = override.newTime,
                    dayOfWeek = dayOfWeek,
                    roomNo = override.newLocation.ifBlank { "N/A" },
                    attendanceStatus = null
                )
                if (classes.none { it.subject.subjectId.equals(override.courseCode, ignoreCase = true) && it.time.equals(override.newTime, ignoreCase = true) }) {
                    classes.add(shiftedClass)
                }
            }
        }

        return classes.sortedBy { parseTimeToMinutes(it.time.substringBefore(" - ")) }
    }

    fun getScheduleEventsForDay(day: String, timetableData: TimetableData? = activeTimetableData): List<ScheduleEvent> {
        val data = timetableData ?: return emptyList()
        val dayName = day.lowercase().replaceFirstChar { it.uppercase() }
        val baseEvents = data.schedule.filter { it.day.equals(dayName, ignoreCase = true) }.toMutableList()

        if (classShiftOverrides.isEmpty()) return baseEvents

        // 1. Remove events shifted AWAY from this day
        classShiftOverrides.forEach { override ->
            val origDay = override.originalDayOfWeek.lowercase().replaceFirstChar { it.uppercase() }
            if (origDay.equals(dayName, ignoreCase = true)) {
                baseEvents.removeAll { ev ->
                    ev.course_code.equals(override.courseCode, ignoreCase = true) &&
                            ev.time.equals(override.originalTime, ignoreCase = true)
                }
            }
        }

        // 2. Add / update events shifted INTO this day
        classShiftOverrides.forEach { override ->
            val newDay = override.newDayOfWeek.lowercase().replaceFirstChar { it.uppercase() }
            if (newDay.equals(dayName, ignoreCase = true)) {
                val times = override.newTime.split("-").map { it.trim() }
                val startT = times.getOrNull(0) ?: "08:00"
                val endT = times.getOrNull(1) ?: "09:00"

                val existingOrig = data.schedule.firstOrNull { ev ->
                    ev.course_code.equals(override.courseCode, ignoreCase = true) &&
                            ev.time.equals(override.originalTime, ignoreCase = true)
                }

                val shiftedEvent = ScheduleEvent(
                    id = existingOrig?.id ?: override.id.toString(),
                    course_code = override.courseCode,
                    day = newDay,
                    time = override.newTime,
                    start_time = startT,
                    end_time = endT,
                    location = override.newLocation.ifBlank { existingOrig?.location ?: "" },
                    faculty_name = existingOrig?.faculty_name,
                    faculty_abbr = existingOrig?.faculty_abbr,
                    type = existingOrig?.type ?: "Lecture"
                )

                if (baseEvents.none { it.course_code.equals(override.courseCode, ignoreCase = true) && it.time.equals(override.newTime, ignoreCase = true) }) {
                    baseEvents.add(shiftedEvent)
                }
            }
        }

        return baseEvents.sortedBy { parseTimeToMinutes(it.time.substringBefore(" - ")) }
    }

    fun getClasses(dayOfWeek: DayOfWeek): List<ClassEntity> {
        val data = activeTimetableData ?: return getDefaultClasses(dayOfWeek)
        val dayName = dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() }
        val dayEvents = data.schedule
            .filter { it.day.equals(dayName, ignoreCase = true) }
            .sortedBy { parseTimeToMinutes(it.time.substringBefore(" - ")) }

        if (dayEvents.isEmpty()) return emptyList()

        return dayEvents.map { event ->
            val courseInfo = data.courses.find { it.code.equals(event.course_code, ignoreCase = true) }
            val displayName = courseInfo?.name?.ifEmpty { null } ?: event.course_code
            val isPractical = event.type.equals("Practical", ignoreCase = true) || event.type.equals("P", ignoreCase = true)

            val finalName = if (isPractical && !displayName.startsWith("(Lab)")) {
                "(Lab) $displayName"
            } else {
                displayName
            }

            val facultyName = event.faculty_name ?: ""
            val facultyAbbr = event.faculty_abbr
                ?: data.faculty.entries.firstOrNull { it.value.equals(facultyName, ignoreCase = true) }?.key
                ?: ""

            val subject = Subject(
                displayName = finalName,
                subjectId = event.course_code,
                teacher = facultyName,
                teacherInitials = facultyAbbr
            )

            ClassEntity(
                subject = subject,
                time = event.time,
                dayOfWeek = dayOfWeek,
                roomNo = event.location ?: "N/A",
                attendanceStatus = null,
                group = event.group
            )
        }
    }

    private fun getDefaultClasses(dayOfWeek: DayOfWeek): List<ClassEntity> {
        return emptyList()
    }

    fun getAllRooms(): List<String> {
        val data = activeTimetableData ?: return emptyList()
        val rooms = mutableSetOf<String>()
        data.schedule.forEach { event ->
            event.location?.trim()?.takeIf { it.isNotEmpty() && !it.equals("N/A", ignoreCase = true) }?.let { rooms.add(it) }
        }
        return rooms.sorted()
    }

    fun getAllFaculties(): List<String> {
        val data = activeTimetableData ?: return emptyList()
        val faculties = mutableSetOf<String>()
        data.schedule.forEach { event ->
            event.faculty_name?.trim()?.takeIf { it.isNotEmpty() }?.let { faculties.add(it) }
        }
        data.faculty.values.forEach { name ->
            name.trim().takeIf { it.isNotEmpty() }?.let { faculties.add(it) }
        }
        data.courses.forEach { course ->
            course.faculty.forEach { name ->
                name.trim().takeIf { it.isNotEmpty() }?.let { faculties.add(it) }
            }
        }
        return faculties
            .filter { name ->
                val trimmed = name.trim()
                !(trimmed.length <= 4 && !trimmed.contains(" ") && !trimmed.contains(".")) &&
                        !trimmed.matches(Regex("^[A-Z]{1,5}$"))
            }
            .sorted()
    }

    fun getAllCourseCodes(): List<String> {
        val data = activeTimetableData ?: return emptyList()
        val set = mutableSetOf<String>()
        data.courses.forEach { set.add(it.code) }
        data.schedule.forEach { set.add(it.course_code) }
        return set.filter { it.isNotBlank() }.sorted()
    }

    fun getAllCourseNames(): List<String> {
        val data = activeTimetableData ?: return emptyList()
        val set = mutableSetOf<String>()
        data.courses.forEach { set.add(it.name) }
        return set.filter { it.isNotBlank() }.sorted()
    }

    fun getAllGroups(): List<String> {
        val data = activeTimetableData ?: return emptyList()
        val set = mutableSetOf<String>()
        data.schedule.forEach { event ->
            event.group?.trim()?.takeIf { it.isNotEmpty() && !it.equals("N/A", ignoreCase = true) }?.let { set.add(it) }
        }
        return set.sorted()
    }
}

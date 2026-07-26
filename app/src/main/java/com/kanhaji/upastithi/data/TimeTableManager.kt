package com.kanhaji.upastithi.data

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.kanhaji.upastithi.features.home.data.Subject
import com.kanhaji.upastithi.features.home.data.TimeTable
import com.kanhaji.upastithi.features.home.domain.model.ClassEntity
import com.kanhaji.upastithi.features.home.domain.model.CourseInfo
import com.kanhaji.upastithi.features.home.domain.model.ScheduleEvent
import com.kanhaji.upastithi.features.home.domain.model.TimetableData
import kotlinx.datetime.DayOfWeek
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

object TimeTableManager {
    var activeTimetableData by mutableStateOf<TimetableData?>(null)
        private set

    var isCustomized by mutableStateOf(false)
        private set

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

    fun setParsedTimetable(data: TimetableData?, context: Context? = null) {
        if (context != null && loadCustomTimetable(context)) {
            return
        }
        activeTimetableData = data
        isCustomized = false
        if (data != null) {
            registerDynamicSubjects(data)
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
        val newSchedule = current.schedule.map { event ->
            if (event.id == originalEvent.id) updatedEvent else event
        }.sortedWith(compareBy({ it.day }, { parseTimeToMinutes(it.time.substringBefore(" - ")) }))

        val updatedCourses = updateCourseInfoList(current.courses, updatedEvent.course_code, updatedCourseName)
        val updatedData = current.copy(
            schedule = newSchedule,
            courses = updatedCourses
        )

        activeTimetableData = updatedData
        isCustomized = true
        registerDynamicSubjects(updatedData)
        saveCustomTimetable(context, updatedData)
    }

    fun addEvent(
        newEvent: ScheduleEvent,
        courseName: String?,
        context: Context
    ) {
        val current = activeTimetableData ?: return
        val newSchedule = (current.schedule + newEvent)
            .sortedWith(compareBy({ it.day }, { parseTimeToMinutes(it.time.substringBefore(" - ")) }))
        val updatedCourses = updateCourseInfoList(current.courses, newEvent.course_code, courseName)

        val updatedData = current.copy(
            schedule = newSchedule,
            courses = updatedCourses
        )

        activeTimetableData = updatedData
        isCustomized = true
        registerDynamicSubjects(updatedData)
        saveCustomTimetable(context, updatedData)
    }

    fun deleteEvent(
        eventToDelete: ScheduleEvent,
        context: Context
    ) {
        val current = activeTimetableData ?: return
        val newSchedule = current.schedule.filter { it.id != eventToDelete.id }
        val updatedData = current.copy(schedule = newSchedule)

        activeTimetableData = updatedData
        isCustomized = true
        registerDynamicSubjects(updatedData)
        saveCustomTimetable(context, updatedData)
    }

    fun resetToOriginalPdf(context: Context, originalPdfData: TimetableData?) {
        val customFile = File(context.filesDir, "custom_edited_timetable.json")
        if (customFile.exists()) {
            customFile.delete()
        }
        isCustomized = false
        activeTimetableData = originalPdfData
        if (originalPdfData != null) {
            registerDynamicSubjects(originalPdfData)
        } else {
            Subject.clearDynamicSubjects()
        }
    }

    fun parseTimeToMinutes(timeStr: String): Int {
        val clean = timeStr.trim().take(5)
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
        val schedule = activeTimetableData?.schedule ?: return null
        val targetStart = parseTimeToMinutes(startTimeStr)
        val targetEnd = parseTimeToMinutes(endTimeStr)

        if (targetStart >= targetEnd) return null

        return schedule.find { event ->
            if (excludeEventId != null && event.id == excludeEventId) {
                false
            } else if (event.day.equals(day, ignoreCase = true)) {
                val evStart = parseTimeToMinutes(event.start_time.ifEmpty { event.time.split("-").getOrNull(0) ?: "" })
                val evEnd = parseTimeToMinutes(event.end_time.ifEmpty { event.time.split("-").getOrNull(1) ?: "" })
                val maxStart = maxOf(targetStart, evStart)
                val minEnd = minOf(targetEnd, evEnd)
                maxStart < minEnd
            } else {
                false
            }
        }
    }

    fun getCourseName(courseCode: String): String {
        val data = activeTimetableData ?: return courseCode
        val courseInfo = data.courses.find { it.code.equals(courseCode, ignoreCase = true) }
        return courseInfo?.name?.ifEmpty { null } ?: courseCode
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

    fun saveCustomTimetable(context: Context, customData: TimetableData? = null) {
        val data = customData ?: activeTimetableData ?: return
        try {
            val file = File(context.filesDir, "custom_edited_timetable.json")
            val jsonStr = json.encodeToString(data)
            file.writeText(jsonStr)
        } catch (e: Exception) {
            Log.e("TimeTableManager", "Failed to save custom timetable: ${e.message}")
        }
    }

    fun loadCustomTimetable(context: Context): Boolean {
        try {
            val file = File(context.filesDir, "custom_edited_timetable.json")
            if (file.exists()) {
                val jsonStr = file.readText()
                val data = json.decodeFromString<TimetableData>(jsonStr)
                activeTimetableData = data
                isCustomized = true
                registerDynamicSubjects(data)
                return true
            }
        } catch (e: Exception) {
            Log.e("TimeTableManager", "Failed to load custom timetable: ${e.message}")
        }
        return false
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
                attendanceStatus = null
            )
        }
    }

    private fun getDefaultClasses(dayOfWeek: DayOfWeek): List<ClassEntity> {
        return emptyList()
    }
}

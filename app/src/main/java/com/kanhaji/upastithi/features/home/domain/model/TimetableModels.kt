package com.kanhaji.upastithi.features.home.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class DetectedTimetable(
    val page_index: Int,
    val name: String
)

@Serializable
data class CourseInfo(
    val code: String,
    val name: String,
    val details: String,
    val faculty: List<String> = emptyList()
)

@Serializable
data class ScheduleEvent(
    val id: String = java.util.UUID.randomUUID().toString(),
    val day: String,
    val time: String,
    val start_time: String,
    val end_time: String,
    val course_code: String,
    val type: String,
    val location: String? = null,
    val group: String? = null,
    val faculty_abbr: String? = null,
    val faculty_name: String? = null
)

@Serializable
data class TimetableData(
    val semester: String,
    val faculty: Map<String, String> = emptyMap(),
    val courses: List<CourseInfo> = emptyList(),
    val schedule: List<ScheduleEvent> = emptyList()
)

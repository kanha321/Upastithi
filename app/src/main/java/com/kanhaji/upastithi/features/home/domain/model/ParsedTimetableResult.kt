package com.kanhaji.upastithi.features.home.domain.model

import kotlinx.datetime.DayOfWeek

data class ParsedClassSlot(
    val dayOfWeek: DayOfWeek,
    val subjectCode: String,
    val subjectName: String,
    val time: String,
    val roomNo: String
)

data class ParsedTimetableResult(
    val sectionName: String,
    val totalClassesCount: Int,
    val classesByDay: Map<DayOfWeek, List<ParsedClassSlot>>
)

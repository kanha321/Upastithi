package com.kanhaji.upastithi.features.home.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "schedule_events",
    foreignKeys = [
        ForeignKey(
            entity = TimetableMetadataEntity::class,
            parentColumns = ["id"],
            childColumns = ["timetableId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["timetableId"]), Index(value = ["dayOfWeek"])]
)
data class ScheduleEventEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val timetableId: String,
    val dayOfWeek: String, // e.g. "MONDAY", "TUESDAY"
    val time: String, // e.g. "09:00 - 10:00"
    val startTime: String,
    val endTime: String,
    val startMinutes: Int, // for instant time sorting
    val endMinutes: Int,
    val courseCode: String,
    val type: String, // "Lecture" or "Practical"
    val location: String? = null,
    val group: String? = null,
    val facultyAbbr: String? = null,
    val facultyName: String? = null
)

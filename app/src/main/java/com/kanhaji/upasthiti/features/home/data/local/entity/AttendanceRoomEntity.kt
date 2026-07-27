package com.kanhaji.upasthiti.features.home.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "attendance_records")
data class AttendanceRoomEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val timetableId: String = "",
    val date: String, // ISO YYYY-MM-DD
    val time: String,
    val subjectId: String,
    val subjectDisplayName: String,
    val teacher: String,
    val teacherInitials: String,
    val status: String? // PRESENT, ABSENT, CANCELLED, NO_CLASS, OTHER, or null
)

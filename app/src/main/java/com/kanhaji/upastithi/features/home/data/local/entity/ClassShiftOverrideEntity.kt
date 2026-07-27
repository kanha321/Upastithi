package com.kanhaji.upastithi.features.home.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "class_shift_overrides")
data class ClassShiftOverrideEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val timetableId: String,
    val courseCode: String,
    val originalDayOfWeek: String,
    val originalTime: String,
    val effectiveDate: String, // ISO Date format "YYYY-MM-DD"
    val newDayOfWeek: String,
    val newTime: String,
    val newLocation: String = ""
)

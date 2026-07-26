package com.kanhaji.upastithi.features.home.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "timetable_metadata")
data class TimetableMetadataEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val pageIndex: Int,
    val semester: String,
    val isCustomized: Boolean = false,
    val isActive: Boolean = true
)

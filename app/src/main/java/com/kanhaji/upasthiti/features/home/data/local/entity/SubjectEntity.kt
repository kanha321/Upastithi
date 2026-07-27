package com.kanhaji.upasthiti.features.home.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "subjects")
data class SubjectEntity(
    @PrimaryKey
    val subjectId: String,
    val displayName: String,
    val teacher: String,
    val teacherInitials: String,
    val timetableId: String = ""
)

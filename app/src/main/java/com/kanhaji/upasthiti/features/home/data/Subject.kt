package com.kanhaji.upasthiti.features.home.data

import kotlinx.serialization.Serializable

@Serializable
data class Subject(
    val displayName: String,
    val subjectId: String,
    val teacher: String,
    val teacherInitials: String
) {
    companion object {
        val predefinedSubjects: List<Subject> = emptyList()

        private val dynamicSubjects = mutableListOf<Subject>()

        fun registerSubjects(subjects: List<Subject>) {
            dynamicSubjects.clear()
            dynamicSubjects.addAll(subjects)
        }

        fun clearDynamicSubjects() {
            dynamicSubjects.clear()
        }

        fun fromString(value: String?): Subject? = getAllSubjects().find { it.displayName.equals(value, ignoreCase = true) }

        fun getAllSubjects(): List<Subject> {
            return if (dynamicSubjects.isNotEmpty()) {
                dynamicSubjects.toList()
            } else {
                predefinedSubjects
            }
        }
    }
}

package com.kanhaji.upastithi.data

import kotlinx.serialization.Serializable

@Serializable
data class Subject(
    val displayName: String,
    val subjectId: String,
    val teacher: String,
    val teacherInitials: String
) {
    companion object {
        // Dynamic registry — populated from parsed PDF timetable
        private val dynamicSubjects = mutableListOf<Subject>()

        // 4th Sem defaults (fallback when no PDF is loaded)
        val DEFAULTS = listOf(
            Subject("Computer Graphics", "CS34101", "Prof. Rajesh Tripathi", "RT"),
            Subject("Software Engineering", "CS34102", "Dr. Anoj Kumar", "AJK"),
            Subject("Computer Network", "CS34103", "Dr. Shashwati Banerjea", "SHB"),
            Subject("Data Mining", "CS34104", "Prof. A. K. Singh", "AKS"),
            Subject("Image Processing", "CS34312", "Dr. Dushyant Kumar Singh", "DUS"),
            Subject("(Lab) Computer Graphics", "CS34201", "Prof. Rajesh Tripathi", "RT"),
            Subject("(Lab) Computer Network", "CS34202", "Dr. Shashwati Banerjea", "SHB"),
        )

        fun registerSubjects(subjects: List<Subject>) {
            dynamicSubjects.clear()
            dynamicSubjects.addAll(subjects)
        }

        fun clearDynamicSubjects() {
            dynamicSubjects.clear()
        }

        fun getAllSubjects(): List<Subject> {
            return if (dynamicSubjects.isNotEmpty()) dynamicSubjects.toList()
            else DEFAULTS
        }

        fun fromString(value: String?): Subject? {
            return getAllSubjects().find { it.displayName == value }
        }

        fun fromSubjectId(id: String?): Subject? {
            return getAllSubjects().find { it.subjectId.equals(id, ignoreCase = true) }
        }
    }

    override fun toString(): String = displayName
}
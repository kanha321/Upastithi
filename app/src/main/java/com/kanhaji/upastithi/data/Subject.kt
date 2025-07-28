package com.kanhaji.upastithi.data

enum class Subject(val displayName: String, val subjectId: String) {
    OPERATING_SYSTEMS(
        displayName = "Operating Systems",
        subjectId = "CS33101"
    ),
    DATABASE_MANAGEMENT_SYSTEMS(
        displayName = "Database Management Systems",
        subjectId = "CS33102"
    ),
    SOFT_COMPUTING(
        displayName = "Soft Computing",
        subjectId = "CS33103"
    ),
    ANALYSIS_OF_ALGORITHMS(
        displayName = "Analysis of Algorithms",
        subjectId = "CS33104"
    ),
    OBJECT_BASED_MODELING(
        displayName = "Object Based Modeling",
        subjectId = "CS33105"
    ),
    OPERATING_SYSTEMS_LAB(
        displayName = "(Lab) Operating Systems",
        subjectId = "CS33201"
    ),
    DATABASE_MANAGEMENT_SYSTEMS_LAB(
        displayName = "(Lab) Database Management Systems",
        subjectId = "CS33202"
    ),
    WEB_PROGRAMMING_LAB(
        displayName = "(Lab) Web Programming",
        subjectId = "CS33204"
    ),
    ANALYSIS_OF_ALGORITHMS_LAB(
        displayName = "(Lab) Analysis of Algorithms",
        subjectId = "CS33203"
    );

    companion object {
        fun fromString(value: String?): Subject? { // Used to convert string to enum for database storage
            return entries.find { it.displayName == value }
        }

        fun getAllSubjects(): List<Subject> {
            return entries.toList()
        }
    }

    override fun toString(): String {
        return displayName
    }
}
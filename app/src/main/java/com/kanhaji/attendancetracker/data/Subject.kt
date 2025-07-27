package com.kanhaji.attendancetracker.data

enum class Subject(val displayName: String, val subjectId: String) {
    OPERATING_SYSTEMS("Operating Systems", "CS33101"),
    DATABASE_MANAGEMENT_SYSTEMS("Database Management Systems", "CS33102"),
    SOFT_COMPUTING("Soft Computing", "CS33103"),
    ANALYSIS_OF_ALGORITHMS("Analysis of Algorithms", "CS33104"),
    OBJECT_BASED_MODELING("Object Based Modeling", "CS33105"),
    OPERATING_SYSTEMS_LAB("(Lab) Operating Systems", "CS33201"),
    DATABASE_MANAGEMENT_SYSTEMS_LAB("(Lab) Database Management Systems", "CS33202"),
    WEB_PROGRAMMING_LAB("(Lab) Web Programming", "CS33204"),
    ANALYSIS_OF_ALGORITHMS_LAB("(Lab) Analysis of Algorithms", "CS33203");

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
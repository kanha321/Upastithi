package com.kanhaji.upastithi.features.home.data

enum class Subject(
    val displayName: String,
    val subjectId: String,
    val teacher: String,
    val teacherInitials: String
) {
    COMPUTER_GRAPHICS(
        displayName = "Computer Graphics",
        subjectId = "CS34101",
        teacher = "Prof. Rajesh Tripathi",
        teacherInitials = "RT"
    ),
    SOFTWARE_ENGINEERING(
        displayName = "Software Engineering",
        subjectId = "CS34102",
        teacher = "Dr. Anoj Kumar",
        teacherInitials = "AJK"
    ),
    COMPUTER_NETWORK(
        displayName = "Computer Network",
        subjectId = "CS34103",
        teacher = "Dr. Shashwati Banerjea",
        teacherInitials = "SHB"
    ),
    DATA_MINING(
        displayName = "Data Mining",
        subjectId = "CS34104",
        teacher = "Prof. A. K. Singh",
        teacherInitials = "AKS"
    ),
    IMAGE_PROCESSING(
        displayName = "Image Processing",
        subjectId = "CS34312",
        teacher = "Dr. Dushyant Kumar Singh",
        teacherInitials = "DUS"
    ),
    COMPUTER_GRAPHICS_LAB(
        displayName = "(Lab) Computer Graphics",
        subjectId = "CS34201",
        teacher = "Prof. Rajesh Tripathi",
        teacherInitials = "RT"
    ),
    COMPUTER_NETWORK_LAB(
        displayName = "(Lab) Computer Network",
        subjectId = "CS34202",
        teacher = "Dr. Shashwati Banerjea",
        teacherInitials = "SHB"
    );

    companion object {
        fun fromString(value: String?): Subject? = entries.find { it.displayName == value }
        fun getAllSubjects(): List<Subject> = entries.toList()
    }
}

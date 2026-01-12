package com.kanhaji.upastithi.data

//enum class Subject(
//    val displayName: String,
//    val subjectId: String,
//    val teacher: String,
//    val teacherInitials: String
//) {
//    OPERATING_SYSTEMS(
//        displayName = "Operating Systems",
//        subjectId = "CS33101",
//        teacher = "Dr. Manoj Wariya",
//        teacherInitials = "MW"
//    ),
//    DATABASE_MANAGEMENT_SYSTEMS(
//        displayName = "Database Management Systems",
//        subjectId = "CS33102",
//        teacher = "Prof. Anil Kumar Singh",
//        teacherInitials = "AKS"
//    ),
//    SOFT_COMPUTING(
//        displayName = "Soft Computing",
//        subjectId = "CS33103",
//        teacher = "Dr. Abhinav Kumar",
//        teacherInitials = "ABK"
//    ),
//    ANALYSIS_OF_ALGORITHMS(
//        displayName = "Analysis of Algorithms",
//        subjectId = "CS33104",
//        teacher = "Dr. Amit Biswas",
//        teacherInitials = "AMB"
//    ),
//    OBJECT_BASED_MODELING(
//        displayName = "Object Based Modeling",
//        subjectId = "CS33105",
//        teacher = "Prof. Dharmendra Kumar Yadav",
//        teacherInitials = "DKY"
//    ),
//    OPERATING_SYSTEMS_LAB(
//        displayName = "(Lab) Operating Systems",
//        subjectId = "CS33201",
//        teacher = "Dr. Manoj Wariya",
//        teacherInitials = "MW"
//    ),
//    DATABASE_MANAGEMENT_SYSTEMS_LAB(
//        displayName = "(Lab) Database Management Systems",
//        subjectId = "CS33202",
//        teacher = "Prof. Anil Kumar Singh",
//        teacherInitials = "AKS"
//    ),
//    WEB_PROGRAMMING_LAB(
//        displayName = "(Lab) Web Programming",
//        subjectId = "CS33204",
//        teacher = "Dr. Pragya Dwivedi",
//        teacherInitials = "PD"
//    ),
//    ANALYSIS_OF_ALGORITHMS_LAB(
//        displayName = "(Lab) Analysis of Algorithms",
//        subjectId = "CS33203",
//        teacher = "Dr. Amit Biswas",
//        teacherInitials = "AMB"
//    );
//
//    companion object {
//        fun fromString(value: String?): Subject? { // Used to convert string to enum for database storage
//            return entries.find { it.displayName == value }
//        }
//
//        fun getAllSubjects(): List<Subject> {
//            return entries.toList()
//        }
//    }
//
//    override fun toString(): String {
//        return displayName
//    }
//}

//enum class Subject(
//    val displayName: String,
//    val subjectId: String,
//    val teacher: String,
//    val teacherInitials: String
//) {
//    PROGRAMMING_AND_PROBLEM_SOLVING(
//        displayName = "Programming and Problem Solving",
//        subjectId = "CS31101",
//        teacher = "Prof. M. M. Gore",
//        teacherInitials = "MMG"
//    ),
//    PRINCIPLES_OF_IT_INDUSTRY_MANAGEMENT(
//        displayName = "Principles of IT Industry Management",
//        subjectId = "CS31102",
//        teacher = "???",
//        teacherInitials = "?"
//    ),
//    DIGITAL_COMPUTER_ORGANIZATION(
//        displayName = "Digital Computer Organization",
//        subjectId = "CS31103",
//        teacher = "Mr. Rajesh Tripathi",
//        teacherInitials = "RT"
//    ),
//    FOUNDATIONS_OF_LOGIC(
//        displayName = "Foundations of Logic",
//        subjectId = "CS31104",
//        teacher = "Guest Faculty",
//        teacherInitials = "GF2"
//    ),
//    PROGRAMMING_AND_PROBLEM_SOLVING_LAB_GROUP_A(
//        displayName = "(Group A) Programming and Problem Solving",
//        subjectId = "CS31201",
//        teacher = "Dr. Kailash W. Kalare",
//        teacherInitials = "KK"
//    ),
//    DIGITAL_COMPUTER_ORGANIZATION_LAB_GROUP_A(
//        displayName = "(Group A) Digital Computer Organization",
//        subjectId = "CS31202",
//        teacher = "Mr. Rajesh Tripathi",
//        teacherInitials = "RT"
//    ),
//    SHELL_PROGRAMMING_LAB_GROUP_A(
//        displayName = "(Group A) Shell Programming",
//        subjectId = "CS31203",
//        teacher = "Dr. Ranvijay",
//        teacherInitials = "RAN"
//    ),
//    PROGRAMMING_AND_PROBLEM_SOLVING_LAB_GROUP_B(
//        displayName = "(Group B) Programming and Problem Solving",
//        subjectId = "CS31301",
//        teacher = "Dr. Kailash W. Kalare",
//        teacherInitials = "KK"
//    ),
//    DIGITAL_COMPUTER_ORGANIZATION_LAB_GROUP_B(
//        displayName = "(Group B) Digital Computer Organization",
//        subjectId = "CS31302",
//        teacher = "Mr. Rajesh Tripathi",
//        teacherInitials = "RT"
//    ),
//    SHELL_PROGRAMMING_LAB_GROUP_B(
//        displayName = "(Group B) Shell Programming",
//        subjectId = "CS31303",
//        teacher = "Dr. Ranvijay",
//        teacherInitials = "RAN"
//    );
//
//    companion object {
//        fun fromString(value: String?): Subject? { // Used to convert string to enum for database storage
//            return entries.find { it.displayName == value }
//        }
//
//        fun getAllSubjects(): List<Subject> {
//            return entries.toList()
//        }
//    }
//
//    override fun toString(): String {
//        return displayName
//    }
//}

//4th Sem Subjects

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
package com.kanhaji.upastithi.data

import com.kanhaji.upastithi.entity.ClassEntity
import java.time.DayOfWeek

//object TimeTable {
//
//    val MONDAY: List<ClassEntity> = listOf(
//        ClassEntity(
//            subject = Subject.OPERATING_SYSTEMS,
//            time = "9:00 - 10:00",
//            dayOfWeek = DayOfWeek.MONDAY,
//            roomNo = "NLH2",
//            attendanceStatus = null
//        ),
//        ClassEntity(
//            subject = Subject.OBJECT_BASED_MODELING,
//            time = "10:00 - 11:00",
//            dayOfWeek = DayOfWeek.MONDAY,
//            roomNo = "NLH2",
//            attendanceStatus = null
//        ),
//        ClassEntity(
//            subject = Subject.ANALYSIS_OF_ALGORITHMS,
//            time = "13:00 - 14:00",
//            dayOfWeek = DayOfWeek.MONDAY,
//            roomNo = "NLH1",
//            attendanceStatus = null
//        ),
//        ClassEntity(
//            subject = Subject.ANALYSIS_OF_ALGORITHMS,
//            time = "14:00 - 15:00",
//            dayOfWeek = DayOfWeek.MONDAY,
//            roomNo = "NLH1",
//            attendanceStatus = null
//        ),
//        ClassEntity(
//            subject = Subject.SOFT_COMPUTING,
//            time = "15:00 - 16:00",
//            dayOfWeek = DayOfWeek.MONDAY,
//            roomNo = "NLH1",
//            attendanceStatus = null
//        ),
//    )
//
//    val TUESDAY: List<ClassEntity> = listOf(
//        ClassEntity(
//            subject = Subject.ANALYSIS_OF_ALGORITHMS_LAB,
//            time = "9:00 - 12:00",
//            dayOfWeek = DayOfWeek.TUESDAY,
//            roomNo = "L1 (CSED)",
//            attendanceStatus = null
//        ),
//        ClassEntity(
//            subject = Subject.OPERATING_SYSTEMS,
//            time = "14:00 - 15:00",
//            dayOfWeek = DayOfWeek.TUESDAY,
//            roomNo = "NLH2",
//            attendanceStatus = null
//        ),
//        ClassEntity(
//            subject = Subject.OPERATING_SYSTEMS,
//            time = "15:00 - 16:00",
//            dayOfWeek = DayOfWeek.TUESDAY,
//            roomNo = "NLH2",
//            attendanceStatus = null
//        ),
//    )
//
//    val WEDNESDAY: List<ClassEntity> = listOf(
//        ClassEntity(
//            subject = Subject.DATABASE_MANAGEMENT_SYSTEMS,
//            time = "9:00 - 10:00",
//            dayOfWeek = DayOfWeek.WEDNESDAY,
//            roomNo = "NLH2",
//            attendanceStatus = null
//        ),
//        ClassEntity(
//            subject = Subject.DATABASE_MANAGEMENT_SYSTEMS,
//            time = "10:00 - 11:00",
//            dayOfWeek = DayOfWeek.WEDNESDAY,
//            roomNo = "NLH2",
//            attendanceStatus = null
//        ),
//        ClassEntity(
//            subject = Subject.ANALYSIS_OF_ALGORITHMS,
//            time = "11:00 - 12:00",
//            dayOfWeek = DayOfWeek.WEDNESDAY,
//            roomNo = "NLH2",
//            attendanceStatus = null
//        ),
//        ClassEntity(
//            subject = Subject.WEB_PROGRAMMING_LAB,
//            time = "14:00 - 17:00",
//            dayOfWeek = DayOfWeek.WEDNESDAY,
//            roomNo = "L2 (CSED)",
//            attendanceStatus = null
//        ),
//    )
//
//    val THURSDAY: List<ClassEntity> = listOf(
//        ClassEntity(
//            subject = Subject.SOFT_COMPUTING,
//            time = "8:00 - 9:00",
//            dayOfWeek = DayOfWeek.THURSDAY,
//            roomNo = "NLH1",
//            attendanceStatus = null
//        ),
//        ClassEntity(
//            subject = Subject.SOFT_COMPUTING,
//            time = "9:00 - 10:00",
//            dayOfWeek = DayOfWeek.THURSDAY,
//            roomNo = "NLH1",
//            attendanceStatus = null
//        ),
//        ClassEntity(
//            subject = Subject.OBJECT_BASED_MODELING,
//            time = "10:00 - 11:00",
//            dayOfWeek = DayOfWeek.THURSDAY,
//            roomNo = "NLH1",
//            attendanceStatus = null
//        ),
//        ClassEntity(
//            subject = Subject.OBJECT_BASED_MODELING,
//            time = "11:00 - 12:00",
//            dayOfWeek = DayOfWeek.THURSDAY,
//            roomNo = "NLH1",
//            attendanceStatus = null
//        ),
//        ClassEntity(
//            subject = Subject.DATABASE_MANAGEMENT_SYSTEMS_LAB,
//            time = "14:00 - 17:00",
//            dayOfWeek = DayOfWeek.THURSDAY,
//            roomNo = "CCTF",
//            attendanceStatus = null
//        ),
//    )
//
//    val FRIDAY: List<ClassEntity> = listOf(
//        ClassEntity(
//            subject = Subject.DATABASE_MANAGEMENT_SYSTEMS,
//            time = "10:00 - 11:00",
//            dayOfWeek = DayOfWeek.FRIDAY,
//            roomNo = "NLH2",
//            attendanceStatus = null
//        ),
//        ClassEntity(
//            subject = Subject.DATABASE_MANAGEMENT_SYSTEMS,
//            time = "11:00 - 12:00",
//            dayOfWeek = DayOfWeek.FRIDAY,
//            roomNo = "NLH2",
//            attendanceStatus = null
//        ),
//        ClassEntity(
//            subject = Subject.OPERATING_SYSTEMS,
//            time = "13:00 - 14:00",
//            dayOfWeek = DayOfWeek.FRIDAY,
//            roomNo = "NLH2",
//            attendanceStatus = null
//        ),
//        ClassEntity(
//            subject = Subject.OPERATING_SYSTEMS_LAB,
//            time = "14:00 - 17:00",
//            dayOfWeek = DayOfWeek.FRIDAY,
//            roomNo = "L2 (CSED)",
//            attendanceStatus = null
//        ),
//    )
//
//    val SATURDAY: List<ClassEntity> = emptyList()
//    val SUNDAY: List<ClassEntity> = emptyList()
//}

//object TimeTable {
//    // lab = 3 hrs
//    val MONDAY: List<ClassEntity> = listOf(
//        ClassEntity(
//            subject = Subject.FOUNDATIONS_OF_LOGIC,
//            time = "8:00 - 9:00",
//            dayOfWeek = DayOfWeek.MONDAY,
//            roomNo = "NLH2",
//            attendanceStatus = null,
//        ),
//        ClassEntity(
//            subject = Subject.SHELL_PROGRAMMING_LAB_GROUP_A,
//            time = "9:00 - 12:00",
//            dayOfWeek = DayOfWeek.MONDAY,
//            roomNo = "L1 (CSED)",
//            attendanceStatus = null,
//        ),
//        ClassEntity(
//            subject = Subject.DIGITAL_COMPUTER_ORGANIZATION,
//            time = "13:00 - 14:00",
//            dayOfWeek = DayOfWeek.MONDAY,
//            roomNo = "NLH2",
//            attendanceStatus = null,
//        ),
//        ClassEntity(
//            subject = Subject.PROGRAMMING_AND_PROBLEM_SOLVING_LAB_GROUP_A,
//            time = "14:00 - 17:00",
//            dayOfWeek = DayOfWeek.MONDAY,
//            roomNo = "L2 (CSED)",
//            attendanceStatus = null,
//        ),
//    )
//
//    val TUESDAY: List<ClassEntity> = listOf(
//        ClassEntity(
//            subject = Subject.PROGRAMMING_AND_PROBLEM_SOLVING,
//            time = "9:00 - 10:00",
//            dayOfWeek = DayOfWeek.TUESDAY,
//            roomNo = "CSNB1",
//            attendanceStatus = null,
//        ),
//        ClassEntity(
//            subject = Subject.PROGRAMMING_AND_PROBLEM_SOLVING,
//            time = "10:00 - 11:00",
//            dayOfWeek = DayOfWeek.TUESDAY,
//            roomNo = "CSNB1",
//            attendanceStatus = null,
//        ),
//        ClassEntity(
//            subject = Subject.PRINCIPLES_OF_IT_INDUSTRY_MANAGEMENT,
//            time = "13:00 - 14:00",
//            dayOfWeek = DayOfWeek.TUESDAY,
//            roomNo = "L1 (CSED)",
//            attendanceStatus = null,
//        ),
//        ClassEntity(
//            subject = Subject.DIGITAL_COMPUTER_ORGANIZATION_LAB_GROUP_A,
//            time = "14:00 - 17:00",
//            dayOfWeek = DayOfWeek.TUESDAY,
//            roomNo = "L2 (CSED)",
//            attendanceStatus = null,
//        ),
//        ClassEntity(
//            subject = Subject.FOUNDATIONS_OF_LOGIC,
//            time = "17:00 - 18:00",
//            dayOfWeek = DayOfWeek.TUESDAY,
//            roomNo = "CSNB1",
//            attendanceStatus = null,
//        )
//    )
//
//    val WEDNESDAY: List<ClassEntity> = listOf(
//        ClassEntity(
//            subject = Subject.PROGRAMMING_AND_PROBLEM_SOLVING,
//            time = "9:00 - 10:00",
//            dayOfWeek = DayOfWeek.WEDNESDAY,
//            roomNo = "CSNB1",
//            attendanceStatus = null,
//        ),
//        ClassEntity(
//            subject = Subject.PROGRAMMING_AND_PROBLEM_SOLVING,
//            time = "10:00 - 11:00",
//            dayOfWeek = DayOfWeek.WEDNESDAY,
//            roomNo = "CSNB1",
//            attendanceStatus = null,
//        ),
////        ClassEntity(
////            subject = Subject.FOUNDATIONS_OF_LOGIC,
////            time = "13:00 - 14:00",
////            dayOfWeek = DayOfWeek.WEDNESDAY,
////            roomNo = "NLH1",
////            attendanceStatus = null,
////        ),
//        ClassEntity(
//            subject = Subject.FOUNDATIONS_OF_LOGIC,
//            time = "14:00 - 15:00",
//            dayOfWeek = DayOfWeek.WEDNESDAY,
//            roomNo = "NLH2",
//            attendanceStatus = null,
//        ),
//        ClassEntity(
//            subject = Subject.SHELL_PROGRAMMING_LAB_GROUP_B,
//            time = "15:00 - 18:00",
//            dayOfWeek = DayOfWeek.WEDNESDAY,
//            roomNo = "CCTF",
//            attendanceStatus = null,
//        ),
//    )
//
//    val THURSDAY: List<ClassEntity> = listOf(
//        ClassEntity(
//            subject = Subject.PROGRAMMING_AND_PROBLEM_SOLVING_LAB_GROUP_B,
//            time = "8:00 - 11:00",
//            dayOfWeek = DayOfWeek.THURSDAY,
//            roomNo = "L1 (CSED)",
//            attendanceStatus = null,
//        ),
//        ClassEntity(
//            subject = Subject.DIGITAL_COMPUTER_ORGANIZATION,
//            time = "11:00 - 12:00",
//            dayOfWeek = DayOfWeek.THURSDAY,
//            roomNo = "NLH2",
//            attendanceStatus = null,
//        ),
//        ClassEntity(
//            subject = Subject.DIGITAL_COMPUTER_ORGANIZATION_LAB_GROUP_B,
//            time = "14:00 - 17:00",
//            dayOfWeek = DayOfWeek.THURSDAY,
//            roomNo = "L2 (CSED)",
//            attendanceStatus = null,
//        ),
//    )
//
//    val FRIDAY: List<ClassEntity> = listOf(
//        ClassEntity(
//            subject = Subject.PRINCIPLES_OF_IT_INDUSTRY_MANAGEMENT,
//            time = "8:00 - 9:00",
//            dayOfWeek = DayOfWeek.FRIDAY,
//            roomNo = "NLH2",
//            attendanceStatus = null,
//        ),
//        ClassEntity(
//            subject = Subject.FOUNDATIONS_OF_LOGIC,
//            time = "9:00 - 10:00",
//            dayOfWeek = DayOfWeek.FRIDAY,
//            roomNo = "NLH2",
//            attendanceStatus = null,
//        ),
//        ClassEntity(
//            subject = Subject.DIGITAL_COMPUTER_ORGANIZATION,
//            time = "15:00 - 16:00",
//            dayOfWeek = DayOfWeek.FRIDAY,
//            roomNo = "NLH2",
//            attendanceStatus = null,
//        ),
//        ClassEntity(
//            subject = Subject.DIGITAL_COMPUTER_ORGANIZATION,
//            time = "16:00 - 17:00",
//            dayOfWeek = DayOfWeek.FRIDAY,
//            roomNo = "NLH2",
//            attendanceStatus = null,
//        ),
//    )
//
//    val SATURDAY: List<ClassEntity> = emptyList()
//    val SUNDAY: List<ClassEntity> = emptyList()
//}

// 4th Sem TimeTable
object TimeTable {

    private val CG = Subject("Computer Graphics", "CS34101", "Prof. Rajesh Tripathi", "RT")
    private val SE = Subject("Software Engineering", "CS34102", "Dr. Anoj Kumar", "AJK")
    private val CN = Subject("Computer Network", "CS34103", "Dr. Shashwati Banerjea", "SHB")
    private val DM = Subject("Data Mining", "CS34104", "Prof. A. K. Singh", "AKS")
    private val IP = Subject("Image Processing", "CS34312", "Dr. Dushyant Kumar Singh", "DUS")
    private val CG_LAB = Subject("(Lab) Computer Graphics", "CS34201", "Prof. Rajesh Tripathi", "RT")
    private val CN_LAB = Subject("(Lab) Computer Network", "CS34202", "Dr. Shashwati Banerjea", "SHB")

    val MONDAY: List<ClassEntity> = listOf(
        ClassEntity(
            subject = CG_LAB,
            time = "09:00 - 12:00",
            dayOfWeek = DayOfWeek.MONDAY,
            roomNo = "L2 Lab",
            attendanceStatus = null
        ),
        ClassEntity(
            subject = CN,
            time = "12:00 - 13:00",
            dayOfWeek = DayOfWeek.MONDAY,
            roomNo = "GS3",
            attendanceStatus = null
        ),
        ClassEntity(
            subject = CG,
            time = "14:00 - 15:00",
            dayOfWeek = DayOfWeek.MONDAY,
            roomNo = "NLH1",
            attendanceStatus = null
        ),
        ClassEntity(
            subject = IP,
            time = "15:00 - 16:00",
            dayOfWeek = DayOfWeek.MONDAY,
            roomNo = "NLH1",
            attendanceStatus = null
        ),
        ClassEntity(
            subject = IP,
            time = "16:00 - 17:00",
            dayOfWeek = DayOfWeek.MONDAY,
            roomNo = "NLH1",
            attendanceStatus = null
        ),
    )

    val TUESDAY: List<ClassEntity> = listOf(
        ClassEntity(
            subject = IP,
            time = "12:00 - 13:00",
            dayOfWeek = DayOfWeek.TUESDAY,
            roomNo = "GS8",
            attendanceStatus = null
        ),
        ClassEntity(
            subject = SE,
            time = "14:00 - 15:00",
            dayOfWeek = DayOfWeek.TUESDAY,
            roomNo = "NLH2",
            attendanceStatus = null
        ),
        ClassEntity(
            subject = SE,
            time = "15:00 - 16:00",
            dayOfWeek = DayOfWeek.TUESDAY,
            roomNo = "NLH2",
            attendanceStatus = null
        ),
        ClassEntity(
            subject = CG,
            time = "17:00 - 18:00",
            dayOfWeek = DayOfWeek.TUESDAY,
            roomNo = "NLH2",
            attendanceStatus = null
        ),
    )

    val WEDNESDAY: List<ClassEntity> = listOf(
        ClassEntity(
            subject = CN_LAB,
            time = "10:00 - 13:00",
            dayOfWeek = DayOfWeek.WEDNESDAY,
            roomNo = "L3 Lab",
            attendanceStatus = null
        ),
        ClassEntity(
            subject = DM,
            time = "15:00 - 16:00",
            dayOfWeek = DayOfWeek.WEDNESDAY,
            roomNo = "NLH1",
            attendanceStatus = null
        ),
        ClassEntity(
            subject = DM,
            time = "16:00 - 17:00",
            dayOfWeek = DayOfWeek.WEDNESDAY,
            roomNo = "NLH1",
            attendanceStatus = null
        ),
    )

    val THURSDAY: List<ClassEntity> = listOf(
        ClassEntity(
            subject = CN,
            time = "14:00 - 15:00",
            dayOfWeek = DayOfWeek.THURSDAY,
            roomNo = "NLH1",
            attendanceStatus = null
        ),
        ClassEntity(
            subject = DM,
            time = "15:00 - 16:00",
            dayOfWeek = DayOfWeek.THURSDAY,
            roomNo = "NLH1",
            attendanceStatus = null
        ),
        ClassEntity(
            subject = CG,
            time = "16:00 - 17:00",
            dayOfWeek = DayOfWeek.THURSDAY,
            roomNo = "NLH1",
            attendanceStatus = null
        ),
        ClassEntity(
            subject = CG,
            time = "17:00 - 18:00",
            dayOfWeek = DayOfWeek.THURSDAY,
            roomNo = "NLH1",
            attendanceStatus = null
        ),
    )

    val FRIDAY: List<ClassEntity> = listOf(
        ClassEntity(
            subject = SE,
            time = "09:00 - 10:00",
            dayOfWeek = DayOfWeek.FRIDAY,
            roomNo = "GS8",
            attendanceStatus = null
        ),
        ClassEntity(
            subject = CN,
            time = "11:00 - 12:00",
            dayOfWeek = DayOfWeek.FRIDAY,
            roomNo = "NLH1",
            attendanceStatus = null
        ),
        ClassEntity(
            subject = CN,
            time = "12:00 - 13:00",
            dayOfWeek = DayOfWeek.FRIDAY,
            roomNo = "NLH1",
            attendanceStatus = null
        ),
    )

    val SATURDAY: List<ClassEntity> = emptyList()
    val SUNDAY: List<ClassEntity> = emptyList()
}
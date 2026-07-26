package com.kanhaji.upastithi.features.home.data

import android.content.Context
import com.kanhaji.upastithi.AndroidContext
import com.kanhaji.upastithi.features.home.domain.model.AttendanceEntity
import com.kanhaji.upastithi.features.home.domain.model.AttendanceEntitySerialized
import com.kanhaji.upastithi.features.home.domain.model.toEntity
import com.kanhaji.upastithi.features.home.domain.model.toSerialized
import com.kanhaji.upastithi.data.TimeTableManager
import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.Json
import java.io.File

object AttendanceStorage {

    private const val FILE_NAME = "attendance_data.json"
    private val json = Json { prettyPrint = true }

    fun initializeAttendanceFile(context: Context) {
        val file = File(context.filesDir, FILE_NAME)
        if (!file.exists()) {
            file.writeText("[]")
        }
    }

    fun addAttendance(context: Context, attendance: AttendanceEntity) {
        initializeAttendanceFile(context)
        val currentList = loadAttendanceList(context).toMutableList()
        val activeId = TimeTableManager.getTimetableId()
        val targetTimetableId = attendance.timetableId.ifEmpty { activeId }
        val finalAttendance = if (attendance.timetableId.isEmpty()) attendance.copy(timetableId = activeId) else attendance

        val existingIndex = currentList.indexOfFirst {
            (it.timetableId == targetTimetableId || (it.timetableId.isEmpty() && targetTimetableId == "default")) &&
            it.date == attendance.date &&
            it.subject.subjectId == attendance.subject.subjectId &&
            it.time == attendance.time
        }

        if (attendance.attendanceStatus == null) {
            if (existingIndex != -1) {
                currentList.removeAt(existingIndex)
                saveAttendanceList(context, currentList)
            }
            return
        }

        if (existingIndex == -1) {
            currentList.add(finalAttendance)
        } else {
            currentList[existingIndex] = finalAttendance
        }
        saveAttendanceList(context, currentList)
    }

    private fun saveAttendanceList(context: Context, attendanceList: List<AttendanceEntity>) {
        val serializedList = attendanceList.map { it.toSerialized() }
        val jsonString = json.encodeToString(serializedList)
        writeToFile(context, jsonString)
    }

    fun loadAttendanceList(context: Context): List<AttendanceEntity> {
        val file = File(context.filesDir, FILE_NAME)
        if (!file.exists()) return emptyList()

        return try {
            val jsonString = file.readText()
            val serializedList = json.decodeFromString<List<AttendanceEntitySerialized>>(jsonString)
            serializedList.map { it.toEntity() }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    fun deleteAttendanceData(context: Context) {
        val file = File(context.filesDir, FILE_NAME)
        if (file.exists()) file.delete()
    }

    private fun writeToFile(context: Context, data: String) {
        val file = File(context.filesDir, FILE_NAME)
        file.writeText(data)
    }

    fun getAttendanceForDate(
        date: LocalDate,
        context: Context = AndroidContext.appContext
    ): List<AttendanceEntity> {
        val currentTimetableId = TimeTableManager.getTimetableId()
        val allAttendance = loadAttendanceList(context)
        return allAttendance.filter {
            it.date == date && (it.timetableId == currentTimetableId || (it.timetableId.isEmpty() && currentTimetableId == "default"))
        }
    }

    fun getAttendanceForTime(date: LocalDate, time: String): AttendanceEntity? {
        val attendancesForDay = getAttendanceForDate(date)
        return attendancesForDay.find { it.time == time }
    }

    fun getAttendancesForSubject(
        subject: Subject,
        context: Context = AndroidContext.appContext
    ): List<AttendanceEntity> {
        val currentTimetableId = TimeTableManager.getTimetableId()
        val allAttendance = loadAttendanceList(context)
        return allAttendance.filter {
            it.subject.subjectId == subject.subjectId &&
            (it.timetableId == currentTimetableId || (it.timetableId.isEmpty() && currentTimetableId == "default"))
        }
    }

    fun getAttendanceGroupedByDate(context: Context): Map<LocalDate, List<AttendanceEntity>> {
        val currentTimetableId = TimeTableManager.getTimetableId()
        return loadAttendanceList(context)
            .filter { it.timetableId == currentTimetableId || (it.timetableId.isEmpty() && currentTimetableId == "default") }
            .groupBy { it.date }
    }
}

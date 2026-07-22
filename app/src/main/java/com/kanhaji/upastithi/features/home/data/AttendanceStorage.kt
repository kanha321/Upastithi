package com.kanhaji.upastithi.features.home.data

import android.content.Context
import com.kanhaji.upastithi.AndroidContext
import com.kanhaji.upastithi.features.home.domain.model.AttendanceEntity
import com.kanhaji.upastithi.features.home.domain.model.AttendanceEntitySerialized
import com.kanhaji.upastithi.features.home.domain.model.toEntity
import com.kanhaji.upastithi.features.home.domain.model.toSerialized
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
        val existingIndex =
            currentList.indexOfFirst { it.date == attendance.date && it.time == attendance.time }

        if (attendance.attendanceStatus == null) {
            if (existingIndex != -1) {
                currentList.removeAt(existingIndex)
                saveAttendanceList(context, currentList)
            }
            return
        }

        if (existingIndex == -1) {
            currentList.add(attendance)
        } else {
            currentList[existingIndex] = attendance
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
        val allAttendance = loadAttendanceList(context)
        return allAttendance.filter { it.date == date }
    }

    fun getAttendanceForTime(date: LocalDate, time: String): AttendanceEntity? {
        val attendancesForDay = getAttendanceForDate(date)
        return attendancesForDay.find { it.time == time }
    }

    fun getAttendancesForSubject(
        subject: Subject,
        context: Context = AndroidContext.appContext
    ): List<AttendanceEntity> {
        val allAttendance = loadAttendanceList(context)
        return allAttendance.filter { it.subject == subject }
    }

    fun getAttendanceGroupedByDate(context: Context): Map<LocalDate, List<AttendanceEntity>> {
        return loadAttendanceList(context).groupBy { it.date }
    }
}

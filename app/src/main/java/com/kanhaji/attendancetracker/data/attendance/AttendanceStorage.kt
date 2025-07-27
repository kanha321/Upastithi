package com.kanhaji.attendancetracker.data.attendance

import android.content.Context
import com.kanhaji.attendancetracker.AndroidContext
import com.kanhaji.attendancetracker.data.Subject
import com.kanhaji.attendancetracker.entity.AttendanceEntity
import com.kanhaji.attendancetracker.entity.AttendanceEntitySerialized
import com.kanhaji.attendancetracker.entity.toEntity
import com.kanhaji.attendancetracker.entity.toSerialized
import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.Json
import java.io.File

object AttendanceStorage {

    private const val FILE_NAME = "attendance_data.json"
    private val json = Json { prettyPrint = true }

    /**
     * Initializes the attendance data file with an empty array if it does not exist.
     */
    fun initializeAttendanceFile(context: Context) {
        val file = File(context.filesDir, FILE_NAME)
        if (!file.exists()) {
            file.writeText("[]")
        }
    }

    /**
     * Adds a single AttendanceEntity to the existing stored list.
     */
    fun addAttendance(context: Context, attendance: AttendanceEntity) {
        initializeAttendanceFile(context)
        val currentList = loadAttendanceList(context).toMutableList()
        val existingIndex =
            currentList.indexOfFirst { it.date == attendance.date && it.time == attendance.time }

        if (attendance.attendanceStatus == null) {
            // Delete if attendanceStatus is null
            if (existingIndex != -1) {
                currentList.removeAt(existingIndex)
                saveAttendanceList(context, currentList)
            }
            return
        }

        if (existingIndex == -1) {
            // Add new if not found
            currentList.add(attendance)
        } else {
            // Update existing
            currentList[existingIndex] = attendance
        }
        saveAttendanceList(context, currentList)
    }

    /**
     * Saves the given list of AttendanceEntity to internal storage as JSON.
     * This overwrites existing data.
     */
    private fun saveAttendanceList(context: Context, attendanceList: List<AttendanceEntity>) {
        val serializedList = attendanceList.map { it.toSerialized() }
        val jsonString = json.encodeToString(serializedList)
        writeToFile(context, jsonString)
    }

    /**
     * Reads the attendance list from internal storage.
     * Returns an empty list if the file does not exist or on error.
     */
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

    /**
     * Deletes the stored attendance data file.
     */
    fun deleteAttendanceData(context: Context) {
        val file = File(context.filesDir, FILE_NAME)
        if (file.exists()) file.delete()
    }

    private fun writeToFile(context: Context, data: String) {
        val file = File(context.filesDir, FILE_NAME)
        file.writeText(data)
    }

    /**
     * Gets list of attendance data for a given date.
     */
    fun getAttendanceForDate(
        date: LocalDate,
        context: Context = AndroidContext.appContext
    ): List<AttendanceEntity> {
        val allAttendance = loadAttendanceList(context)
        return allAttendance.filter { it.date == date }
    }

    /**
     * Gets attendance data for a specific time from the provided list.
     * Returns null if no attendance record is found for the given time.
     */
//    fun getAttendanceForTime(
//        attendancesForDay: List<AttendanceEntity>,
//        time: String
//    ): AttendanceEntity? {
//        return attendancesForDay.find { it.time == time }
//    }
    fun getAttendanceForTime(date: LocalDate, time: String): AttendanceEntity? {
        val attendancesForDay = getAttendanceForDate(date)
        return attendancesForDay.find { it.time == time }
    }

    /**
     * Deletes an attendance record by its ID.
     * Returns true if the record was found and deleted, false otherwise.
     */
    fun deleteAttendanceById(context: Context, attendanceId: String): Boolean {
        initializeAttendanceFile(context)
        val currentList = loadAttendanceList(context).toMutableList()
        val initialSize = currentList.size

        currentList.removeAll { it.attendanceId.toString() == attendanceId }

        if (currentList.size < initialSize) {
            saveAttendanceList(context, currentList)
            return true
        }
        return false
    }

    /**
     * Deletes an attendance record by date and time.
     * Returns true if the record was found and deleted, false otherwise.
     */
    fun deleteAttendanceByTimeAndDate(context: Context, date: LocalDate, time: String): Boolean {
        initializeAttendanceFile(context)
        val currentList = loadAttendanceList(context).toMutableList()
        val initialSize = currentList.size

        currentList.removeAll { it.date == date && it.time == time }

        if (currentList.size < initialSize) {
            saveAttendanceList(context, currentList)
            return true
        }
        return false
    }

    /**
     * Get all attendance records for a specific subject as list
     */
    fun getAttendancesForSubject(
        subject: Subject,
        context: Context = AndroidContext.appContext
    ): List<AttendanceEntity> {
        val allAttendance = loadAttendanceList(context)
        val attendances = allAttendance.filter { it.subject == subject }
        println("getAttendancesForSubject screenModel: $attendances")
        return attendances
    }
}
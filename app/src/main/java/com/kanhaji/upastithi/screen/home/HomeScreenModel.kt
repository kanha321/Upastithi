package com.kanhaji.upastithi.screen.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.kanhaji.basics.entity.Update
import com.kanhaji.basics.networking.httpClient
import com.kanhaji.basics.util.Updater.update
import com.kanhaji.upastithi.AndroidContext
import com.kanhaji.upastithi.data.Subject
import com.kanhaji.upastithi.data.attendance.AttendanceStatus
import com.kanhaji.upastithi.data.attendance.AttendanceStorage
import com.kanhaji.upastithi.entity.AttendanceEntity
import com.kanhaji.upastithi.entity.ClassEntity
import com.kanhaji.upastithi.util.Course
import com.kanhaji.upastithi.util.UpasthitiUtils
import com.kanhaji.upastithi.util.roundTo
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate

/*

data class ClassEntity(
    val classId: Int = 0,
    val time: String,
    val subject: Subject,
    val attendanceStatus: AttendanceStatus?,
    val startTime: Int = time.split(" - ")[0].split(":")[0].toInt(10), // Extracting start time as an Int
)

data class AttendanceEntity (
    val date: LocalDate,
    val subject: Subject,
    val attendanceStatus: AttendanceStatus,
)

 */

class HomeScreenModel : ScreenModel {
    // Make this reactive
    val attendanceByDate = mutableStateMapOf<LocalDate, List<AttendanceEntity>>()

    var isUpdateAvailable by mutableStateOf(false)
    var showUpdateIcon by mutableStateOf(false)


    init {
        // Load initial data
        val initial = AttendanceStorage.getAttendanceGroupedByDate(AndroidContext.appContext)
        attendanceByDate.putAll(initial)
    }

    fun saveAttendance(
        classEntity: ClassEntity,
        attendanceStatus: AttendanceStatus?,
        date: LocalDate
    ) {
        val attendanceEntity = AttendanceEntity(
            date = date,
            time = classEntity.time,
            subject = classEntity.subject,
            attendanceStatus = attendanceStatus
        )

        // Update JSON file
        AttendanceStorage.addAttendance(AndroidContext.appContext, attendanceEntity)

        // Update state map after modification
        val updatedList = AttendanceStorage.getAttendanceForDate(date)
        attendanceByDate[date] = updatedList
    }

    fun getAttendanceForDate(date: LocalDate): List<AttendanceEntity> {
        return attendanceByDate[date] ?: emptyList()
    }

    fun getAttendanceForClass(date: LocalDate, time: String): AttendanceEntity? {
        return getAttendanceForDate(date).firstOrNull { it.time == time }
    }

    fun getAttendancesForSubject(subject: Subject): Pair<String, Double> {
        val attendances = AttendanceStorage.getAttendancesForSubject(subject)
        val totalClasses = attendances.count {
            it.attendanceStatus != AttendanceStatus.LEAVE &&
                    it.attendanceStatus != AttendanceStatus.HOLIDAY &&
                    it.attendanceStatus != AttendanceStatus.CANCELLED
        }
        val attendedClasses = attendances.count {
            it.attendanceStatus == AttendanceStatus.PRESENT ||
                    it.attendanceStatus == AttendanceStatus.PROXY
        }
        val percentage = if (totalClasses > 0) {
            (attendedClasses.toDouble() / totalClasses * 100).roundTo(2)
        } else {
            0.0
        }
        return Pair("$attendedClasses/$totalClasses", percentage)
    }

    fun getAttendanceColor(
        classEntity: ClassEntity,
        attendancesForDate: List<AttendanceEntity>,
        defaultColor: Color
    ): Color {
        return attendancesForDate.firstOrNull { it.time == classEntity.time }
            ?.attendanceStatus?.color ?: defaultColor
    }

    fun getAttendanceDotsForDate(
        date: LocalDate,
        allAttendances: List<AttendanceEntity>
    ): List<Color> {
        fun parseStartMinutes(timeRange: String): Int {
            val start = timeRange.substringBefore(" - ")
            val parts = start.split(":")
            val h = parts.getOrNull(0)?.toIntOrNull() ?: 0
            val m = parts.getOrNull(1)?.toIntOrNull() ?: 0
            return h * 60 + m
        }

        return allAttendances
            .filter { it.date == date && it.attendanceStatus != null }
            .sortedBy { parseStartMinutes(it.time) }   // Ensures 9 AM before 10 AM
            .map { it.attendanceStatus!!.color }
    }


    suspend fun getLatestVersion(): Update {
        return try {
            val response = httpClient.get(UpasthitiUtils.BASE_URL + UpasthitiUtils.UPDATE_ENDPOINT)
            response.body()
        } catch (e: java.net.UnknownHostException) {
            println("Update: No internet connection.")
            Update(
                latestVersionCode = -1,
                latestVersionName = "No Internet",
                downloadMCA1 = "No Internet",
                downloadMCA3 = "No Internet",
                changelog = "No Internet"
            )
        } catch (e: java.net.ConnectException) {
            println("Update: Unable to connect to the server.")
            Update(
                latestVersionCode = -1,
                latestVersionName = "Server Unreachable",
                downloadMCA1 = "Server Unreachable",
                downloadMCA3 = "Server Unreachable",
                changelog = "Server Unreachable"
            )
        } catch (e: java.net.SocketTimeoutException) {
            println("Update: Connection timed out.")
            Update(
                latestVersionCode = -1,
                latestVersionName = "Timeout",
                downloadMCA1 = "Timeout",
                downloadMCA3 = "Timeout",
                changelog = "Timeout"
            )
        } catch (e: Exception) {
            println("Update: Error $e")
            e.printStackTrace()
            Update(
                latestVersionCode = -1,
                latestVersionName = "Error",
                downloadMCA1 = "Error",
                downloadMCA3 = "Error",
                changelog = "Error"
            )
        }
    }

    fun getUpdateInfo() {
        screenModelScope.launch {
            println("Update: Checking for updates...")
            update = getLatestVersion()
            UpasthitiUtils.updateChecked = true
            println("Update: app version code: ${UpasthitiUtils.appVersionCode}, latest version code: ${update!!.latestVersionCode}")
            if (update!!.latestVersionCode <= UpasthitiUtils.appVersionCode) {
                println("Update: App is up to date.")
            } else {
                isUpdateAvailable = true
                println("Update: New version available: ${update!!.latestVersionName}")
                println("Update: isUpdateAvailable = $isUpdateAvailable")
            }
        }
    }

    suspend fun startDownload(course: Course) {
        if (update == null) {
            println("Update: No update info available.")
            return
        }
        val downloadUrl = when (course) {
            Course.MCA1 -> update!!.downloadMCA1
            Course.MCA3 -> update!!.downloadMCA3
        }
    }


}

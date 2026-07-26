package com.kanhaji.upastithi.features.home.ui

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
import com.kanhaji.upastithi.features.home.data.AttendanceStatus
import com.kanhaji.upastithi.features.home.data.AttendanceStorage
import com.kanhaji.upastithi.features.home.data.Subject
import com.kanhaji.upastithi.features.home.domain.model.AttendanceEntity
import com.kanhaji.upastithi.features.home.domain.model.ClassEntity
import com.kanhaji.upastithi.features.home.domain.usecase.GetClassesForDateUseCase
import com.kanhaji.upastithi.features.home.domain.usecase.GetSubjectAttendanceStatsUseCase
import com.kanhaji.upastithi.features.home.domain.usecase.SaveAttendanceUseCase
import com.kanhaji.upastithi.util.UpasthitiUtils
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate

class HomeScreenModel(
    val getClassesForDateUseCase: GetClassesForDateUseCase = GetClassesForDateUseCase(),
    val saveAttendanceUseCase: SaveAttendanceUseCase = SaveAttendanceUseCase(),
    val getSubjectAttendanceStatsUseCase: GetSubjectAttendanceStatsUseCase = GetSubjectAttendanceStatsUseCase()
) : ScreenModel {

    val attendanceByDate = mutableStateMapOf<LocalDate, List<AttendanceEntity>>()
    var isUpdateAvailable by mutableStateOf(false)

    init {
        refreshAttendance()
    }

    fun refreshAttendance() {
        attendanceByDate.clear()
        val initial = AttendanceStorage.getAttendanceGroupedByDate(AndroidContext.appContext)
        attendanceByDate.putAll(initial)
    }

    fun saveAttendance(
        classEntity: ClassEntity,
        attendanceStatus: AttendanceStatus?,
        date: LocalDate
    ) {
        saveAttendanceUseCase(classEntity = classEntity, attendanceStatus = attendanceStatus, date = date)
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
        return getSubjectAttendanceStatsUseCase(subject)
    }

    fun getAttendanceColor(
        classEntity: ClassEntity,
        attendancesForDate: List<AttendanceEntity>,
        defaultColor: Color
    ): Color {
        return attendancesForDate.firstOrNull { it.time == classEntity.time }
            ?.attendanceStatus?.color ?: defaultColor
    }

    suspend fun getLatestVersion(): Update {
        return try {
            val response = httpClient.get(UpasthitiUtils.BASE_URL + UpasthitiUtils.UPDATE_ENDPOINT)
            response.body()
        } catch (e: Exception) {
            Update(
                latestVersionCode = -1,
                latestVersionName = "Offline",
                downloadMCA1 = "",
                downloadMCA3 = "",
                changelog = ""
            )
        }
    }

    fun getUpdateInfo() {
        screenModelScope.launch {
            update = getLatestVersion()
            UpasthitiUtils.updateChecked = true
            if (update != null && update!!.latestVersionCode > UpasthitiUtils.appVersionCode) {
                isUpdateAvailable = true
            }
        }
    }
}

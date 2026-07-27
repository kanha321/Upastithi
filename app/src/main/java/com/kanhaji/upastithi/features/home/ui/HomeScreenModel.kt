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
import com.kanhaji.upastithi.features.home.data.local.entity.ClassShiftOverrideEntity
import com.kanhaji.upastithi.features.home.data.repository.TimetableRepositoryImpl
import com.kanhaji.upastithi.features.home.domain.repository.TimetableRepository
import kotlinx.coroutines.launch
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate

class HomeScreenModel(
    val getClassesForDateUseCase: GetClassesForDateUseCase = GetClassesForDateUseCase(),
    val saveAttendanceUseCase: SaveAttendanceUseCase = SaveAttendanceUseCase(),
    val getSubjectAttendanceStatsUseCase: GetSubjectAttendanceStatsUseCase = GetSubjectAttendanceStatsUseCase()
) : ScreenModel {

    val attendanceByDate = mutableStateMapOf<LocalDate, List<AttendanceEntity>>()
    var isUpdateAvailable by mutableStateOf(false)

    init {
        screenModelScope.launch {
            val repository = TimetableRepositoryImpl()
            val overrides = repository.getClassShiftOverridesDirect()
            com.kanhaji.upastithi.data.TimeTableManager.setClassShiftOverrides(overrides)
        }
        screenModelScope.launch {
            AttendanceStorage.getAttendanceFlow().collect { map ->
                attendanceByDate.clear()
                attendanceByDate.putAll(map)
            }
        }
    }

    fun refreshAttendance() {
        screenModelScope.launch {
            val initial = AttendanceStorage.getAttendanceGroupedByDate(AndroidContext.appContext)
            attendanceByDate.clear()
            attendanceByDate.putAll(initial)
        }
    }

    fun saveAttendance(
        classEntity: ClassEntity,
        attendanceStatus: AttendanceStatus?,
        date: LocalDate
    ) {
        screenModelScope.launch {
            saveAttendanceUseCase(classEntity = classEntity, attendanceStatus = attendanceStatus, date = date)
            val updatedList = AttendanceStorage.getAttendanceForDate(date)
            attendanceByDate[date] = updatedList
        }
    }

    fun shiftClass(
        classEntity: ClassEntity,
        newDayOfWeek: DayOfWeek,
        newTime: String,
        newLocation: String,
        effectiveDate: LocalDate,
        timetableRepository: TimetableRepository = TimetableRepositoryImpl()
    ) {
        screenModelScope.launch {
            val activeMeta = timetableRepository.getActiveTimetableDirect()
            val timetableId = activeMeta?.id ?: com.kanhaji.upastithi.data.TimeTableManager.getTimetableId()
            val override = ClassShiftOverrideEntity(
                timetableId = timetableId,
                courseCode = classEntity.subject.subjectId,
                originalDayOfWeek = classEntity.dayOfWeek.name,
                originalTime = classEntity.time,
                effectiveDate = effectiveDate.toString(),
                newDayOfWeek = newDayOfWeek.name,
                newTime = newTime,
                newLocation = newLocation
            )
            timetableRepository.saveClassShiftOverride(override)
            com.kanhaji.upastithi.data.TimeTableManager.addClassShiftOverride(override)
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                com.kanhaji.upastithi.features.home.data.local.UpasthitiDatabase
                    .getInstance(AndroidContext.appContext)
                    .attendanceDao()
                    .updateShiftedAttendanceTime(
                        timetableId = timetableId,
                        subjectId = classEntity.subject.subjectId,
                        originalTime = classEntity.time,
                        newTime = newTime,
                        effectiveDate = effectiveDate.toString()
                    )
            }
            refreshAttendance()
        }
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

package com.kanhaji.upasthiti.features.home.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.kanhaji.basics.entity.Update
import com.kanhaji.basics.networking.httpClient
import com.kanhaji.basics.util.Updater
import com.kanhaji.upasthiti.AndroidContext
import com.kanhaji.upasthiti.data.TimeTableManager
import com.kanhaji.upasthiti.features.home.data.AttendanceStatus
import com.kanhaji.upasthiti.features.home.data.AttendanceStorage
import com.kanhaji.upasthiti.features.home.data.Subject
import com.kanhaji.upasthiti.features.home.domain.model.AttendanceEntity
import com.kanhaji.upasthiti.features.home.domain.model.ClassEntity
import com.kanhaji.upasthiti.features.home.domain.usecase.GetClassesForDateUseCase
import com.kanhaji.upasthiti.features.home.domain.usecase.GetSubjectAttendanceStatsUseCase
import com.kanhaji.upasthiti.features.home.domain.usecase.SaveAttendanceUseCase
import com.kanhaji.upasthiti.util.UpasthitiUtils
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import com.kanhaji.upasthiti.features.home.data.local.entity.ClassShiftOverrideEntity
import com.kanhaji.upasthiti.features.home.data.repository.TimetableRepositoryImpl
import com.kanhaji.upasthiti.features.home.domain.model.ScheduleEvent
import com.kanhaji.upasthiti.features.home.domain.repository.TimetableRepository
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
            com.kanhaji.upasthiti.features.home.data.SaturdayScheduleManager.loadSettings()
        }
        screenModelScope.launch {
            androidx.compose.runtime.snapshotFlow { com.kanhaji.upasthiti.data.TimeTableManager.activeTimetableData }
                .collect {
                    val repository = TimetableRepositoryImpl()
                    val overrides = repository.getClassShiftOverridesDirect()
                    com.kanhaji.upasthiti.data.TimeTableManager.setClassShiftOverrides(overrides)
                }
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
            val timetableId = activeMeta?.id ?: com.kanhaji.upasthiti.data.TimeTableManager.getTimetableId()
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
            com.kanhaji.upasthiti.data.TimeTableManager.addClassShiftOverride(override)
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                com.kanhaji.upasthiti.features.home.data.local.UpasthitiDatabase
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

    fun swapClasses(
        class1: ClassEntity,
        targetEvent: ScheduleEvent,
        targetDay: DayOfWeek,
        effectiveDate: LocalDate,
        timetableRepository: TimetableRepository = TimetableRepositoryImpl()
    ) {
        screenModelScope.launch {
            val activeMeta = timetableRepository.getActiveTimetableDirect()
            val timetableId = activeMeta?.id ?: com.kanhaji.upasthiti.data.TimeTableManager.getTimetableId()

            // 1. Override for class1 moving to targetDay @ targetEvent.time
            val override1 = ClassShiftOverrideEntity(
                timetableId = timetableId,
                courseCode = class1.subject.subjectId,
                originalDayOfWeek = class1.dayOfWeek.name,
                originalTime = class1.time,
                effectiveDate = effectiveDate.toString(),
                newDayOfWeek = targetDay.name,
                newTime = targetEvent.time,
                newLocation = targetEvent.location ?: ""
            )

            // 2. Override for targetEvent moving to class1.dayOfWeek @ class1.time
            val override2 = ClassShiftOverrideEntity(
                timetableId = timetableId,
                courseCode = targetEvent.course_code,
                originalDayOfWeek = targetDay.name,
                originalTime = targetEvent.time,
                effectiveDate = effectiveDate.toString(),
                newDayOfWeek = class1.dayOfWeek.name,
                newTime = class1.time,
                newLocation = class1.roomNo
            )

            timetableRepository.saveClassShiftOverride(override1)
            timetableRepository.saveClassShiftOverride(override2)

            com.kanhaji.upasthiti.data.TimeTableManager.addClassShiftOverride(override1)
            com.kanhaji.upasthiti.data.TimeTableManager.addClassShiftOverride(override2)

            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                val attendanceDao = com.kanhaji.upasthiti.features.home.data.local.UpasthitiDatabase
                    .getInstance(AndroidContext.appContext)
                    .attendanceDao()

                attendanceDao.updateShiftedAttendanceTime(
                    timetableId = timetableId,
                    subjectId = class1.subject.subjectId,
                    originalTime = class1.time,
                    newTime = targetEvent.time,
                    effectiveDate = effectiveDate.toString()
                )

                attendanceDao.updateShiftedAttendanceTime(
                    timetableId = timetableId,
                    subjectId = targetEvent.course_code,
                    originalTime = targetEvent.time,
                    newTime = class1.time,
                    effectiveDate = effectiveDate.toString()
                )
            }
            refreshAttendance()
        }
    }

    fun getAttendanceForDate(date: LocalDate): List<AttendanceEntity> {
        return attendanceByDate[date] ?: emptyList()
    }

    fun getAttendanceForClass(date: LocalDate, time: String, subjectId: String? = null): AttendanceEntity? {
        val list = getAttendanceForDate(date)
        return if (subjectId != null) {
            list.firstOrNull { 
                it.time == time && 
                it.subject.subjectId.equals(subjectId, ignoreCase = true)
            }
        } else {
            list.firstOrNull { it.time == time }
        }
    }

    fun getAttendancesForSubject(subject: Subject): Pair<String, Double> {
        return getSubjectAttendanceStatsUseCase(subject)
    }

    fun getAttendanceColor(
        classEntity: ClassEntity,
        attendancesForDate: List<AttendanceEntity>,
        defaultColor: Color
    ): Color {
        return attendancesForDate.firstOrNull { 
            it.time == classEntity.time && 
            it.subject.subjectId.equals(classEntity.subject.subjectId, ignoreCase = true)
        }?.attendanceStatus?.color ?: defaultColor
    }

    suspend fun getLatestVersion(): Update {
        return try {
            val url = "${UpasthitiUtils.BASE_URL}${UpasthitiUtils.UPDATE_ENDPOINT}?t=${System.currentTimeMillis()}"
            println(">>> [UPDATE_CHECK] Requesting: $url")
            val response: io.ktor.client.statement.HttpResponse = httpClient.get(url) {
                headers.append(io.ktor.http.HttpHeaders.CacheControl, "no-cache, no-store, must-revalidate")
                headers.append("Pragma", "no-cache")
                headers.append("Expires", "0")
                headers.append("Connection", "close")
            }
            val rawBody: String = response.body()
            println(">>> [UPDATE_CHECK] Raw Response (${response.status}):\n$rawBody")
            val update: Update = com.kanhaji.basics.networking.json.decodeFromString(rawBody)
            println(">>> [UPDATE_CHECK] Parsed Update -> VersionCode: ${update.latestVersionCode}, Priority: ${update.priority}, Effective: ${update.getEffectivePriority(UpasthitiUtils.appVersionCode)}")
            update
        } catch (e: Exception) {
            println(">>> [UPDATE_CHECK] Error fetching update: ${e.message}")
            e.printStackTrace()
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
            Updater.showCriticalDialog = false
            Updater.showUpdateBottomSheet = false
            isUpdateAvailable = false

            val latest = getLatestVersion()
            Updater.update = latest
            UpasthitiUtils.updateChecked = true

            if (latest.latestVersionCode > UpasthitiUtils.appVersionCode) {
                isUpdateAvailable = true
                val effectivePriority = latest.getEffectivePriority(UpasthitiUtils.appVersionCode)
                Updater.updatePriority = effectivePriority
                Updater.isForceUpdate = (effectivePriority == com.kanhaji.basics.entity.UpdatePriority.CRITICAL)
                
                // Only auto-open on initial app launch / startup, not on every screen navigation
                if (!Updater.hasAutoPromptedUpdateThisSession) {
                    Updater.hasAutoPromptedUpdateThisSession = true
                    if (effectivePriority == com.kanhaji.basics.entity.UpdatePriority.RECOMMENDED || effectivePriority == com.kanhaji.basics.entity.UpdatePriority.CRITICAL) {
                        Updater.showUpdateBottomSheet = true
                    }
                }
                Updater.fetchChangelog(latest.changelog.ifBlank { "https://kanha321.github.io/Upastithi/Changelog.md" })
                Updater.prewarmConnection(latest.downloadUrl)
            } else {
                isUpdateAvailable = false
                Updater.isForceUpdate = false
                Updater.updatePriority = com.kanhaji.basics.entity.UpdatePriority.OPTIONAL
                Updater.showUpdateBottomSheet = false
                Updater.showCriticalDialog = false
            }
        }
    }
}

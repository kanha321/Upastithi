package com.kanhaji.upasthiti.features.home.data

import android.content.Context
import com.kanhaji.upasthiti.AndroidContext
import com.kanhaji.upasthiti.data.TimeTableManager
import com.kanhaji.upasthiti.features.home.data.local.UpasthitiDatabase
import com.kanhaji.upasthiti.features.home.data.local.dao.AttendanceDao
import com.kanhaji.upasthiti.features.home.data.local.entity.AttendanceRoomEntity
import com.kanhaji.upasthiti.features.home.domain.model.AttendanceEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.LocalDate
import java.util.UUID

import com.kanhaji.basics.datastore.PrefsManager

object AttendanceStorage {

    private val modeMap = mutableMapOf<String, AttendanceMode>()

    fun getSubjectAttendanceMode(subject: Subject): AttendanceMode {
        val key = subject.subjectId.ifEmpty { subject.displayName }
        return modeMap[key] ?: run {
            val saved = runBlocking(Dispatchers.IO) { PrefsManager.getString("att_mode_$key") }
            val mode = if (saved == AttendanceMode.PER_DAY.name) AttendanceMode.PER_DAY else AttendanceMode.PER_SLOT
            modeMap[key] = mode
            mode
        }
    }

    fun toggleSubjectAttendanceMode(subject: Subject): AttendanceMode {
        val key = subject.subjectId.ifEmpty { subject.displayName }
        val current = getSubjectAttendanceMode(subject)
        val next = if (current == AttendanceMode.PER_SLOT) AttendanceMode.PER_DAY else AttendanceMode.PER_SLOT
        modeMap[key] = next
        CoroutineScope(Dispatchers.IO).launch {
            PrefsManager.saveString("att_mode_$key", next.name)
        }
        return next
    }

    private fun getDao(context: Context = AndroidContext.appContext): AttendanceDao {
        return UpasthitiDatabase.getInstance(context).attendanceDao()
    }

    fun getAttendanceFlow(context: Context = AndroidContext.appContext): Flow<Map<LocalDate, List<AttendanceEntity>>> {
        val timetableId = TimeTableManager.getTimetableId()
        return getDao(context).getAllAttendancesForTimetable(timetableId).map { roomList ->
            roomList.map { it.toDomainEntity() }.groupBy { it.date }
        }
    }

    fun addAttendance(context: Context, attendance: AttendanceEntity) {
        val dao = getDao(context)
        val timetableId = attendance.timetableId.ifEmpty { TimeTableManager.getTimetableId() }

        CoroutineScope(Dispatchers.IO).launch {
            if (attendance.attendanceStatus == null) {
                dao.deleteAttendanceSlot(
                    timetableId = timetableId,
                    date = attendance.date.toString(),
                    time = attendance.time,
                    subjectId = attendance.subject.subjectId
                )
            } else {
                dao.upsertAttendance(attendance.toRoomEntity(timetableId))
            }
        }
    }

    fun loadAttendanceList(context: Context = AndroidContext.appContext): List<AttendanceEntity> {
        return runBlocking(Dispatchers.IO) {
            getDao(context).getAllAttendancesDirect().map { it.toDomainEntity() }
        }
    }

    fun getAttendanceForDate(
        date: LocalDate,
        context: Context = AndroidContext.appContext
    ): List<AttendanceEntity> {
        val timetableId = TimeTableManager.getTimetableId()
        return runBlocking(Dispatchers.IO) {
            getDao(context).getAttendancesForDateDirect(timetableId, date.toString()).map { it.toDomainEntity() }
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
        val timetableId = TimeTableManager.getTimetableId()
        return runBlocking(Dispatchers.IO) {
            getDao(context).getAttendancesForSubjectDirect(timetableId, subject.subjectId).map { it.toDomainEntity() }
        }
    }

    fun getAttendanceGroupedByDate(context: Context = AndroidContext.appContext): Map<LocalDate, List<AttendanceEntity>> {
        val timetableId = TimeTableManager.getTimetableId()
        return runBlocking(Dispatchers.IO) {
            val all = getDao(context).getAllAttendancesDirect()
                .filter { it.timetableId == timetableId || (it.timetableId.isEmpty() && timetableId == "default") }
                .map { it.toDomainEntity() }
            all.groupBy { it.date }
        }
    }
}

fun AttendanceRoomEntity.toDomainEntity(): AttendanceEntity {
    return AttendanceEntity(
        attendanceId = try { UUID.fromString(id) } catch (e: Exception) { UUID.nameUUIDFromBytes(id.toByteArray()) },
        timetableId = timetableId,
        date = LocalDate.parse(date),
        time = time,
        subject = Subject(
            displayName = subjectDisplayName,
            subjectId = subjectId,
            teacher = teacher,
            teacherInitials = teacherInitials
        ),
        attendanceStatus = status?.let {
            try { AttendanceStatus.valueOf(it) } catch (e: Exception) { null }
        }
    )
}

fun AttendanceEntity.toRoomEntity(timetableIdOverride: String? = null): AttendanceRoomEntity {
    val finalTimetableId = timetableIdOverride ?: timetableId.ifEmpty { TimeTableManager.getTimetableId() }
    val compositeSlotId = "${finalTimetableId}_${date}_${time}_${subject.subjectId}"
    return AttendanceRoomEntity(
        id = compositeSlotId,
        timetableId = finalTimetableId,
        date = date.toString(),
        time = time,
        subjectId = subject.subjectId,
        subjectDisplayName = subject.displayName,
        teacher = subject.teacher,
        teacherInitials = subject.teacherInitials ?: "",
        status = attendanceStatus?.name
    )
}

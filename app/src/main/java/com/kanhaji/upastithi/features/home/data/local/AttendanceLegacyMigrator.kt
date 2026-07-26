package com.kanhaji.upastithi.features.home.data.local

import android.content.Context
import android.util.Log
import com.kanhaji.upastithi.data.TimeTableManager
import com.kanhaji.upastithi.features.home.data.local.dao.AttendanceDao
import com.kanhaji.upastithi.features.home.data.local.entity.AttendanceRoomEntity
import com.kanhaji.upastithi.features.home.domain.model.AttendanceEntitySerialized
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.File

object AttendanceLegacyMigrator {

    private const val LEGACY_FILE = "attendance_data.json"
    private const val BAK_FILE = "attendance_data.json.bak"
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun migrateLegacyJsonIfNeeded(context: Context, dao: AttendanceDao) {
        withContext(Dispatchers.IO) {
            try {
                val legacyFile = File(context.filesDir, LEGACY_FILE)
                if (!legacyFile.exists()) return@withContext

                val jsonString = legacyFile.readText()
                if (jsonString.isBlank() || jsonString == "[]") {
                    legacyFile.renameTo(File(context.filesDir, BAK_FILE))
                    return@withContext
                }

                val legacyList = json.decodeFromString<List<AttendanceEntitySerialized>>(jsonString)
                if (legacyList.isNotEmpty()) {
                    val currentActiveId = TimeTableManager.getTimetableId()
                    val roomEntities = legacyList.map { legacy ->
                        AttendanceRoomEntity(
                            id = legacy.attendanceId,
                            timetableId = legacy.timetableId.ifEmpty { currentActiveId },
                            date = legacy.date,
                            time = legacy.time,
                            subjectId = legacy.subject.subjectId,
                            subjectDisplayName = legacy.subject.displayName,
                            teacher = legacy.subject.teacher,
                            teacherInitials = legacy.subject.teacherInitials ?: "",
                            status = legacy.attendanceStatus?.name
                        )
                    }
                    dao.insertAll(roomEntities)
                    Log.d("AttendanceLegacyMigrator", "Successfully migrated ${roomEntities.size} legacy records to Room DB.")
                }

                // Backup legacy file so migration doesn't re-run
                val bakFile = File(context.filesDir, BAK_FILE)
                if (bakFile.exists()) bakFile.delete()
                legacyFile.renameTo(bakFile)
            } catch (e: Exception) {
                Log.e("AttendanceLegacyMigrator", "Error migrating legacy JSON: ${e.message}", e)
            }
        }
    }
}

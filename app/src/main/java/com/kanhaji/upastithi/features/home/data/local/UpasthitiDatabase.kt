package com.kanhaji.upastithi.features.home.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.kanhaji.upastithi.features.home.data.local.dao.AttendanceDao
import com.kanhaji.upastithi.features.home.data.local.dao.ScheduleEventDao
import com.kanhaji.upastithi.features.home.data.local.dao.SubjectDao
import com.kanhaji.upastithi.features.home.data.local.dao.TimetableMetadataDao
import com.kanhaji.upastithi.features.home.data.local.entity.AttendanceRoomEntity
import com.kanhaji.upastithi.features.home.data.local.entity.ScheduleEventEntity
import com.kanhaji.upastithi.features.home.data.local.entity.SubjectEntity
import com.kanhaji.upastithi.features.home.data.local.entity.TimetableMetadataEntity

@Database(
    entities = [
        AttendanceRoomEntity::class,
        TimetableMetadataEntity::class,
        ScheduleEventEntity::class,
        SubjectEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class UpasthitiDatabase : RoomDatabase() {

    abstract fun attendanceDao(): AttendanceDao
    abstract fun timetableMetadataDao(): TimetableMetadataDao
    abstract fun scheduleEventDao(): ScheduleEventDao
    abstract fun subjectDao(): SubjectDao

    companion object {
        @Volatile
        private var INSTANCE: UpasthitiDatabase? = null

        fun getInstance(context: Context): UpasthitiDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    UpasthitiDatabase::class.java,
                    "upasthiti_database.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

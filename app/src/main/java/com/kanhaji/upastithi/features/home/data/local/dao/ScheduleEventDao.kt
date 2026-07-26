package com.kanhaji.upastithi.features.home.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kanhaji.upastithi.features.home.data.local.entity.ScheduleEventEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ScheduleEventDao {

    @Query("SELECT * FROM schedule_events WHERE timetableId = :timetableId ORDER BY startMinutes ASC")
    fun getAllEventsForTimetable(timetableId: String): Flow<List<ScheduleEventEntity>>

    @Query("SELECT * FROM schedule_events WHERE timetableId = :timetableId ORDER BY startMinutes ASC")
    fun getAllEventsForTimetableDirect(timetableId: String): List<ScheduleEventEntity>

    @Query("SELECT * FROM schedule_events WHERE timetableId = :timetableId AND dayOfWeek = :dayOfWeek ORDER BY startMinutes ASC")
    fun getEventsForDay(timetableId: String, dayOfWeek: String): Flow<List<ScheduleEventEntity>>

    @Query("SELECT * FROM schedule_events WHERE timetableId = :timetableId AND dayOfWeek = :dayOfWeek ORDER BY startMinutes ASC")
    fun getEventsForDayDirect(timetableId: String, dayOfWeek: String): List<ScheduleEventEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrUpdate(event: ScheduleEventEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(events: List<ScheduleEventEntity>): List<Long>

    @Query("DELETE FROM schedule_events WHERE id = :eventId")
    fun deleteById(eventId: String): Int

    @Query("DELETE FROM schedule_events WHERE timetableId = :timetableId")
    fun deleteByTimetableId(timetableId: String): Int
}

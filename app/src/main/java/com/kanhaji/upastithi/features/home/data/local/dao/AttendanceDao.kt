package com.kanhaji.upastithi.features.home.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kanhaji.upastithi.features.home.data.local.entity.AttendanceRoomEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AttendanceDao {

    @Query("SELECT * FROM attendance_records WHERE timetableId = :timetableId OR timetableId = '' OR :timetableId = 'default'")
    fun getAllAttendancesForTimetable(timetableId: String): Flow<List<AttendanceRoomEntity>>

    @Query("SELECT * FROM attendance_records WHERE (timetableId = :timetableId OR timetableId = '' OR :timetableId = 'default') AND date = :date")
    fun getAttendancesForDate(timetableId: String, date: String): Flow<List<AttendanceRoomEntity>>

    @Query("SELECT * FROM attendance_records WHERE (timetableId = :timetableId OR timetableId = '' OR :timetableId = 'default') AND date = :date")
    fun getAttendancesForDateDirect(timetableId: String, date: String): List<AttendanceRoomEntity>

    @Query("SELECT * FROM attendance_records WHERE (timetableId = :timetableId OR timetableId = '' OR :timetableId = 'default') AND subjectId = :subjectId")
    fun getAttendancesForSubject(timetableId: String, subjectId: String): Flow<List<AttendanceRoomEntity>>

    @Query("SELECT * FROM attendance_records WHERE (timetableId = :timetableId OR timetableId = '' OR :timetableId = 'default') AND subjectId = :subjectId")
    fun getAttendancesForSubjectDirect(timetableId: String, subjectId: String): List<AttendanceRoomEntity>

    @Query("SELECT * FROM attendance_records")
    fun getAllAttendancesDirect(): List<AttendanceRoomEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsertAttendance(attendance: AttendanceRoomEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(attendances: List<AttendanceRoomEntity>): List<Long>

    @Query("DELETE FROM attendance_records WHERE (timetableId = :timetableId OR (timetableId = '' AND :timetableId = 'default')) AND date = :date AND time = :time AND subjectId = :subjectId")
    fun deleteAttendanceSlot(timetableId: String, date: String, time: String, subjectId: String): Int

    @Query("DELETE FROM attendance_records WHERE id = :id")
    fun deleteById(id: String): Int
}

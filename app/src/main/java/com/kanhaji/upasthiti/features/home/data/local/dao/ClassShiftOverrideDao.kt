package com.kanhaji.upasthiti.features.home.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kanhaji.upasthiti.features.home.data.local.entity.ClassShiftOverrideEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ClassShiftOverrideDao {

    @Query("SELECT * FROM class_shift_overrides WHERE timetableId = :timetableId")
    fun getOverridesForTimetable(timetableId: String): Flow<List<ClassShiftOverrideEntity>>

    @Query("SELECT * FROM class_shift_overrides WHERE timetableId = :timetableId")
    fun getOverridesForTimetableDirect(timetableId: String): List<ClassShiftOverrideEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrUpdate(override: ClassShiftOverrideEntity): Long

    @Query("DELETE FROM class_shift_overrides WHERE id = :overrideId")
    fun deleteById(overrideId: String): Int

    @Query("DELETE FROM class_shift_overrides WHERE timetableId = :timetableId")
    fun deleteByTimetableId(timetableId: String): Int
}

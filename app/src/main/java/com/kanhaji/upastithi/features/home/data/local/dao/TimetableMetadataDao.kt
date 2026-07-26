package com.kanhaji.upastithi.features.home.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kanhaji.upastithi.features.home.data.local.entity.TimetableMetadataEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TimetableMetadataDao {

    @Query("SELECT * FROM timetable_metadata WHERE isActive = 1 LIMIT 1")
    fun getActiveTimetable(): Flow<TimetableMetadataEntity?>

    @Query("SELECT * FROM timetable_metadata WHERE isActive = 1 LIMIT 1")
    fun getActiveTimetableDirect(): TimetableMetadataEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrUpdate(metadata: TimetableMetadataEntity): Long

    @Query("UPDATE timetable_metadata SET isActive = 0")
    fun deactivateAll(): Int

    @Query("UPDATE timetable_metadata SET isActive = 1 WHERE id = :id")
    fun setActive(id: String): Int

    @Query("DELETE FROM timetable_metadata WHERE id = :id")
    fun deleteById(id: String): Int
}

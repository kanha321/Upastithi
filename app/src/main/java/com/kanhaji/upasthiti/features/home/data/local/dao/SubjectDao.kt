package com.kanhaji.upasthiti.features.home.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kanhaji.upasthiti.features.home.data.local.entity.SubjectEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SubjectDao {

    @Query("SELECT * FROM subjects WHERE timetableId = :timetableId OR timetableId = ''")
    fun getAllSubjects(timetableId: String): Flow<List<SubjectEntity>>

    @Query("SELECT * FROM subjects WHERE timetableId = :timetableId OR timetableId = ''")
    fun getAllSubjectsDirect(timetableId: String): List<SubjectEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(subjects: List<SubjectEntity>): List<Long>

    @Query("DELETE FROM subjects WHERE timetableId = :timetableId")
    fun deleteByTimetableId(timetableId: String): Int

    @Query("DELETE FROM subjects")
    fun deleteAll(): Int
}

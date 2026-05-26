package com.jlsh.aifit.feature.workout.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert

@Dao
interface WorkoutLogDao {

    @Query("SELECT * FROM workout_logs ORDER BY date DESC")
    suspend fun getAll(): List<WorkoutLogEntity>

    @Query(
        "SELECT * FROM workout_logs WHERE date >= :fromEpochDay AND date <= :toEpochDay ORDER BY date DESC",
    )
    suspend fun getByDateRange(fromEpochDay: Long, toEpochDay: Long): List<WorkoutLogEntity>

    @Query("SELECT * FROM workout_logs ORDER BY date DESC LIMIT :limit")
    suspend fun getRecent(limit: Int): List<WorkoutLogEntity>

    @Upsert
    suspend fun upsertAll(logs: List<WorkoutLogEntity>)

    @Query("SELECT * FROM workout_logs WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): WorkoutLogEntity?

    @Query("DELETE FROM workout_logs WHERE id = :id")
    suspend fun deleteById(id: String)
}


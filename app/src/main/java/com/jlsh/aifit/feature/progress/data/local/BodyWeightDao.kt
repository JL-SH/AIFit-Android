package com.jlsh.aifit.feature.progress.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert

@Dao
interface BodyWeightDao {

    @Query("SELECT * FROM body_weight_logs ORDER BY date DESC")
    suspend fun getAll(): List<BodyWeightEntity>

    @Query("SELECT * FROM body_weight_logs WHERE date BETWEEN :from AND :to ORDER BY date ASC")
    suspend fun getByDateRange(from: Long, to: Long): List<BodyWeightEntity>

    @Query("SELECT * FROM body_weight_logs ORDER BY date DESC LIMIT 1")
    suspend fun getLatest(): BodyWeightEntity?

    @Upsert
    suspend fun upsert(entity: BodyWeightEntity)

    @Upsert
    suspend fun upsertAll(entities: List<BodyWeightEntity>)
}


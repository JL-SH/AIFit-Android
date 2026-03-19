package com.jlsh.aifit.feature.training.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert

@Dao
interface TrainingPlanDao {

    @Query("SELECT * FROM training_plans WHERE userId = :userId")
    suspend fun getAllByUserId(userId: String): List<TrainingPlanEntity>

    @Query("SELECT * FROM training_plans WHERE id = :id")
    suspend fun getById(id: String): TrainingPlanEntity?

    @Upsert
    suspend fun upsertAll(plans: List<TrainingPlanEntity>)

    @Query("DELETE FROM training_plans WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM training_plans WHERE userId = :userId")
    suspend fun deleteAllByUserId(userId: String)
}


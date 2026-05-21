package com.jlsh.aifit.feature.training.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface TrainingPlanDetailCacheDao {

    @Query("SELECT * FROM training_plan_details WHERE planId = :planId LIMIT 1")
    suspend fun getById(planId: String): TrainingPlanDetailCacheEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: TrainingPlanDetailCacheEntity)
}

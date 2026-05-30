package com.jlsh.aifit.feature.diet.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface DietPlanDetailCacheDao {

    @Query("SELECT * FROM diet_plan_details WHERE planId = :planId LIMIT 1")
    suspend fun getById(planId: String): DietPlanDetailCacheEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: DietPlanDetailCacheEntity)
}

package com.jlsh.aifit.feature.nutrition.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface NutritionTargetDao {

    @Query("SELECT * FROM nutrition_targets ORDER BY effectiveFrom DESC LIMIT 1")
    suspend fun getCurrent(): NutritionTargetEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: NutritionTargetEntity)
}

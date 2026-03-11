package com.jlsh.aifit.feature.nutrition.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert

@Dao
interface NutritionLogDao {

    @Query("SELECT * FROM nutrition_logs WHERE date = :date")
    suspend fun getByDate(date: Long): NutritionLogEntity?

    @Upsert
    suspend fun upsert(entity: NutritionLogEntity)

    @Query("DELETE FROM nutrition_logs WHERE date = :date")
    suspend fun deleteByDate(date: Long)
}


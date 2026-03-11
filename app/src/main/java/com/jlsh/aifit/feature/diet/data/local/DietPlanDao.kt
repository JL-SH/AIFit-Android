package com.jlsh.aifit.feature.diet.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert

@Dao
interface DietPlanDao {

    @Query("SELECT * FROM diet_plans")
    suspend fun getAll(): List<DietPlanEntity>

    @Query("SELECT * FROM diet_plans WHERE id = :id")
    suspend fun getById(id: String): DietPlanEntity?

    @Upsert
    suspend fun upsertAll(plans: List<DietPlanEntity>)

    @Query("DELETE FROM diet_plans WHERE id = :id")
    suspend fun deleteById(id: String)
}


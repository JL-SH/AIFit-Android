package com.jlsh.aifit.feature.diet.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert

@Dao
interface DietPlanDao {

    @Query("SELECT * FROM diet_plans WHERE userId = :userId")
    suspend fun getAllByUserId(userId: String): List<DietPlanEntity>

    @Query("SELECT * FROM diet_plans WHERE id = :id")
    suspend fun getById(id: String): DietPlanEntity?

    @Upsert
    suspend fun upsertAll(plans: List<DietPlanEntity>)

    @Query("DELETE FROM diet_plans WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM diet_plans WHERE userId = :userId")
    suspend fun deleteAllByUserId(userId: String)

    /**
     * Reconciliation query: removes every row for the given user whose id is NOT in the
     * provided network-response set. Mirrors TrainingPlanDao.deleteAllNotInIds().
     */
    @Query("DELETE FROM diet_plans WHERE userId = :userId AND id NOT IN (:ids)")
    suspend fun deleteAllNotInIds(userId: String, ids: List<String>)

    @Query("DELETE FROM diet_plans")
    suspend fun deleteAll()
}


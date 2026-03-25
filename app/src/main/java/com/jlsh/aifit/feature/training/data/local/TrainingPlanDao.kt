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

    /**
     * Reconciliation query: removes every row whose id is NOT in the provided network-response set.
     * Called after a successful full-list sync to make Room a mirror of the server state and
     * prevent ghost plans (soft-deleted server-side but still present in the local cache).
     */
    @Query("DELETE FROM training_plans WHERE id NOT IN (:ids)")
    suspend fun deleteAllNotInIds(ids: List<String>)

    /** Nuclear fallback used when the network returns an empty list (user deleted all plans). */
    @Query("DELETE FROM training_plans")
    suspend fun deleteAll()
}


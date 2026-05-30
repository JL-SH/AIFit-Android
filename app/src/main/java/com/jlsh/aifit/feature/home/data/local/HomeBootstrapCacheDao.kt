package com.jlsh.aifit.feature.home.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface HomeBootstrapCacheDao {

    @Query("SELECT * FROM home_bootstrap_cache WHERE userId = :userId LIMIT 1")
    suspend fun getByUserId(userId: String): HomeBootstrapCacheEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: HomeBootstrapCacheEntity)

    @Query("DELETE FROM home_bootstrap_cache WHERE userId = :userId")
    suspend fun deleteByUserId(userId: String)
}

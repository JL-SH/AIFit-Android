package com.jlsh.aifit.feature.user.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert

@Dao
interface UserProfileDao {

    @Query("SELECT * FROM user_profiles WHERE id = :id")
    suspend fun getById(id: String): UserProfileEntity?

    @Upsert
    suspend fun upsert(entity: UserProfileEntity)

    @Query("DELETE FROM user_profiles WHERE id = :id")
    suspend fun delete(id: String)
}


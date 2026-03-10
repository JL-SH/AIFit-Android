package com.jlsh.aifit.feature.user.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profiles")
data class UserProfileEntity(
    @PrimaryKey val id: String,
    val name: String,
    val email: String,
    val goalType: String?,
    val fitnessLevel: String?,
    val profilePictureUrl: String?,
)


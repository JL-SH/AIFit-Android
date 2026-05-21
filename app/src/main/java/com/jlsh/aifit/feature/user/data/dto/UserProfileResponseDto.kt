package com.jlsh.aifit.feature.user.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class UserProfileResponseDto(
    val id: String,
    val name: String,
    val email: String,
    val authProvider: String,
    val profilePictureUrl: String? = null,
    /** Canonical Cloudinary URL from backend; coalesced into profilePictureUrl in UserMapper. */
    val profileImageUrl: String? = null,
    val birthDate: String? = null,
    val gender: String? = null,
    val height: Float? = null,
    val weight: Float? = null,
    val targetWeight: Float? = null,
    val goalType: String? = null,
    val activityLevel: String? = null,
    val fitnessLevel: String? = null,
    val preferredLocation: String? = null,
    val dietPreference: String? = null,
    val knowledgeLevel: String? = null,
    val weeklyWorkoutDays: Int? = null,
    val availableMinutesPerSession: Int? = null,
    val injuries: String? = null,
    val calorieTarget: Int? = null,
)


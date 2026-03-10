package com.jlsh.aifit.feature.user.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class CreateUserProfileRequestDto(
    val birthDate: String? = null,
    val gender: String? = null,
    val goalType: String? = null,
    val activityLevel: String? = null,
    val fitnessLevel: String? = null,
    val preferredLocation: String? = null,
    val dietPreference: String? = null,
    val height: Float? = null,
    val weight: Float? = null,
    val targetWeight: Float? = null,
    val weeklyWorkoutDays: Int? = null,
    val availableMinutesPerSession: Int? = null,
    val injuries: String? = null,
    val calorieTarget: Int? = null,
)


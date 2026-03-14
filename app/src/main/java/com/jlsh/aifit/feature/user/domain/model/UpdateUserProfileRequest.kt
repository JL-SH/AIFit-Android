package com.jlsh.aifit.feature.user.domain.model

import java.time.LocalDate

data class UpdateUserProfileRequest(
    val birthDate: LocalDate? = null,
    val gender: Gender? = null,
    val goalType: GoalType? = null,
    val activityLevel: ActivityLevel? = null,
    val fitnessLevel: FitnessLevel? = null,
    val workoutLocation: WorkoutLocation? = null,
    val dietPreference: DietPreference? = null,
    val height: Float? = null,
    val weight: Float? = null,
    val targetWeight: Float? = null,
    val weeklyWorkoutDays: Int? = null,
    val availableMinutesPerSession: Int? = null,
    val injuries: String? = null,
    val calorieTarget: Int? = null,
)


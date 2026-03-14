package com.jlsh.aifit.feature.user.domain.model

import java.time.LocalDate

data class UserProfile(
    val id: String,
    val name: String,
    val email: String,
    val authProvider: String,
    val profilePictureUrl: String? = null,
    val birthDate: LocalDate? = null,
    val gender: Gender? = null,
    val height: Float? = null,
    val weight: Float? = null,
    val targetWeight: Float? = null,
    val goalType: GoalType? = null,
    val activityLevel: ActivityLevel? = null,
    val fitnessLevel: FitnessLevel? = null,
    val workoutLocation: WorkoutLocation? = null,
    val dietPreference: DietPreference? = null,
    val knowledgeLevel: String? = null,
    val weeklyWorkoutDays: Int? = null,
    val availableMinutesPerSession: Int? = null,
    val injuries: String? = null,
    val calorieTarget: Int? = null,
)


package com.jlsh.aifit.feature.user.domain.model

import java.time.LocalDate

/**
 * User profile with identity data, anthropometry and training/nutrition preferences.
 *
 * @property id Unique identifier of the user.
 * @property name Name to display.
 * @property email Email of the account.
 * @property authProvider Authentication provider (e.g. `LOCAL`, `GOOGLE`).
 * @property profilePictureUrl URL of the avatar, if it exists.
 * @property birthDate Date of birth.
 * @property gender Declared gender.
 * @property height Height in centimeters.
 * @property weight Current weight in kilograms.
 * @property targetWeight Target weight in kilograms.
 * @property goalType Main goal (fat loss, muscle gain, etc.).
 * @property activityLevel Daily activity level.
 * @property fitnessLevel Training experience level.
 * @property workoutLocation Preferred place to work out.
 * @property dietPreference Dietary preference.
 * @property knowledgeLevel Fitness/nutrition knowledge level (free text from backend).
 * @property weeklyWorkoutDays Training days per week.
 * @property availableMinutesPerSession Available minutes per session.
 * @property injuries Injuries or limitations indicated by the user.
 * @property calorieTarget Daily calorie goal in kcal.
 */
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


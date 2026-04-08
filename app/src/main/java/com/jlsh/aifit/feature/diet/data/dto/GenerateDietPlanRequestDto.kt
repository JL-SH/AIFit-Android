package com.jlsh.aifit.feature.diet.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class GenerateDietPlanRequestDto(
    val durationWeeks: Int,
    val mealsPerDay: Int,
    val dietPreference: String,
    val goalType: String? = null,
    val dailyCalories: Int? = null,
    val budget: String? = null,
    val allergies: String? = null,
    val additionalNotes: String? = null,
)


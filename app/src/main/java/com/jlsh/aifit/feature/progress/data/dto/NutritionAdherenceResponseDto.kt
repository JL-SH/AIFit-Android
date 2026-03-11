package com.jlsh.aifit.feature.progress.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class NutritionAdherenceResponseDto(
    val targetCalories: Int,
    val averageCaloriesConsumed: Double,
    val calorieAdherencePercentage: Double,
    val targetProtein: Double,
    val averageProteinConsumed: Double,
    val proteinAdherencePercentage: Double,
    val daysTracked: Int,
    val weeklyBreakdown: List<WeeklyNutritionDto> = emptyList(),
)

@Serializable
data class WeeklyNutritionDto(
    val weekStart: String,
    val avgCalories: Double,
    val avgProtein: Double,
    val daysTracked: Int,
)

package com.jlsh.aifit.feature.nutrition.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class NutritionLogResponseDto(
    val id: String,
    val date: String,
    val totalCalories: Int,
    val totalProteinGrams: Double,
    val totalCarbsGrams: Double,
    val totalFatGrams: Double,
    val meals: List<MealLogResponseDto> = emptyList(),
)


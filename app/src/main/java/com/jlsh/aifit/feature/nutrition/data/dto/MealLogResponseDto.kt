package com.jlsh.aifit.feature.nutrition.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class MealLogResponseDto(
    val id: String,
    val mealType: String,
    val name: String? = null,
    val time: String,
    val calories: Int,
    val proteinGrams: Double,
    val carbsGrams: Double,
    val fatGrams: Double,
    val aiGenerated: Boolean = false,
    val rawInputText: String? = null,
    val items: List<FoodItemLogResponseDto> = emptyList(),
)


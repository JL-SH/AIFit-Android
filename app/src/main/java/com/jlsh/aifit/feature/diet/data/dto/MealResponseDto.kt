package com.jlsh.aifit.feature.diet.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class MealResponseDto(
    val id: String,
    val mealType: String,
    val name: String,
    val time: String,
    val calories: Int,
    val proteinGrams: Int,
    val carbsGrams: Int,
    val fatGrams: Int,
    val items: List<MealItemResponseDto> = emptyList(),
)


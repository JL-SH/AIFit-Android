package com.jlsh.aifit.feature.diet.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class DietDayResponseDto(
    val id: String,
    val dayNumber: Int,
    val name: String,
    val totalCalories: Int,
    val meals: List<MealResponseDto> = emptyList(),
)


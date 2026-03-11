package com.jlsh.aifit.feature.diet.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class DietPlanResponseDto(
    val id: String,
    val name: String,
    val description: String? = null,
    val dailyCalories: Int,
    val proteinGrams: Int,
    val carbsGrams: Int,
    val fatGrams: Int,
    val durationWeeks: Int,
    val preference: String,
    val status: String,
    val totalDays: Int? = null,
    val createdAt: String,
    val days: List<DietDayResponseDto> = emptyList(),
)


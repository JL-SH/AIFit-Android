package com.jlsh.aifit.feature.nutrition.domain.model

import java.time.LocalDate

data class NutritionLog(
    val id: String,
    val date: LocalDate,
    val totalCalories: Int,
    val totalProteinGrams: Double,
    val totalCarbsGrams: Double,
    val totalFatGrams: Double,
    val meals: List<MealLog> = emptyList(),
)


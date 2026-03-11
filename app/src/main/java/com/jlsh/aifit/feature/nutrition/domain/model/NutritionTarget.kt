package com.jlsh.aifit.feature.nutrition.domain.model

import java.time.LocalDate

data class NutritionTarget(
    val id: String,
    val calorieTarget: Int,
    val proteinTarget: Double,
    val carbsTarget: Double,
    val fatTarget: Double,
    val effectiveFrom: LocalDate,
    val setBy: TargetSource,
)


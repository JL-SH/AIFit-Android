package com.jlsh.aifit.feature.progress.domain.model

data class NutritionAdherence(
    val averageCalories: Double,
    val calorieTarget: Int,
    val adherencePercentage: Double,
)


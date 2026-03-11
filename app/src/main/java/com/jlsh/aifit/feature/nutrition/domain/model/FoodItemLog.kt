package com.jlsh.aifit.feature.nutrition.domain.model

data class FoodItemLog(
    val id: String,
    val name: String,
    val quantity: Double,
    val unit: String,
    val calories: Int,
    val proteinGrams: Double,
    val carbsGrams: Double,
    val fatGrams: Double,
)


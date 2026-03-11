package com.jlsh.aifit.feature.diet.domain.model

data class MealItem(
    val id: String,
    val name: String,
    val quantity: Float,
    val unit: String,
    val calories: Int,
    val proteinGrams: Float,
    val carbsGrams: Float,
    val fatGrams: Float,
)


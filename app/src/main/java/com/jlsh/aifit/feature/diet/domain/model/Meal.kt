package com.jlsh.aifit.feature.diet.domain.model

data class Meal(
    val id: String,
    val mealType: MealType,
    val name: String,
    val time: String,
    val calories: Int,
    val proteinGrams: Int,
    val carbsGrams: Int,
    val fatGrams: Int,
    val items: List<MealItem>,
)


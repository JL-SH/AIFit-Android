package com.jlsh.aifit.feature.nutrition.domain.model

import com.jlsh.aifit.feature.diet.domain.model.MealType

data class MealLog(
    val id: String,
    val mealType: MealType,
    val name: String?,
    val time: String,
    val calories: Int,
    val proteinGrams: Double,
    val carbsGrams: Double,
    val fatGrams: Double,
    val aiGenerated: Boolean,
    val rawInputText: String?,
    val items: List<FoodItemLog>,
)


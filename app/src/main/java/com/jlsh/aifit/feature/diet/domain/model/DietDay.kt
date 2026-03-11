package com.jlsh.aifit.feature.diet.domain.model

data class DietDay(
    val id: String,
    val dayNumber: Int,
    val name: String,
    val totalCalories: Int,
    val meals: List<Meal>,
)


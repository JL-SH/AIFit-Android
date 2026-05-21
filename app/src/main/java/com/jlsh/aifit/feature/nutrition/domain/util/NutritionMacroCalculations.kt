package com.jlsh.aifit.feature.nutrition.domain.util

import kotlin.math.roundToInt

private val WEIGHT_VOLUME_UNITS = setOf("g", "ml")

fun usesPer100gScaling(unit: String): Boolean =
    unit.lowercase() in WEIGHT_VOLUME_UNITS

fun scaleCaloriesFromPer100g(caloriesPer100g: Int, quantityGrams: Double): Int =
    (caloriesPer100g * quantityGrams / 100.0).roundToInt()

fun scaleMacroGramsFromPer100g(gramsPer100g: Double, quantityGrams: Double): Double =
    gramsPer100g * quantityGrams / 100.0

fun scaleFoodItemMacros(
    unit: String,
    quantity: Double?,
    caloriesPer100g: Int?,
    proteinPer100g: Double?,
    carbsPer100g: Double?,
    fatPer100g: Double?,
): ScaledFoodMacros {
    if (!usesPer100gScaling(unit) || quantity == null || quantity <= 0.0) {
        return ScaledFoodMacros(
            calories = caloriesPer100g,
            proteinGrams = proteinPer100g,
            carbsGrams = carbsPer100g,
            fatGrams = fatPer100g,
        )
    }
    return ScaledFoodMacros(
        calories = caloriesPer100g?.let { scaleCaloriesFromPer100g(it, quantity) },
        proteinGrams = proteinPer100g?.let { scaleMacroGramsFromPer100g(it, quantity) },
        carbsGrams = carbsPer100g?.let { scaleMacroGramsFromPer100g(it, quantity) },
        fatGrams = fatPer100g?.let { scaleMacroGramsFromPer100g(it, quantity) },
    )
}

data class ScaledFoodMacros(
    val calories: Int?,
    val proteinGrams: Double?,
    val carbsGrams: Double?,
    val fatGrams: Double?,
)

package com.jlsh.aifit.feature.nutrition.domain.model

import java.time.LocalDate

/**
 * Aggregate one-day nutritional log with macro totals and meals recorded.
 *
 * @property id Unique identifier of the journal.
 * @property date Registration date.
 * @property totalCalories Sum of calories consumed in the day.
 * @property totalProteinGrams Sum of protein consumed in grams.
 * @property totalCarbsGrams Sum of carbohydrates consumed in grams.
 * @property totalFatGrams Sum of fat consumed in grams.
 * @property meals List of meals recorded on that day.
 */
data class NutritionLog(
    val id: String,
    val date: LocalDate,
    val totalCalories: Int,
    val totalProteinGrams: Double,
    val totalCarbsGrams: Double,
    val totalFatGrams: Double,
    val meals: List<MealLog> = emptyList(),
)

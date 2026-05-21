package com.jlsh.aifit.feature.nutrition.domain.model

import java.time.LocalDate

/**
 * Registro nutricional agregado de un día con totales de macros y comidas registradas.
 *
 * @property id Identificador único del registro diario.
 * @property date Fecha del registro.
 * @property totalCalories Suma de calorías consumidas en el día.
 * @property totalProteinGrams Suma de proteína consumida en gramos.
 * @property totalCarbsGrams Suma de carbohidratos consumidos en gramos.
 * @property totalFatGrams Suma de grasas consumidas en gramos.
 * @property meals Lista de comidas registradas en ese día.
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

package com.jlsh.aifit.feature.diet.domain.util

import com.jlsh.aifit.feature.diet.domain.model.DietPlan
import com.jlsh.aifit.feature.diet.domain.model.Meal
import java.time.LocalDate

/**
 * Devuelve las comidas programadas para el día actual según el calendario del plan.
 * Usa el mismo índice cíclico que el dashboard de inicio (lunes = día 1, etc.).
 */
fun DietPlan.mealsForToday(): List<Meal> {
    if (days.isEmpty()) return emptyList()
    val dayOfWeek = LocalDate.now().dayOfWeek.value
    val todayDietDay = days.getOrNull((dayOfWeek - 1) % days.size) ?: return emptyList()
    return todayDietDay.meals
}

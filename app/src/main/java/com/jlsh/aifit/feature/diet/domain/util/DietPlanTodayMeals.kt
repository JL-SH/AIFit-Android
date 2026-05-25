package com.jlsh.aifit.feature.diet.domain.util

import com.jlsh.aifit.feature.diet.domain.model.DietPlan
import com.jlsh.aifit.feature.diet.domain.model.Meal
import java.time.LocalDate

/**
 * Returns meals scheduled for the current day based on the plan calendar.
 * Use the same cyclical index as the home dashboard (Monday = day 1, etc.).
 */
fun DietPlan.mealsForToday(): List<Meal> {
    if (days.isEmpty()) return emptyList()
    val dayOfWeek = LocalDate.now().dayOfWeek.value
    val todayDietDay = days.getOrNull((dayOfWeek - 1) % days.size) ?: return emptyList()
    return todayDietDay.meals
}

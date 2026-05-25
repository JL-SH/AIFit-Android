package com.jlsh.aifit.feature.diet.domain.model

import com.jlsh.aifit.feature.training.domain.model.PlanStatus
import com.jlsh.aifit.feature.user.domain.model.DietPreference
import java.time.LocalDateTime

/**
 * Structured diet plan with associated calorie goals, macros, and meal days.
 *
 * @property id Unique identifier of the plan.
 * @property name Display name of the plan.
 * @property description Optional description of the plan.
 * @property dailyCalories Daily calorie goal in kcal.
 * @property proteinGrams Daily protein goal in grams.
 * @property carbsGrams Daily carb goal in grams.
 * @property fatGrams Daily fat goal in grams.
 * @property durationWeeks Total duration of the plan in weeks.
 * @property preference Diet preference or style ([DietPreference]).
 * @property status Plan lifecycle status ([PlanStatus]).
 * @property totalDays Total number of days in the plan.
 * @property createdAt Plan creation date and time.
 * @property days List of days with meals; empty in listing summaries.
 */
data class DietPlan(
    val id: String,
    val name: String,
    val description: String?,
    val dailyCalories: Int,
    val proteinGrams: Int,
    val carbsGrams: Int,
    val fatGrams: Int,
    val durationWeeks: Int,
    val preference: DietPreference,
    val status: PlanStatus,
    val totalDays: Int,
    val createdAt: LocalDateTime,
    val days: List<DietDay> = emptyList(),
)

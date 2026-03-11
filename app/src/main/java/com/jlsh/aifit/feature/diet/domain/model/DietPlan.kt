package com.jlsh.aifit.feature.diet.domain.model

import com.jlsh.aifit.feature.training.domain.model.PlanStatus
import com.jlsh.aifit.feature.user.domain.model.DietPreference
import java.time.LocalDateTime

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


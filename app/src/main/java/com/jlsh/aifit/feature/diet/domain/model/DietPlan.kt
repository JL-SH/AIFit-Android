package com.jlsh.aifit.feature.diet.domain.model

import com.jlsh.aifit.feature.training.domain.model.PlanStatus
import com.jlsh.aifit.feature.user.domain.model.DietPreference
import java.time.LocalDateTime

/**
 * Plan de dieta estructurado con objetivos calóricos, macros y días de comidas asociados.
 *
 * @property id Identificador único del plan.
 * @property name Nombre visible del plan.
 * @property description Descripción opcional del plan.
 * @property dailyCalories Objetivo calórico diario en kcal.
 * @property proteinGrams Objetivo diario de proteína en gramos.
 * @property carbsGrams Objetivo diario de carbohidratos en gramos.
 * @property fatGrams Objetivo diario de grasas en gramos.
 * @property durationWeeks Duración total del plan en semanas.
 * @property preference Preferencia o estilo de dieta ([DietPreference]).
 * @property status Estado de ciclo de vida del plan ([PlanStatus]).
 * @property totalDays Número total de días del plan.
 * @property createdAt Fecha y hora de creación del plan.
 * @property days Lista de días con comidas; vacía en resúmenes de listado.
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

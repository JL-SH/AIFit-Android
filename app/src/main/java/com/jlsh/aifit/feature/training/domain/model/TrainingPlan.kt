package com.jlsh.aifit.feature.training.domain.model

import com.jlsh.aifit.feature.user.domain.model.FitnessLevel
import com.jlsh.aifit.feature.user.domain.model.GoalType
import com.jlsh.aifit.feature.user.domain.model.WorkoutLocation
import java.time.LocalDateTime

/**
 * Plan de entrenamiento estructurado con metadatos de configuración y días asociados.
 *
 * @property id Identificador único del plan.
 * @property name Nombre visible del plan.
 * @property description Descripción opcional del plan.
 * @property frequencyDaysPerWeek Días de entrenamiento por semana previstos.
 * @property durationWeeks Duración total del plan en semanas.
 * @property goalType Objetivo principal del usuario para este plan.
 * @property fitnessLevel Nivel de condición física objetivo del plan.
 * @property location Lugar habitual de entrenamiento (gimnasio, casa, etc.).
 * @property status Estado de ciclo de vida del plan ([PlanStatus]).
 * @property totalDays Número total de días de entrenamiento del plan.
 * @property createdAt Fecha y hora de creación del plan.
 * @property days Lista de días de entrenamiento; vacía en resúmenes de listado.
 */
data class TrainingPlan(
    val id: String,
    val name: String,
    val description: String?,
    val frequencyDaysPerWeek: Int,
    val durationWeeks: Int,
    val goalType: GoalType,
    val fitnessLevel: FitnessLevel,
    val location: WorkoutLocation,
    val status: PlanStatus,
    val totalDays: Int,
    val createdAt: LocalDateTime,
    val days: List<TrainingDay> = emptyList(),
)

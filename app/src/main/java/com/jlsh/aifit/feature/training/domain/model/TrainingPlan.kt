package com.jlsh.aifit.feature.training.domain.model

import com.jlsh.aifit.feature.user.domain.model.FitnessLevel
import com.jlsh.aifit.feature.user.domain.model.GoalType
import com.jlsh.aifit.feature.user.domain.model.WorkoutLocation
import java.time.LocalDateTime

/**
 * Structured training plan with configuration metadata and associated days.
 *
 * @property id Unique identifier of the plan.
 * @property name Display name of the plan.
 * @property description Optional description of the plan.
 * @property frequencyDaysPerWeek Expected training days per week.
 * @property durationWeeks Total duration of the plan in weeks.
 * @property goalType User's primary goal for this plan.
 * @property fitnessLevel Plan's target fitness level.
 * @property location Usual workout location (gym, home, etc.).
 * @property status Plan lifecycle status ([PlanStatus]).
 * @property totalDays Total number of training days in the plan.
 * @property createdAt Plan creation date and time.
 * @property days List of training days; empty in listing summaries.
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

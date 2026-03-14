package com.jlsh.aifit.feature.training.domain.model

import com.jlsh.aifit.feature.user.domain.model.FitnessLevel
import com.jlsh.aifit.feature.user.domain.model.GoalType
import com.jlsh.aifit.feature.user.domain.model.WorkoutLocation
import java.time.LocalDateTime

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


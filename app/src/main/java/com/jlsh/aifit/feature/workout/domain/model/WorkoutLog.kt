package com.jlsh.aifit.feature.workout.domain.model

import java.time.LocalDate
import java.time.LocalDateTime

data class WorkoutLog(
    val id: String,
    val trainingPlanId: String,
    val trainingDayId: String,
    val date: LocalDate,
    val durationMinutes: Int?,
    val perceivedExertion: Int?,
    val notes: String?,
    val totalExercises: Int,
    val completedAt: LocalDateTime,
    val sets: List<WorkoutSetLog> = emptyList(),
    val gamificationResult: GamificationResult? = null,
)


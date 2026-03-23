package com.jlsh.aifit.feature.workout.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class WorkoutLogSummaryResponseDto(
    val id: String,
    val trainingPlanId: String,
    val trainingDayId: String,
    val date: String,
    val durationMinutes: Int? = null,
    val perceivedExertion: Int? = null,
    val totalExercises: Int,
    val completedAt: String,
    val isLocked: Boolean = false,
)


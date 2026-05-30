package com.jlsh.aifit.feature.workout.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class WorkoutLogSummaryResponseDto(
    val id: String,
    val trainingPlanId: String? = null,
    val trainingDayId: String,
    val date: String,
    val durationMinutes: Int? = null,
    val perceivedExertion: Int? = null,
    val totalExercises: Int,
    val totalSets: Int = 0,
    val completedAt: String,
    val isLocked: Boolean = false,
)


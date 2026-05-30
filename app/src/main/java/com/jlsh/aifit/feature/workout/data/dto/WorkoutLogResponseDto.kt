package com.jlsh.aifit.feature.workout.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class WorkoutLogResponseDto(
    val id: String,
    val trainingPlanId: String? = null,
    val trainingDayId: String,
    val date: String,
    val durationMinutes: Int? = null,
    val perceivedExertion: Int? = null,
    val notes: String? = null,
    val exercises: List<WorkoutSetLogResponseDto> = emptyList(),
    val completedAt: String,
    val gamificationResult: GamificationResultResponseDto? = null,
    val isLocked: Boolean = false,
    val perceivedSystemicFatigue: Int? = null,
    val jointPainReport: List<JointPainEntryDto>? = null,
)


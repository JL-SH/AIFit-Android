package com.jlsh.aifit.feature.workout.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class FinalizeWorkoutSessionRequestDto(
    val systemicFatigue: Int,
    val jointPainReport: List<JointPainEntryDto>,
)


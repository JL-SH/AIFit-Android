package com.jlsh.aifit.feature.workout.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class ProgressionEntryResponseDto(
    val date: String,
    val exerciseSetNumber: Int,
    val repsCompleted: Int,
    val weightUsed: Double,
)


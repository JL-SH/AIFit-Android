package com.jlsh.aifit.feature.training.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class GenerateTrainingPlanRequestDto(
    val frequencyDaysPerWeek: Int,
    val sessionDurationMinutes: Int,
    val durationWeeks: Int,
    val goalType: String,
    val fitnessLevel: String,
    val location: String,
    val injuries: String? = null,
    val additionalNotes: String? = null,
)


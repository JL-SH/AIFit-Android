package com.jlsh.aifit.feature.training.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class TrainingPlanSummaryResponseDto(
    val id: String,
    val name: String,
    val description: String? = null,
    val frequencyDaysPerWeek: Int,
    val durationWeeks: Int,
    val goalType: String,
    val fitnessLevel: String,
    val location: String,
    val status: String,
    val totalDays: Int,
    val createdAt: String,
)


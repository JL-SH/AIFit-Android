package com.jlsh.aifit.feature.progression.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class PlanProgressionSummaryResponseDto(
    val trainingPlanId: String,
    val totalExercises: Int,
    val exercisesAnalyzed: Int,
    val recommendations: List<ProgressionRecommendationResponseDto> = emptyList(),
)


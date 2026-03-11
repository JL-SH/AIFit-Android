package com.jlsh.aifit.feature.metabolic.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class MetabolicInsightResponseDto(
    val id: String,
    val statusAtTime: String,
    val adjustmentType: String,
    val previousCalorieTarget: Int,
    val newCalorieTarget: Int,
    val magnitude: String,
    val rationale: String,
    val appliedAt: String,
)


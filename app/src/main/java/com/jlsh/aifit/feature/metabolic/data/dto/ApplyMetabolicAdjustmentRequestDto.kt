package com.jlsh.aifit.feature.metabolic.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class ApplyMetabolicAdjustmentRequestDto(
    val newCalorieTarget: Int,
    val newProteinTarget: Double,
    val newCarbsTarget: Double,
    val newFatTarget: Double,
    val adjustmentType: String,
    val magnitude: String,
    val rationale: String? = null,
)


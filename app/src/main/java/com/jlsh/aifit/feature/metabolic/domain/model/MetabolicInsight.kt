package com.jlsh.aifit.feature.metabolic.domain.model

data class MetabolicInsight(
    val id: String,
    val statusAtTime: MetabolicStatus,
    val adjustmentType: AdjustmentType,
    val previousCalorieTarget: Int,
    val newCalorieTarget: Int,
    val magnitude: AdjustmentMagnitude,
    val rationale: String,
    val appliedAt: String,
)


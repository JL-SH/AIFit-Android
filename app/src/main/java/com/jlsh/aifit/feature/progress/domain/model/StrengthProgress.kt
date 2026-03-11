package com.jlsh.aifit.feature.progress.domain.model

data class StrengthProgress(
    val exerciseName: String,
    val startMax: Double,
    val currentMax: Double,
    val changePercentage: Double,
)


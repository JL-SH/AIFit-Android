package com.jlsh.aifit.feature.metabolic.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class WeightTrendResponseDto(
    val averageWeeklyChange: Double,
    val trend: String,
    val expectedWeeklyChange: Double,
    val deviationFromExpected: Double,
    val dataPoints: Int,
)


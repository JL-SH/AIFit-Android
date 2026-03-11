package com.jlsh.aifit.feature.metabolic.domain.model

data class WeightTrend(
    val averageWeeklyChange: Double,
    val trend: String,
    val expectedWeeklyChange: Double,
    val deviationFromExpected: Double,
    val dataPoints: Int,
)


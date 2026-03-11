package com.jlsh.aifit.feature.progress.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class WeightProgressResponseDto(
    val initialWeight: Double,
    val currentWeight: Double,
    val targetWeight: Double,
    val change: Double,
    val trend: String,
    val weeklyAverage: Double,
    val entries: List<WeightEntryDto> = emptyList(),
)


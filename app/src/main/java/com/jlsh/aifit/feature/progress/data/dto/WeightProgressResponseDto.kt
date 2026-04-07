package com.jlsh.aifit.feature.progress.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class WeightProgressResponseDto(
    val initialWeight: Double? = null,
    val currentWeight: Double? = null,
    val targetWeight: Double? = null,
    val change: Double? = null,
    val trend: String? = null,
    val weeklyAverage: Double? = null,
    val entries: List<WeightEntryDto> = emptyList(),
)


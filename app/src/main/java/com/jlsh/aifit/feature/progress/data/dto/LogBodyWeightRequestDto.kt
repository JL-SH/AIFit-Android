package com.jlsh.aifit.feature.progress.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class LogBodyWeightRequestDto(
    val weight: Double,
    val date: String,
    val notes: String? = null,
)

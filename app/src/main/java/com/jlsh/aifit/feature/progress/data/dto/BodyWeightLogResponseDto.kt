package com.jlsh.aifit.feature.progress.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class BodyWeightLogResponseDto(
    val id: String,
    val weight: Double,
    val date: String,
    val notes: String? = null,
    val createdAt: String,
)

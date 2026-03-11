package com.jlsh.aifit.feature.workout.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class StreakResponseDto(
    val type: String,
    val status: String,
    val currentCount: Int,
    val longestCount: Int,
    val lastActivityDate: String? = null,
    val startedAt: String? = null,
)


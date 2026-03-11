package com.jlsh.aifit.feature.gamification.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class StreakResponseDto(
    val type: String,
    val status: String,
    val currentCount: Int,
    val longestCount: Int,
    val lastActivityDate: String,
    val startedAt: String,
)


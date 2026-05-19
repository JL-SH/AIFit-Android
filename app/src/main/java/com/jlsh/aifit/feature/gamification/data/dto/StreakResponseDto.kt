package com.jlsh.aifit.feature.gamification.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class StreakResponseDto(
    val type: String = "UNKNOWN",
    val status: String = "UNKNOWN",
    val currentCount: Int = 0,
    val longestCount: Int = 0,
    val lastActivityDate: String = "",
    val startedAt: String = "",
)

package com.jlsh.aifit.feature.gamification.domain.model

import java.time.LocalDate

enum class StreakType {
    WORKOUT,
    NUTRITION,
    COMBINED,
    LOGIN,
    UNKNOWN;

    companion object {
        fun fromString(value: String?): StreakType =
            value?.let { runCatching { valueOf(it) }.getOrDefault(UNKNOWN) } ?: UNKNOWN
    }
}

enum class StreakStatus {
    ACTIVE,
    FROZEN,
    BROKEN,
    UNKNOWN;

    companion object {
        fun fromString(value: String?): StreakStatus =
            value?.let { runCatching { valueOf(it) }.getOrDefault(UNKNOWN) } ?: UNKNOWN
    }
}

data class Streak(
    val type: StreakType,
    val status: StreakStatus,
    val currentCount: Int,
    val longestCount: Int,
    val lastActivityDate: LocalDate,
    val startedAt: String,
)


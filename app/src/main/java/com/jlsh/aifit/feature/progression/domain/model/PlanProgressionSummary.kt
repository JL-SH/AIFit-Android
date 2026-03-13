package com.jlsh.aifit.feature.progression.domain.model

enum class ProgressTrend {
    IMPROVING,
    MAINTAINING,
    DECLINING,
    INSUFFICIENT_DATA,
    UNKNOWN;

    companion object {
        fun fromString(value: String?): ProgressTrend =
            value?.let { runCatching { valueOf(it) }.getOrDefault(UNKNOWN) } ?: UNKNOWN
    }
}

data class PlanProgressionSummary(
    val planId: String,
    val recommendations: List<ProgressionRecommendation>,
    val overallTrend: ProgressTrend,
    val lastAnalyzedAt: String,
)


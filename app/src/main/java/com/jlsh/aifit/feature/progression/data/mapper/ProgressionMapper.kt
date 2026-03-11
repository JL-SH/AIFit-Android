package com.jlsh.aifit.feature.progression.data.mapper

import com.jlsh.aifit.feature.progression.data.dto.PlanProgressionSummaryResponseDto
import com.jlsh.aifit.feature.progression.data.dto.ProgressionRecommendationResponseDto
import com.jlsh.aifit.feature.progression.domain.model.PlanProgressionSummary
import com.jlsh.aifit.feature.progression.domain.model.ProgressTrend
import com.jlsh.aifit.feature.progression.domain.model.ProgressionRecommendation
import com.jlsh.aifit.feature.progression.domain.model.ProgressionType

object ProgressionMapper {

    fun ProgressionRecommendationResponseDto.toDomain(): ProgressionRecommendation =
        ProgressionRecommendation(
            trainingExerciseId = trainingExerciseId,
            exerciseName = exerciseName,
            type = ProgressionType.fromString(type),
            currentLoad = currentLoad,
            suggestedLoad = suggestedLoad,
            suggestedRepsMin = suggestedRepsMin,
            suggestedRepsMax = suggestedRepsMax,
            rationale = rationale,
            confidence = confidence,
            basedOnSessions = basedOnSessions,
        )

    fun PlanProgressionSummaryResponseDto.toDomain(): PlanProgressionSummary =
        PlanProgressionSummary(
            planId = trainingPlanId,
            recommendations = recommendations.map { it.toDomain() },
            overallTrend = ProgressTrend.UNKNOWN,
            lastAnalyzedAt = "",
        )
}


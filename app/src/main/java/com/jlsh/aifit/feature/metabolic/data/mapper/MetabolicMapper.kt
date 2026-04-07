package com.jlsh.aifit.feature.metabolic.data.mapper

import com.jlsh.aifit.feature.metabolic.data.dto.MetabolicAdjustmentRecommendationResponseDto
import com.jlsh.aifit.feature.metabolic.data.dto.MetabolicAnalysisResponseDto
import com.jlsh.aifit.feature.metabolic.data.dto.MetabolicInsightResponseDto
import com.jlsh.aifit.feature.metabolic.data.dto.WeightTrendResponseDto
import com.jlsh.aifit.feature.metabolic.domain.model.AdjustmentMagnitude
import com.jlsh.aifit.feature.metabolic.domain.model.AdjustmentType
import com.jlsh.aifit.feature.metabolic.domain.model.AdjustmentUrgency
import com.jlsh.aifit.feature.metabolic.domain.model.MetabolicAdjustmentRecommendation
import com.jlsh.aifit.feature.metabolic.domain.model.MetabolicAnalysis
import com.jlsh.aifit.feature.metabolic.domain.model.MetabolicInsight
import com.jlsh.aifit.feature.metabolic.domain.model.MetabolicStatus
import com.jlsh.aifit.feature.metabolic.domain.model.WeightTrend

object MetabolicMapper {

    fun MetabolicAnalysisResponseDto.toDomain(): MetabolicAnalysis = MetabolicAnalysis(
        status = MetabolicStatus.fromString(status),
        weightTrend = weightTrend?.toDomain(),
        calorieAdherenceRate = calorieAdherenceRate,
        averageCalorieDeficitSurplus = averageCalorieDeficitSurplus,
        recommendation = recommendation?.toDomain(),
        rationale = rationale,
    )

    fun WeightTrendResponseDto.toDomain(): WeightTrend = WeightTrend(
        averageWeeklyChange = averageWeeklyChange,
        trend = trend,
        expectedWeeklyChange = expectedWeeklyChange,
        deviationFromExpected = deviationFromExpected,
        dataPoints = dataPoints,
    )

    fun MetabolicAdjustmentRecommendationResponseDto.toDomain(): MetabolicAdjustmentRecommendation =
        MetabolicAdjustmentRecommendation(
            type = AdjustmentType.fromString(type),
            suggestedCalorieTarget = suggestedCalorieTarget,
            suggestedProteinTarget = suggestedProteinTarget,
            suggestedCarbsTarget = suggestedCarbsTarget,
            suggestedFatTarget = suggestedFatTarget,
            magnitude = AdjustmentMagnitude.fromString(magnitude),
            urgency = AdjustmentUrgency.fromString(urgency),
        )

    fun MetabolicInsightResponseDto.toDomain(): MetabolicInsight = MetabolicInsight(
        id = id,
        statusAtTime = MetabolicStatus.fromString(statusAtTime),
        adjustmentType = AdjustmentType.fromString(adjustmentType),
        previousCalorieTarget = previousCalorieTarget,
        newCalorieTarget = newCalorieTarget,
        magnitude = AdjustmentMagnitude.fromString(magnitude),
        rationale = rationale,
        appliedAt = appliedAt,
    )
}


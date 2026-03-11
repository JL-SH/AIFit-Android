package com.jlsh.aifit.feature.progression.domain.repository

import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.feature.progression.domain.model.PlanProgressionSummary
import com.jlsh.aifit.feature.progression.domain.model.ProgressionRecommendation

interface ProgressionRepository {
    suspend fun getExerciseRecommendation(exerciseId: String): Result<ProgressionRecommendation>
    suspend fun getPlanRecommendations(planId: String): Result<PlanProgressionSummary>
}


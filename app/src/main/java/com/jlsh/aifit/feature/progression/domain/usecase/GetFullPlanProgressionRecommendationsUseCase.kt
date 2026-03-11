package com.jlsh.aifit.feature.progression.domain.usecase

import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.feature.progression.domain.model.PlanProgressionSummary
import com.jlsh.aifit.feature.progression.domain.repository.ProgressionRepository
import javax.inject.Inject

class GetFullPlanProgressionRecommendationsUseCase @Inject constructor(
    private val repository: ProgressionRepository,
) {
    suspend operator fun invoke(planId: String): Result<PlanProgressionSummary> =
        repository.getPlanRecommendations(planId)
}


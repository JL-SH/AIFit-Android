package com.jlsh.aifit.feature.progression.domain.usecase

import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.feature.progression.domain.model.PlanProgressionSummary
import com.jlsh.aifit.feature.progression.domain.repository.ProgressionRepository
import javax.inject.Inject

/**
 * Use case that gets progression recommendations for all exercises in a plan.
 *
 * @param repository Training progression repository.
 */
class GetFullPlanProgressionRecommendationsUseCase @Inject constructor(
    private val repository: ProgressionRepository,
) {
    /**
     * Load the full plan progression summary.
     *
     * @param planId Identifier of the training plan.
     * @return [Result.Success] with [PlanProgressionSummary], or [Result.Error] if the query fails.
     */
    suspend operator fun invoke(planId: String): Result<PlanProgressionSummary> =
        repository.getPlanRecommendations(planId)
}


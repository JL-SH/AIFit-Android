package com.jlsh.aifit.feature.training.domain.usecase

import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.feature.training.domain.model.TrainingPlan
import com.jlsh.aifit.feature.training.domain.repository.TrainingRepository
import javax.inject.Inject

/**
 * Use case to mark a plan as active and pause the previous active plan.
 */
class SetActivePlanUseCase @Inject constructor(
    private val repository: TrainingRepository,
) {
    /**
     * Activate the indicated plan on the server and local cache.
     *
     * @param planId Identifier of the plan to activate.
     * @return [Result.Success] with plan activated, or [Result.Error] (e.g. plan not found).
     */
    suspend operator fun invoke(planId: String): Result<TrainingPlan> =
        repository.activatePlan(planId)
}

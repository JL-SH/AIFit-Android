package com.jlsh.aifit.feature.training.domain.usecase

import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.feature.training.domain.repository.TrainingRepository
import javax.inject.Inject

/**
 * Use case to delete a training plan from the server and local cache.
 */
class DeleteTrainingPlanUseCase @Inject constructor(
    private val repository: TrainingRepository,
) {
    /**
     * Deletes the plan identified by [planId].
     *
     * @param planId Identifier of the plan to delete.
     * @return [Result.Success] if the delete succeeds, or [Result.Error] if it fails.
     */
    suspend operator fun invoke(planId: String): Result<Unit> =
        repository.deleteTrainingPlan(planId)
}

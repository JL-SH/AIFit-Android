package com.jlsh.aifit.feature.diet.domain.usecase

import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.feature.diet.domain.repository.DietRepository
import javax.inject.Inject

/**
 * Use case that deletes a user's diet plan in server and local cache.
 *
 * @param repository Diet plan repository.
 */
class DeleteDietPlanUseCase @Inject constructor(
    private val repository: DietRepository,
) {
    /**
     * Deletes the plan identified by [planId].
     *
     * @param planId Identifier of the plan to delete.
     * @return [Result.Success] if the delete succeeds, or [Result.Error] if it fails.
     */
    suspend operator fun invoke(planId: String): Result<Unit> =
        repository.deleteDietPlan(planId)
}

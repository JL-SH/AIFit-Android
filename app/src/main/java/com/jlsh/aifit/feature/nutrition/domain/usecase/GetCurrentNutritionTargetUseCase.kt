package com.jlsh.aifit.feature.nutrition.domain.usecase

import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.feature.nutrition.domain.model.NutritionTarget
import com.jlsh.aifit.feature.nutrition.domain.repository.NutritionTargetRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case that obtains the user's current nutritional goals.
 *
 * @param repository Nutritional goals repository.
 */
class GetCurrentNutritionTargetUseCase @Inject constructor(
    private val repository: NutritionTargetRepository,
) {
    /**
     * Outputs current goal (calories and macros) from cache and/or network.
     *
     * @return Flow of [Result] with [NutritionTarget], [Result.Loading], or [Result.Error].
     */
    operator fun invoke(): Flow<Result<NutritionTarget>> =
        repository.getCurrentTarget()
}

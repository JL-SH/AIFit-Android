package com.jlsh.aifit.feature.training.domain.usecase

import com.jlsh.aifit.feature.training.domain.repository.TrainingRepository
import javax.inject.Inject

/**
 * Warms the JSON detail cache for plans that are not cached yet (no-op when already present).
 */
class PrefetchTrainingPlanDetailsUseCase @Inject constructor(
    private val repository: TrainingRepository,
) {
    suspend operator fun invoke(planIds: List<String>) =
        repository.prefetchTrainingPlanDetailsIfMissing(planIds)
}

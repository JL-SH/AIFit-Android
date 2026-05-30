package com.jlsh.aifit.feature.training.domain.usecase

import com.jlsh.aifit.feature.training.domain.model.TrainingPlan
import com.jlsh.aifit.feature.training.domain.repository.TrainingRepository
import javax.inject.Inject

/**
 * Reads training plans from Room only (no network).
 */
class GetCachedTrainingPlansUseCase @Inject constructor(
    private val repository: TrainingRepository,
) {
    suspend operator fun invoke(): List<TrainingPlan> = repository.getCachedTrainingPlans()
}

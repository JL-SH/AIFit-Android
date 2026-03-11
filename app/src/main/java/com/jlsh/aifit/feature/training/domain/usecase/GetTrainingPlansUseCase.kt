package com.jlsh.aifit.feature.training.domain.usecase

import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.feature.training.domain.model.TrainingPlan
import com.jlsh.aifit.feature.training.domain.repository.TrainingRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetTrainingPlansUseCase @Inject constructor(
    private val repository: TrainingRepository,
) {
    operator fun invoke(): Flow<Result<List<TrainingPlan>>> =
        repository.getTrainingPlans()
}


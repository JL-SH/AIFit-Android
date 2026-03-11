package com.jlsh.aifit.feature.progression.domain.usecase

import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.feature.progression.domain.model.ProgressionRecommendation
import com.jlsh.aifit.feature.progression.domain.repository.ProgressionRepository
import javax.inject.Inject

class GetExerciseProgressionRecommendationUseCase @Inject constructor(
    private val repository: ProgressionRepository,
) {
    suspend operator fun invoke(exerciseId: String): Result<ProgressionRecommendation> =
        repository.getExerciseRecommendation(exerciseId)
}


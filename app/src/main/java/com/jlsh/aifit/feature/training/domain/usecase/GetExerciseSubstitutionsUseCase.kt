package com.jlsh.aifit.feature.training.domain.usecase

import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.feature.training.domain.model.ExerciseSubstitution
import com.jlsh.aifit.feature.training.domain.repository.TrainingRepository
import javax.inject.Inject

class GetExerciseSubstitutionsUseCase @Inject constructor(
    private val repository: TrainingRepository,
) {
    suspend operator fun invoke(exerciseId: String): Result<List<ExerciseSubstitution>> =
        repository.getExerciseSubstitutions(exerciseId)
}


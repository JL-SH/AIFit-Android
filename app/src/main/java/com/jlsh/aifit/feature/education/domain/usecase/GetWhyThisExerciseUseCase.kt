package com.jlsh.aifit.feature.education.domain.usecase

import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.feature.education.domain.model.WhyThisExplanation
import com.jlsh.aifit.feature.education.domain.repository.EducationRepository
import javax.inject.Inject

class GetWhyThisExerciseUseCase @Inject constructor(
    private val repository: EducationRepository,
) {
    suspend operator fun invoke(exerciseId: String): Result<WhyThisExplanation> =
        repository.getWhyThisExercise(exerciseId)
}


package com.jlsh.aifit.feature.education.domain.usecase

import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.feature.education.domain.model.ContextualExplanation
import com.jlsh.aifit.feature.education.domain.repository.EducationRepository
import javax.inject.Inject

class GetMealExplanationUseCase @Inject constructor(
    private val repository: EducationRepository,
) {
    suspend operator fun invoke(mealId: String): Result<ContextualExplanation> =
        repository.getMealExplanation(mealId)
}


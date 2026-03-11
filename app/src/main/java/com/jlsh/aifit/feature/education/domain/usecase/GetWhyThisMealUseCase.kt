package com.jlsh.aifit.feature.education.domain.usecase

import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.feature.education.domain.model.WhyThisExplanation
import com.jlsh.aifit.feature.education.domain.repository.EducationRepository
import javax.inject.Inject

class GetWhyThisMealUseCase @Inject constructor(
    private val repository: EducationRepository,
) {
    suspend operator fun invoke(mealId: String): Result<WhyThisExplanation> =
        repository.getWhyThisMeal(mealId)
}


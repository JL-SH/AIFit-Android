package com.jlsh.aifit.feature.education.domain.usecase

import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.feature.education.domain.model.GlossaryDefinition
import com.jlsh.aifit.feature.education.domain.repository.EducationRepository
import javax.inject.Inject

class GetGlossaryTermUseCase @Inject constructor(
    private val repository: EducationRepository,
) {
    suspend operator fun invoke(term: String): Result<GlossaryDefinition> =
        repository.getGlossaryTerm(term)
}


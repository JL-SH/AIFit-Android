package com.jlsh.aifit.feature.education.domain.usecase

import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.feature.education.domain.repository.EducationRepository
import javax.inject.Inject

class UpdateKnowledgeLevelUseCase @Inject constructor(
    private val repository: EducationRepository,
) {
    suspend operator fun invoke(level: String): Result<String> =
        repository.updateKnowledgeLevel(level)
}


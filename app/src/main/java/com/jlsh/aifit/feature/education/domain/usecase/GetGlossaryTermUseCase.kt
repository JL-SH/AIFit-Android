package com.jlsh.aifit.feature.education.domain.usecase

import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.feature.education.domain.model.GlossaryDefinition
import com.jlsh.aifit.feature.education.domain.repository.EducationRepository
import javax.inject.Inject

/**
 * Use case that queries the definition of a term from the educational glossary.
 *
 * @param repository Educational content repository.
 */
class GetGlossaryTermUseCase @Inject constructor(
    private val repository: EducationRepository,
) {
    /**
     * Look up the definition of the indicated term.
     *
     * @param term Term to query (e.g. "Progressive Overload").
     * @return [Result.Success] with the definition and related terms, or [Result.Error] if not found or network fails.
     */
    suspend operator fun invoke(term: String): Result<GlossaryDefinition> =
        repository.getGlossaryTerm(term)
}


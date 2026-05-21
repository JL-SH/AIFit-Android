package com.jlsh.aifit.feature.education.domain.usecase

import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.feature.education.domain.model.GlossaryDefinition
import com.jlsh.aifit.feature.education.domain.repository.EducationRepository
import javax.inject.Inject

/**
 * Caso de uso que consulta la definición de un término del glosario educativo.
 *
 * @param repository Repositorio de contenido educativo.
 */
class GetGlossaryTermUseCase @Inject constructor(
    private val repository: EducationRepository,
) {
    /**
     * Busca la definición del término indicado.
     *
     * @param term Término a consultar (p. ej. "Progressive Overload").
     * @return [Result.Success] con la definición y términos relacionados, o [Result.Error] si no se encuentra o falla la red.
     */
    suspend operator fun invoke(term: String): Result<GlossaryDefinition> =
        repository.getGlossaryTerm(term)
}


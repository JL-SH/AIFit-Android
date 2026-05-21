package com.jlsh.aifit.feature.metabolic.domain.usecase

import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.feature.metabolic.domain.model.MetabolicAnalysis
import com.jlsh.aifit.feature.metabolic.domain.repository.MetabolicRepository
import javax.inject.Inject

/**
 * Caso de uso que solicita el análisis metabólico actual del usuario.
 *
 * @param repository Repositorio de datos metabólicos.
 */
class AnalyzeMetabolicProgressUseCase @Inject constructor(
    private val repository: MetabolicRepository,
) {
    /**
     * Obtiene tendencia de peso, adherencia calórica y recomendación de ajuste.
     *
     * @return [Result.Success] con [MetabolicAnalysis], o [Result.Error] (p. ej. datos insuficientes).
     */
    suspend operator fun invoke(): Result<MetabolicAnalysis> =
        repository.analyzeMetabolicProgress()
}


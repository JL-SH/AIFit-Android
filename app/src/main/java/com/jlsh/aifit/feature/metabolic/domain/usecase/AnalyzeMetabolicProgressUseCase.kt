package com.jlsh.aifit.feature.metabolic.domain.usecase

import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.feature.metabolic.domain.model.MetabolicAnalysis
import com.jlsh.aifit.feature.metabolic.domain.repository.MetabolicRepository
import javax.inject.Inject

/**
 * Use case that requests the user's current metabolic analysis.
 *
 * @param repository Metabolic data repository.
 */
class AnalyzeMetabolicProgressUseCase @Inject constructor(
    private val repository: MetabolicRepository,
) {
    /**
     * Get weight trend, calorie adherence and fit recommendation.
     *
     * @return [Result.Success] with [MetabolicAnalysis], or [Result.Error] (e.g. insufficient data).
     */
    suspend operator fun invoke(): Result<MetabolicAnalysis> =
        repository.analyzeMetabolicProgress()
}


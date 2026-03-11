package com.jlsh.aifit.feature.metabolic.domain.usecase

import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.feature.metabolic.domain.model.MetabolicAnalysis
import com.jlsh.aifit.feature.metabolic.domain.repository.MetabolicRepository
import javax.inject.Inject

class AnalyzeMetabolicProgressUseCase @Inject constructor(
    private val repository: MetabolicRepository,
) {
    suspend operator fun invoke(): Result<MetabolicAnalysis> =
        repository.analyzeMetabolicProgress()
}


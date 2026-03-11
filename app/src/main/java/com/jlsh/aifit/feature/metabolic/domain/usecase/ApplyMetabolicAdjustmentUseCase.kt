package com.jlsh.aifit.feature.metabolic.domain.usecase

import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.feature.metabolic.data.dto.ApplyMetabolicAdjustmentRequestDto
import com.jlsh.aifit.feature.metabolic.domain.repository.MetabolicRepository
import com.jlsh.aifit.feature.nutrition.domain.model.NutritionTarget
import javax.inject.Inject

class ApplyMetabolicAdjustmentUseCase @Inject constructor(
    private val repository: MetabolicRepository,
) {
    suspend operator fun invoke(request: ApplyMetabolicAdjustmentRequestDto): Result<NutritionTarget> =
        repository.applyAdjustment(request)
}


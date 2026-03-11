package com.jlsh.aifit.feature.nutrition.domain.usecase

import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.feature.nutrition.data.dto.UpdateNutritionTargetRequestDto
import com.jlsh.aifit.feature.nutrition.domain.model.NutritionTarget
import com.jlsh.aifit.feature.nutrition.domain.repository.NutritionTargetRepository
import javax.inject.Inject

class UpdateNutritionTargetUseCase @Inject constructor(
    private val repository: NutritionTargetRepository,
) {
    suspend operator fun invoke(request: UpdateNutritionTargetRequestDto): Result<NutritionTarget> =
        repository.updateTarget(request)
}


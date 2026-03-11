package com.jlsh.aifit.feature.nutrition.domain.repository

import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.feature.nutrition.data.dto.UpdateNutritionTargetRequestDto
import com.jlsh.aifit.feature.nutrition.domain.model.NutritionTarget
import kotlinx.coroutines.flow.Flow

interface NutritionTargetRepository {
    fun getCurrentTarget(): Flow<Result<NutritionTarget>>
    suspend fun updateTarget(request: UpdateNutritionTargetRequestDto): Result<NutritionTarget>
}


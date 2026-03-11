package com.jlsh.aifit.feature.metabolic.domain.repository

import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.feature.metabolic.data.dto.ApplyMetabolicAdjustmentRequestDto
import com.jlsh.aifit.feature.metabolic.domain.model.MetabolicAnalysis
import com.jlsh.aifit.feature.metabolic.domain.model.MetabolicInsight
import com.jlsh.aifit.feature.nutrition.domain.model.NutritionTarget

interface MetabolicRepository {
    suspend fun analyzeMetabolicProgress(): Result<MetabolicAnalysis>
    suspend fun getInsights(): Result<List<MetabolicInsight>>
    suspend fun applyAdjustment(request: ApplyMetabolicAdjustmentRequestDto): Result<NutritionTarget>
}


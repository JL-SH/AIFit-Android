package com.jlsh.aifit.feature.diet.domain.repository

import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.feature.diet.data.dto.GenerateAdaptiveDietPlanRequestDto
import com.jlsh.aifit.feature.diet.data.dto.GenerateDietPlanRequestDto
import com.jlsh.aifit.feature.diet.domain.model.DietPlan
import kotlinx.coroutines.flow.Flow

interface DietRepository {
    fun getDietPlans(): Flow<Result<List<DietPlan>>>
    suspend fun getDietPlanDetail(planId: String): Result<DietPlan>
    suspend fun generateDietPlan(request: GenerateDietPlanRequestDto): Result<DietPlan>
    suspend fun generateAdaptiveDietPlan(request: GenerateAdaptiveDietPlanRequestDto): Result<DietPlan>
    suspend fun deleteDietPlan(planId: String): Result<Unit>
}


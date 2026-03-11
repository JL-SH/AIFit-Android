package com.jlsh.aifit.feature.training.domain.repository

import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.feature.training.data.dto.GenerateAdaptiveTrainingPlanRequestDto
import com.jlsh.aifit.feature.training.data.dto.GenerateTrainingPlanRequestDto
import com.jlsh.aifit.feature.training.domain.model.TrainingPlan
import kotlinx.coroutines.flow.Flow

interface TrainingRepository {
    fun getTrainingPlans(): Flow<Result<List<TrainingPlan>>>
    suspend fun getTrainingPlanDetail(planId: String): Result<TrainingPlan>
    suspend fun generateTrainingPlan(request: GenerateTrainingPlanRequestDto): Result<TrainingPlan>
    suspend fun generateAdaptiveTrainingPlan(request: GenerateAdaptiveTrainingPlanRequestDto): Result<TrainingPlan>
    suspend fun deleteTrainingPlan(planId: String): Result<Unit>
}


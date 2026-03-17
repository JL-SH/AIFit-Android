package com.jlsh.aifit.feature.training.data.api

import com.jlsh.aifit.core.network.ApiResponse
import com.jlsh.aifit.feature.training.data.dto.ExerciseSubstitutionResponseDto
import com.jlsh.aifit.feature.training.data.dto.GenerateAdaptiveTrainingPlanRequestDto
import com.jlsh.aifit.feature.training.data.dto.GenerateTrainingPlanRequestDto
import com.jlsh.aifit.feature.training.data.dto.TrainingPlanResponseDto
import com.jlsh.aifit.feature.training.data.dto.TrainingPlanSummaryResponseDto
import com.jlsh.aifit.feature.training.data.dto.WarmUpProtocolResponseDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface TrainingApiService {

    @GET("training-plans")
    suspend fun getTrainingPlans(): ApiResponse<List<TrainingPlanSummaryResponseDto>>

    @GET("training-plans/{id}")
    suspend fun getTrainingPlanById(@Path("id") id: String): ApiResponse<TrainingPlanResponseDto>

    @POST("training-plans/generate")
    suspend fun generateTrainingPlan(
        @Body request: GenerateTrainingPlanRequestDto
    ): ApiResponse<TrainingPlanResponseDto>

    @POST("training-plans/generate-adaptive")
    suspend fun generateAdaptiveTrainingPlan(
        @Body request: GenerateAdaptiveTrainingPlanRequestDto
    ): ApiResponse<TrainingPlanResponseDto>

    @DELETE("training-plans/{id}")
    suspend fun deleteTrainingPlan(@Path("id") id: String): ApiResponse<Unit>

    @GET("training-plans/{planId}/days/{dayId}/warmup")
    suspend fun getWarmUpProtocol(
        @Path("planId") planId: String,
        @Path("dayId") dayId: String,
    ): ApiResponse<WarmUpProtocolResponseDto>

    @GET("training/exercises/{exerciseId}/substitutions")
    suspend fun getExerciseSubstitutions(
        @Path("exerciseId") exerciseId: String,
    ): ApiResponse<List<ExerciseSubstitutionResponseDto>>
}


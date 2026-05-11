package com.jlsh.aifit.feature.diet.data.api

import com.jlsh.aifit.core.network.ApiResponse
import com.jlsh.aifit.feature.diet.data.dto.DietPlanResponseDto
import com.jlsh.aifit.feature.diet.data.dto.DietPlanSummaryResponseDto
import com.jlsh.aifit.feature.diet.data.dto.GenerateAdaptiveDietPlanRequestDto
import com.jlsh.aifit.feature.diet.data.dto.GenerateDietPlanRequestDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface DietApiService {

    @GET("diet-plans")
    suspend fun getDietPlans(): ApiResponse<List<DietPlanSummaryResponseDto>>

    @GET("diet-plans/{id}")
    suspend fun getDietPlanById(@Path("id") id: String): ApiResponse<DietPlanResponseDto>

    @POST("diet-plans/generate")
    suspend fun generateDietPlan(
        @Body request: GenerateDietPlanRequestDto,
    ): ApiResponse<DietPlanResponseDto>

    @POST("diet-plans/generate-adaptive")
    suspend fun generateAdaptiveDietPlan(
        @Body request: GenerateAdaptiveDietPlanRequestDto,
    ): ApiResponse<DietPlanResponseDto>

    @PATCH("diet-plans/{id}/activate")
    suspend fun activateDietPlan(@Path("id") planId: String): ApiResponse<DietPlanResponseDto>

    @PATCH("diet-plans/{id}/pause")
    suspend fun pauseDietPlan(@Path("id") planId: String): ApiResponse<DietPlanResponseDto>

    @DELETE("diet-plans/{id}")
    suspend fun deleteDietPlan(@Path("id") id: String): ApiResponse<Unit>
}


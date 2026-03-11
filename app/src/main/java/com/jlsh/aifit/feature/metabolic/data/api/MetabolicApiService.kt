package com.jlsh.aifit.feature.metabolic.data.api

import com.jlsh.aifit.core.network.ApiResponse
import com.jlsh.aifit.feature.metabolic.data.dto.ApplyMetabolicAdjustmentRequestDto
import com.jlsh.aifit.feature.metabolic.data.dto.MetabolicAnalysisResponseDto
import com.jlsh.aifit.feature.metabolic.data.dto.MetabolicInsightResponseDto
import com.jlsh.aifit.feature.nutrition.data.dto.NutritionTargetResponseDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface MetabolicApiService {

    @GET("metabolic/analysis")
    suspend fun getAnalysis(): ApiResponse<MetabolicAnalysisResponseDto>

    @GET("metabolic/insights")
    suspend fun getInsights(): ApiResponse<List<MetabolicInsightResponseDto>>

    @POST("metabolic/adjustments")
    suspend fun applyAdjustment(
        @Body request: ApplyMetabolicAdjustmentRequestDto,
    ): ApiResponse<NutritionTargetResponseDto>
}


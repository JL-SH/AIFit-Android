package com.jlsh.aifit.feature.progression.data.api

import com.jlsh.aifit.core.network.ApiResponse
import com.jlsh.aifit.feature.progression.data.dto.PlanProgressionSummaryResponseDto
import com.jlsh.aifit.feature.progression.data.dto.ProgressionRecommendationResponseDto
import retrofit2.http.GET
import retrofit2.http.Path

interface ProgressionApiService {

    @GET("progression/exercises/{exerciseId}")
    suspend fun getExerciseRecommendation(
        @Path("exerciseId") exerciseId: String,
    ): ApiResponse<ProgressionRecommendationResponseDto>

    @GET("progression/plans/{planId}")
    suspend fun getPlanRecommendations(
        @Path("planId") planId: String,
    ): ApiResponse<PlanProgressionSummaryResponseDto>
}


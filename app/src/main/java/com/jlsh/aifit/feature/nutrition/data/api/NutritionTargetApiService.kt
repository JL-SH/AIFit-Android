package com.jlsh.aifit.feature.nutrition.data.api

import com.jlsh.aifit.core.network.ApiResponse
import com.jlsh.aifit.feature.nutrition.data.dto.NutritionTargetResponseDto
import com.jlsh.aifit.feature.nutrition.data.dto.UpdateNutritionTargetRequestDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT

interface NutritionTargetApiService {

    @GET("nutrition-targets/current")
    suspend fun getCurrentTarget(): ApiResponse<NutritionTargetResponseDto>

    @PUT("nutrition-targets")
    suspend fun updateTarget(
        @Body request: UpdateNutritionTargetRequestDto,
    ): ApiResponse<NutritionTargetResponseDto>
}


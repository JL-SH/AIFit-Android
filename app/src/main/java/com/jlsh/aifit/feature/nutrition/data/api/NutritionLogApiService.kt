package com.jlsh.aifit.feature.nutrition.data.api

import com.jlsh.aifit.core.network.ApiResponse
import com.jlsh.aifit.feature.nutrition.data.dto.AnalyzeMealFromTextRequestDto
import com.jlsh.aifit.feature.nutrition.data.dto.MealLogResponseDto
import com.jlsh.aifit.feature.nutrition.data.dto.NutritionLogResponseDto
import com.jlsh.aifit.feature.nutrition.data.dto.TrackMealRequestDto
import com.jlsh.aifit.feature.nutrition.data.dto.UpdateMealRequestDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.PATCH
import retrofit2.http.Query

interface NutritionLogApiService {

    @GET("nutrition-logs/{date}")
    suspend fun getNutritionLog(@Path("date") date: String): ApiResponse<NutritionLogResponseDto>

    @GET("nutrition-logs")
    suspend fun getNutritionHistory(
        @Query("from") from: String,
        @Query("to") to: String,
    ): ApiResponse<List<NutritionLogResponseDto>>

    @POST("nutrition-logs/meals")
    suspend fun trackMeal(@Body request: TrackMealRequestDto): ApiResponse<MealLogResponseDto>

    @POST("nutrition-logs/meals/analyze")
    suspend fun analyzeMealFromText(@Body request: AnalyzeMealFromTextRequestDto): ApiResponse<MealLogResponseDto>

    @DELETE("nutrition-logs/meals/{mealId}")
    suspend fun deleteMealLog(@Path("mealId") mealId: String): ApiResponse<Unit>

    @PATCH("nutrition-logs/meals/{mealId}")
    suspend fun updateMealLog(
        @Path("mealId") mealId: String,
        @Body request: UpdateMealRequestDto,
    ): ApiResponse<MealLogResponseDto>
}


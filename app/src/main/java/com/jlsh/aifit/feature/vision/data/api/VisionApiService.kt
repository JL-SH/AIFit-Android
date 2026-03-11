package com.jlsh.aifit.feature.vision.data.api

import com.jlsh.aifit.core.network.ApiResponse
import com.jlsh.aifit.feature.vision.data.dto.FoodPhotoAnalysisResponseDto
import okhttp3.MultipartBody
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface VisionApiService {

    @Multipart
    @POST("food-vision/analyze")
    suspend fun analyzePhoto(
        @Part image: MultipartBody.Part,
    ): ApiResponse<FoodPhotoAnalysisResponseDto>
}


package com.jlsh.aifit.feature.progress.data.api

import com.jlsh.aifit.core.network.ApiResponse
import com.jlsh.aifit.feature.progress.data.dto.BodyWeightLogResponseDto
import com.jlsh.aifit.feature.progress.data.dto.LogBodyWeightRequestDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface BodyWeightApiService {

    @GET("body-weight")
    suspend fun getHistory(
        @Query("from") from: String,
        @Query("to") to: String,
    ): ApiResponse<List<BodyWeightLogResponseDto>>

    @POST("body-weight")
    suspend fun logWeight(@Body request: LogBodyWeightRequestDto): ApiResponse<BodyWeightLogResponseDto>
}

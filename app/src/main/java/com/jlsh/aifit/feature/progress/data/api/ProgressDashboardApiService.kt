package com.jlsh.aifit.feature.progress.data.api

import com.jlsh.aifit.core.network.ApiResponse
import com.jlsh.aifit.feature.progress.data.dto.ProgressDashboardResponseDto
import com.jlsh.aifit.feature.progress.data.dto.WeeklyProgressSummaryResponseDto
import retrofit2.http.GET
import retrofit2.http.Query

interface ProgressDashboardApiService {

    @GET("progress/dashboard")
    suspend fun getDashboard(
        @Query("from") from: String,
        @Query("to") to: String,
    ): ApiResponse<ProgressDashboardResponseDto>

    @GET("progress/weekly-summary")
    suspend fun getWeeklySummary(): ApiResponse<WeeklyProgressSummaryResponseDto>
}

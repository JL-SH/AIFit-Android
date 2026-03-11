package com.jlsh.aifit.feature.workout.data.api

import com.jlsh.aifit.core.network.ApiResponse
import com.jlsh.aifit.feature.workout.data.dto.ExerciseProgressionResponseDto
import com.jlsh.aifit.feature.workout.data.dto.LogWorkoutSessionRequestDto
import com.jlsh.aifit.feature.workout.data.dto.WorkoutLogResponseDto
import com.jlsh.aifit.feature.workout.data.dto.WorkoutLogSummaryResponseDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface WorkoutApiService {

    @POST("workout-logs")
    suspend fun logWorkoutSession(
        @Body request: LogWorkoutSessionRequestDto,
    ): ApiResponse<WorkoutLogResponseDto>

    @GET("workout-logs")
    suspend fun getWorkoutLogs(
        @Query("planId") planId: String? = null,
        @Query("from") from: String? = null,
        @Query("to") to: String? = null,
    ): ApiResponse<List<WorkoutLogSummaryResponseDto>>

    @GET("workout-logs/{id}")
    suspend fun getWorkoutLogById(@Path("id") id: String): ApiResponse<WorkoutLogResponseDto>

    @DELETE("workout-logs/{id}")
    suspend fun deleteWorkoutLog(@Path("id") id: String): ApiResponse<Unit>

    @GET("workout-logs/exercises/{exerciseId}/progression")
    suspend fun getExerciseProgression(
        @Path("exerciseId") exerciseId: String,
    ): ApiResponse<ExerciseProgressionResponseDto>
}


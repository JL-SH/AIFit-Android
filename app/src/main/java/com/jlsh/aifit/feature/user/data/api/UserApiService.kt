package com.jlsh.aifit.feature.user.data.api

import com.jlsh.aifit.core.network.ApiResponse
import com.jlsh.aifit.feature.user.data.dto.CreateUserProfileRequestDto
import com.jlsh.aifit.feature.user.data.dto.OnboardingFeedbackRequestDto
import com.jlsh.aifit.feature.user.data.dto.OnboardingResultDto
import com.jlsh.aifit.feature.user.data.dto.UpdateUserProfileRequestDto
import com.jlsh.aifit.feature.user.data.dto.UserProfileResponseDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT

interface UserApiService {

    @GET("users/me")
    suspend fun getProfile(): ApiResponse<UserProfileResponseDto>

    @POST("users/me")
    suspend fun createProfile(
        @Body request: CreateUserProfileRequestDto,
    ): ApiResponse<UserProfileResponseDto>

    @PUT("users/me")
    suspend fun updateProfile(
        @Body request: UpdateUserProfileRequestDto,
    ): ApiResponse<UserProfileResponseDto>

    @POST("onboarding/complete")
    suspend fun completeOnboarding(
        @Body request: OnboardingFeedbackRequestDto? = null,
    ): ApiResponse<OnboardingResultDto>
}


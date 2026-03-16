package com.jlsh.aifit.feature.user.domain.repository

import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.feature.user.domain.model.CreateUserProfileRequest
import com.jlsh.aifit.feature.user.domain.model.OnboardingResult
import com.jlsh.aifit.feature.user.domain.model.UpdateUserProfileRequest
import com.jlsh.aifit.feature.user.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun getProfile(): Flow<Result<UserProfile>>
    suspend fun createProfile(request: CreateUserProfileRequest): Result<UserProfile>
    suspend fun updateProfile(request: UpdateUserProfileRequest): Result<UserProfile>
    suspend fun completeOnboarding(feedback: String? = null): Result<OnboardingResult>
}


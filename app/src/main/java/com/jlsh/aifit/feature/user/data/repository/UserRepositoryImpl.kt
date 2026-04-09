package com.jlsh.aifit.feature.user.data.repository

import android.util.Log
import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.core.datastore.AuthDataStore
import com.jlsh.aifit.core.network.BaseRemoteDataSource
import com.jlsh.aifit.feature.user.data.api.UserApiService
import com.jlsh.aifit.feature.user.data.dto.OnboardingFeedbackRequestDto
import com.jlsh.aifit.feature.user.data.local.UserProfileDao
import com.jlsh.aifit.feature.user.data.mapper.UserMapper.toDomain
import com.jlsh.aifit.feature.user.data.mapper.UserMapper.toDto
import com.jlsh.aifit.feature.user.data.mapper.UserMapper.toEntity
import com.jlsh.aifit.feature.user.domain.model.CreateUserProfileRequest
import com.jlsh.aifit.feature.user.domain.model.OnboardingResult
import com.jlsh.aifit.feature.user.domain.model.UpdateUserProfileRequest
import com.jlsh.aifit.feature.user.domain.model.UserProfile
import com.jlsh.aifit.feature.user.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val apiService: UserApiService,
    private val dao: UserProfileDao,
    private val authDataStore: AuthDataStore,
) : BaseRemoteDataSource(), UserRepository {

    override fun getProfile(): Flow<Result<UserProfile>> = flow {
        emit(Result.Loading)

        // BUG-008 fix: use the real userId from AuthDataStore for cache lookup,
        // because entities are stored with the actual UUID, not "me".
        val userId = authDataStore.getUserId()
        val cached = if (userId != null) dao.getById(userId) else null
        if (cached != null) {
            Log.d("AIFIT_DEBUG", "getProfile: cache HIT for userId=$userId")
            emit(Result.Success(cached.toDomain()))
        } else {
            Log.d("AIFIT_DEBUG", "getProfile: cache MISS (userId=$userId)")
        }

        when (val remote = safeApiCall { apiService.getProfile() }) {
            is Result.Success -> {
                val profile = remote.data.toDomain()
                dao.upsert(profile.toEntity())
                Log.d("AIFIT_DEBUG", "getProfile: API success, profile=${profile.id}")
                emit(Result.Success(profile))
            }
            is Result.Error -> {
                Log.w("AIFIT_DEBUG", "getProfile: API error=${remote.exception.message}")
                if (cached == null) emit(remote)
            }
            is Result.Loading -> Unit
        }
    }

    override suspend fun createProfile(request: CreateUserProfileRequest): Result<UserProfile> {
        return when (val result = safeApiCall { apiService.createProfile(request.toDto()) }) {
            is Result.Success -> {
                val profile = result.data.toDomain()
                dao.upsert(profile.toEntity())
                Result.Success(profile)
            }
            is Result.Error -> result
            is Result.Loading -> Result.Loading
        }
    }

    override suspend fun updateProfile(request: UpdateUserProfileRequest): Result<UserProfile> {
        return when (val result = safeApiCall { apiService.updateProfile(request.toDto()) }) {
            is Result.Success -> {
                val profile = result.data.toDomain()
                dao.upsert(profile.toEntity())
                Result.Success(profile)
            }
            is Result.Error -> result
            is Result.Loading -> Result.Loading
        }
    }

    override suspend fun completeOnboarding(feedback: String?): Result<OnboardingResult> {
        val request = OnboardingFeedbackRequestDto(feedback)
        return when (val result = safeApiCall { apiService.completeOnboarding(request) }) {
            is Result.Success -> Result.Success(result.data.toDomain())
            is Result.Error -> result
            is Result.Loading -> Result.Loading
        }
    }
}


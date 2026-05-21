package com.jlsh.aifit.feature.user.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.jlsh.aifit.core.common.AppException
import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.core.datastore.AuthDataStore
import com.jlsh.aifit.core.network.BaseRemoteDataSource
import com.jlsh.aifit.feature.user.data.api.UserApiService
import com.jlsh.aifit.feature.user.data.dto.OnboardingFeedbackRequestDto
import com.jlsh.aifit.feature.user.data.dto.UserProfileResponseDto
import com.jlsh.aifit.feature.user.data.local.UserProfileDao
import com.jlsh.aifit.feature.user.data.mapper.UserMapper.pickBestProfilePictureUrl
import com.jlsh.aifit.feature.user.data.mapper.UserMapper.toDomain
import com.jlsh.aifit.feature.user.data.mapper.UserMapper.toDto
import com.jlsh.aifit.feature.user.data.mapper.UserMapper.toEntity
import com.jlsh.aifit.feature.user.domain.model.CreateUserProfileRequest
import com.jlsh.aifit.feature.user.domain.model.OnboardingResult
import com.jlsh.aifit.feature.user.domain.model.UpdateUserProfileRequest
import com.jlsh.aifit.feature.user.domain.model.UserProfile
import com.jlsh.aifit.feature.user.domain.repository.UserRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val apiService: UserApiService,
    private val dao: UserProfileDao,
    private val authDataStore: AuthDataStore,
    @ApplicationContext private val context: Context,
) : BaseRemoteDataSource(), UserRepository {

    override fun getProfile(): Flow<Result<UserProfile>> = flow {
        emit(Result.Loading)

        val userId = authDataStore.getUserId()
        val cached = if (userId != null) dao.getById(userId) else null
        val persistedAvatarUrl = authDataStore.getAvatarUrl(userId)
        val cachedPictureUrl = cached?.profilePictureUrl?.takeIf { it.isNotBlank() }

        if (cached != null) {
            val cachedProfile = cached.toDomain().copy(
                profilePictureUrl = pickBestProfilePictureUrl(
                    cached.profilePictureUrl,
                    cachedPictureUrl,
                    persistedAvatarUrl,
                ),
            )
            Log.d(
                "AIFIT_DEBUG",
                "getProfile: cache HIT for userId=$userId avatarUrl=${cachedProfile.profilePictureUrl}",
            )
            emit(Result.Success(cachedProfile))
        } else {
            Log.d("AIFIT_DEBUG", "getProfile: cache MISS (userId=$userId)")
        }

        when (val remote = safeApiCall { apiService.getProfile() }) {
            is Result.Success -> {
                val profile = mergeProfileFromApi(
                    dto = remote.data,
                    cachedPictureUrl = cachedPictureUrl,
                    persistedAvatarUrl = persistedAvatarUrl,
                )
                persistAvatarUrlIfBetter(userId, profile.profilePictureUrl)
                dao.upsert(profile.toEntity())
                Log.d(
                    "AIFIT_DEBUG",
                    "getProfile: API success, profile=${profile.id} avatarUrl=${profile.profilePictureUrl}" +
                        " dtoPicture=${remote.data.profilePictureUrl} dtoImage=${remote.data.profileImageUrl}",
                )
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
                val userId = authDataStore.getUserId()
                val profile = mergeProfileFromApi(
                    dto = result.data,
                    cachedPictureUrl = userId?.let { dao.getById(it)?.profilePictureUrl },
                    persistedAvatarUrl = authDataStore.getAvatarUrl(userId),
                )
                persistProfile(profile)
                Result.Success(profile)
            }
            is Result.Error -> result
            is Result.Loading -> Result.Loading
        }
    }

    override suspend fun updateProfile(request: UpdateUserProfileRequest): Result<UserProfile> {
        return when (val result = safeApiCall { apiService.updateProfile(request.toDto()) }) {
            is Result.Success -> {
                val profile = mergeProfileFromApi(
                    dto = result.data,
                    cachedPictureUrl = authDataStore.getUserId()?.let { dao.getById(it)?.profilePictureUrl },
                    persistedAvatarUrl = authDataStore.getAvatarUrl(authDataStore.getUserId()),
                )
                persistProfile(profile)
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

    override suspend fun uploadProfilePhoto(uri: Uri): Result<UserProfile> {
        val inputStream = context.contentResolver.openInputStream(uri)
            ?: return Result.Error(AppException.UnknownException("No se pudo abrir la imagen seleccionada"))
        val bytes = inputStream.use { it.readBytes() }
        val mimeType = context.contentResolver.getType(uri) ?: "image/jpeg"
        val extension = when (mimeType) {
            "image/png" -> "png"
            "image/webp" -> "webp"
            else -> "jpg"
        }
        val requestBody = bytes.toRequestBody(mimeType.toMediaType())
        val part = MultipartBody.Part.createFormData("photo", "photo.$extension", requestBody)

        return when (val uploadResult = safeApiCall { apiService.uploadProfilePhoto(part) }) {
            is Result.Success -> {
                Log.d("AIFIT_DEBUG", "uploadProfilePhoto: upload OK — refreshing full profile")
                val uploadUrl = uploadResult.data.profilePictureUrl
                    ?: uploadResult.data.profileImageUrl
                when (val profileResult = safeApiCall { apiService.getProfile() }) {
                    is Result.Success -> {
                        val userId = authDataStore.getUserId()
                        val cached = if (userId != null) dao.getById(userId) else null
                        val profile = mergeProfileFromApi(
                            dto = profileResult.data,
                            cachedPictureUrl = cached?.profilePictureUrl,
                            persistedAvatarUrl = authDataStore.getAvatarUrl(userId),
                            uploadUrl = uploadUrl,
                        )
                        if (profile.profilePictureUrl == null && uploadUrl != null) {
                            Log.d(
                                "AIFIT_DEBUG",
                                "uploadProfilePhoto: GET returned null URL, patching with upload DTO url=$uploadUrl",
                            )
                        }
                        persistProfile(profile)
                        Log.d("AIFIT_DEBUG", "uploadProfilePhoto: profile refreshed, url=${profile.profilePictureUrl}")
                        Result.Success(profile)
                    }
                    is Result.Error -> {
                        val fallbackUrl = uploadUrl
                        Log.w(
                            "AIFIT_DEBUG",
                            "uploadProfilePhoto: getProfile failed, fallback url=$fallbackUrl, cause=${profileResult.exception.message}",
                        )
                        if (fallbackUrl != null) {
                            val userId = authDataStore.getUserId()
                            if (userId != null) {
                                val cached = dao.getById(userId)
                                if (cached != null) {
                                    val profile = cached.toDomain().copy(
                                        profilePictureUrl = pickBestProfilePictureUrl(
                                            fallbackUrl,
                                            cached.profilePictureUrl,
                                            authDataStore.getAvatarUrl(userId),
                                        ),
                                    )
                                    persistProfile(profile)
                                    Result.Success(profile)
                                } else {
                                    val profile = UserProfile(
                                        id = userId,
                                        name = authDataStore.getName().orEmpty(),
                                        email = authDataStore.getEmail().orEmpty(),
                                        authProvider = "",
                                        profilePictureUrl = fallbackUrl,
                                    )
                                    persistProfile(profile)
                                    Result.Success(profile)
                                }
                            } else {
                                profileResult
                            }
                        } else {
                            profileResult
                        }
                    }
                    is Result.Loading -> Result.Loading
                }
            }
            is Result.Error -> {
                Log.w("AIFIT_DEBUG", "uploadProfilePhoto: upload error=${uploadResult.exception.message}")
                uploadResult
            }
            is Result.Loading -> Result.Loading
        }
    }

    private fun mergeProfileFromApi(
        dto: UserProfileResponseDto,
        cachedPictureUrl: String?,
        persistedAvatarUrl: String?,
        uploadUrl: String? = null,
    ): UserProfile {
        val base = dto.toDomain()
        val bestUrl = pickBestProfilePictureUrl(
            uploadUrl,
            persistedAvatarUrl,
            cachedPictureUrl,
            dto.profileImageUrl,
            dto.profilePictureUrl,
        )
        return base.copy(profilePictureUrl = bestUrl)
    }

    private suspend fun persistProfile(profile: UserProfile) {
        dao.upsert(profile.toEntity())
        persistAvatarUrlIfBetter(authDataStore.getUserId() ?: profile.id, profile.profilePictureUrl)
    }

    /**
     * Stores the avatar locally only when it is an improvement (e.g. keep Cloudinary over Google OAuth default).
     */
    private fun persistAvatarUrlIfBetter(userId: String?, url: String?) {
        if (userId == null || url.isNullOrBlank()) return
        val existing = authDataStore.getAvatarUrl(userId)
        val best = pickBestProfilePictureUrl(url, existing) ?: return
        if (existing == best) return
        authDataStore.saveAvatarUrl(userId, best)
    }
}

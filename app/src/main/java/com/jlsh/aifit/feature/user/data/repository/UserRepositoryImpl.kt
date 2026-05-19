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
import com.jlsh.aifit.feature.user.data.local.UserProfileDao
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
                // The upload endpoint returns a partial payload whose URL field name is
                // unknown — it varies across backend implementations and cannot be
                // reliably mapped without risking silent data loss (ignoreUnknownKeys
                // discards any field not present in the DTO).
                //
                // Strategy: after a successful upload, immediately call getProfile() to
                // obtain the authoritative full profile (including the new URL) and
                // persist it to Room. This is resilient to any field-name mismatch.
                Log.d("AIFIT_DEBUG", "uploadProfilePhoto: upload OK — refreshing full profile")
                when (val profileResult = safeApiCall { apiService.getProfile() }) {
                    is Result.Success -> {
                        val freshProfile = profileResult.data.toDomain()
                        // Guard against server-side timing race: if the backend hasn't
                        // committed the Cloudinary URL to its DB yet, getProfile() may
                        // return profilePictureUrl=null even though the upload succeeded.
                        // Prefer the URL from the upload DTO over null so Room and the
                        // ViewModel never lose a URL they just successfully uploaded.
                        val uploadUrl = uploadResult.data.profilePictureUrl
                            ?: uploadResult.data.profileImageUrl
                        val profile = if (freshProfile.profilePictureUrl == null && uploadUrl != null) {
                            Log.d("AIFIT_DEBUG", "uploadProfilePhoto: GET returned null URL, patching with upload DTO url=$uploadUrl")
                            freshProfile.copy(profilePictureUrl = uploadUrl)
                        } else {
                            freshProfile
                        }
                        dao.upsert(profile.toEntity())
                        Log.d("AIFIT_DEBUG", "uploadProfilePhoto: profile refreshed, url=${profile.profilePictureUrl}")
                        Result.Success(profile)
                    }
                    is Result.Error -> {
                        // getProfile failed (e.g. offline) — try the partial URL from
                        // the upload response as a best-effort fallback.
                        val fallbackUrl = uploadResult.data.profilePictureUrl
                            ?: uploadResult.data.profileImageUrl
                        Log.w("AIFIT_DEBUG", "uploadProfilePhoto: getProfile failed, fallback url=$fallbackUrl, cause=${profileResult.exception.message}")
                        if (fallbackUrl != null) {
                            val userId = authDataStore.getUserId()
                            if (userId != null) {
                                val cached = dao.getById(userId)
                                if (cached != null) {
                                    val updated = cached.copy(profilePictureUrl = fallbackUrl)
                                    dao.upsert(updated)
                                    Result.Success(updated.toDomain())
                                } else {
                                    Result.Success(
                                        UserProfile(
                                            id = userId,
                                            name = "",
                                            email = "",
                                            authProvider = "",
                                            profilePictureUrl = fallbackUrl,
                                        )
                                    )
                                }
                            } else {
                                profileResult
                            }
                        } else {
                            // Neither the follow-up profile fetch nor the upload payload
                            // yielded a URL — surface the getProfile error so the user
                            // knows the photo may not have applied.
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
}

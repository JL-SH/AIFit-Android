package com.jlsh.aifit.feature.auth.data.repository

import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.core.network.BaseRemoteDataSource
import com.jlsh.aifit.core.session.SessionManager
import com.jlsh.aifit.feature.auth.data.api.AuthApiService
import com.jlsh.aifit.feature.auth.data.dto.GoogleLoginRequestDto
import com.jlsh.aifit.feature.auth.data.dto.LoginRequestDto
import com.jlsh.aifit.feature.auth.data.dto.RegisterRequestDto
import com.jlsh.aifit.feature.auth.domain.model.AuthToken
import com.jlsh.aifit.feature.auth.domain.repository.AuthRepository
import javax.inject.Inject

/**
 * Implementation of [AuthRepository] that delegates to the remote API and persists the local session.
 *
 * After a successful login, update [SessionManager] with token, user and full profile flag.
 *
 * @param apiService Authentication HTTP Client.
 * @param sessionManager Local session manager.
 */
class AuthRepositoryImpl @Inject constructor(
    private val apiService: AuthApiService,
    private val sessionManager: SessionManager,
) : BaseRemoteDataSource(), AuthRepository {
    private companion object {
        const val LOG_TAG = "AIFIT"
    }

    /**
     * Authenticate with email and password via API and persist the session if successful.
     *
     * @param email User's email.
     * @param password Password.
     * @return Domain token or network/validation error.
     */
    override suspend fun login(email: String, password: String): Result<AuthToken> {
        val result = safeApiCall { apiService.login(LoginRequestDto(email, password)) }
        return handleAuthResult(result)
    }

    /**
     * Register a new account and return your initial session.
     *
     * @param email Email of the new user.
     * @param password Password.
     * @param name Display name.
     * @return Domain token or error.
     */
    override suspend fun register(email: String, password: String, name: String): Result<AuthToken> {
        val result = safeApiCall { apiService.register(RegisterRequestDto(email, password, name)) }
        return handleAuthResult(result)
    }

    /**
     * Exchange one Google token ID per session in the backend.
     *
     * @param idToken Google Sign-In JWT.
     * @return Domain token or error.
     */
    override suspend fun googleLogin(idToken: String): Result<AuthToken> {
        logDebug("AuthRepository: googleLogin - calling POST auth/google with idToken (${idToken.length} chars)")
        val result = safeApiCall { apiService.googleLogin(GoogleLoginRequestDto(idToken)) }
        logDebug("AuthRepository: googleLogin - result: ${result::class.simpleName}" +
                if (result is Result.Error) " → ${result.exception}" else "")
        return handleAuthResult(result)
    }

    private fun handleAuthResult(result: Result<com.jlsh.aifit.feature.auth.data.dto.AuthResponseDto>): Result<AuthToken> {
        return when (result) {
            is Result.Success -> {
                val dto = result.data
                sessionManager.onLoginSuccess(
                    token = dto.token,
                    userId = dto.userId,
                    email = dto.email,
                    name = dto.name,
                    profileComplete = dto.profileComplete,
                )
                Result.Success(
                    AuthToken(
                        token = dto.token,
                        userId = dto.userId,
                        email = dto.email,
                        name = dto.name,
                        expiresIn = dto.expiresIn,
                        profileComplete = dto.profileComplete,
                    )
                )
            }
            is Result.Error -> result
            is Result.Loading -> result
        }
    }

    private fun logDebug(message: String) {
        runCatching {
            android.util.Log.d(LOG_TAG, message)
        }
    }
}


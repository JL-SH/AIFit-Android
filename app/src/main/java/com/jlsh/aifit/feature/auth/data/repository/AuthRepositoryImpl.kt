package com.jlsh.aifit.feature.auth.data.repository

import android.util.Log
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

class AuthRepositoryImpl @Inject constructor(
    private val apiService: AuthApiService,
    private val sessionManager: SessionManager,
) : BaseRemoteDataSource(), AuthRepository {

    override suspend fun login(email: String, password: String): Result<AuthToken> {
        val result = safeApiCall { apiService.login(LoginRequestDto(email, password)) }
        return handleAuthResult(result)
    }

    override suspend fun register(email: String, password: String, name: String): Result<AuthToken> {
        val result = safeApiCall { apiService.register(RegisterRequestDto(email, password, name)) }
        return handleAuthResult(result)
    }

    override suspend fun googleLogin(idToken: String): Result<AuthToken> {
        Log.d("AIFIT", "AuthRepository: googleLogin — llamando a POST auth/google con idToken (${idToken.length} chars)")
        val result = safeApiCall { apiService.googleLogin(GoogleLoginRequestDto(idToken)) }
        Log.d("AIFIT", "AuthRepository: googleLogin — resultado: ${result::class.simpleName}" +
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
}


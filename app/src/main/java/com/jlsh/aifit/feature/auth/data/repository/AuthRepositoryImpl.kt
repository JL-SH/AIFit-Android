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

/**
 * Implementación de [AuthRepository] que delega en la API remota y persiste la sesión local.
 *
 * Tras un login exitoso, actualiza [SessionManager] con token, usuario y flag de perfil completo.
 *
 * @param apiService Cliente HTTP de autenticación.
 * @param sessionManager Gestor de sesión local.
 */
class AuthRepositoryImpl @Inject constructor(
    private val apiService: AuthApiService,
    private val sessionManager: SessionManager,
) : BaseRemoteDataSource(), AuthRepository {

    /**
     * Autentica con email y contraseña vía API y persiste la sesión si tiene éxito.
     *
     * @param email Correo del usuario.
     * @param password Contraseña.
     * @return Token de dominio o error de red/validación.
     */
    override suspend fun login(email: String, password: String): Result<AuthToken> {
        val result = safeApiCall { apiService.login(LoginRequestDto(email, password)) }
        return handleAuthResult(result)
    }

    /**
     * Registra una cuenta nueva y devuelve la sesión inicial.
     *
     * @param email Correo del nuevo usuario.
     * @param password Contraseña.
     * @param name Nombre visible.
     * @return Token de dominio o error.
     */
    override suspend fun register(email: String, password: String, name: String): Result<AuthToken> {
        val result = safeApiCall { apiService.register(RegisterRequestDto(email, password, name)) }
        return handleAuthResult(result)
    }

    /**
     * Intercambia un ID token de Google por sesión en el backend.
     *
     * @param idToken JWT de Google Sign-In.
     * @return Token de dominio o error.
     */
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


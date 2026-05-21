package com.jlsh.aifit.feature.auth.domain.usecase

import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.feature.auth.domain.model.AuthToken
import com.jlsh.aifit.feature.auth.domain.repository.AuthRepository
import javax.inject.Inject

/**
 * Caso de uso que autentica al usuario mediante Google Sign-In.
 *
 * @param repository Repositorio de autenticación.
 */
class GoogleLoginUseCase @Inject constructor(
    private val repository: AuthRepository,
) {
    /**
     * Intercambia el ID token de Google por una sesión en el backend.
     *
     * @param idToken Token JWT obtenido del flujo de Credential Manager / Google.
     * @return [Result.Success] con el token de sesión, o [Result.Error] si el backend rechaza el token.
     */
    suspend operator fun invoke(idToken: String): Result<AuthToken> =
        repository.googleLogin(idToken)
}


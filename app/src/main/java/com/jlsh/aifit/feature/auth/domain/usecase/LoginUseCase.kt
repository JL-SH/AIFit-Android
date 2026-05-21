package com.jlsh.aifit.feature.auth.domain.usecase

import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.feature.auth.domain.model.AuthToken
import com.jlsh.aifit.feature.auth.domain.repository.AuthRepository
import javax.inject.Inject

/**
 * Caso de uso que autentica al usuario con email y contraseña.
 *
 * @param repository Repositorio de autenticación.
 */
class LoginUseCase @Inject constructor(
    private val repository: AuthRepository,
) {
    /**
     * Inicia sesión con las credenciales indicadas.
     *
     * @param email Correo electrónico del usuario.
     * @param password Contraseña en texto plano.
     * @return [Result.Success] con el token de sesión, o [Result.Error] si falla la autenticación.
     */
    suspend operator fun invoke(email: String, password: String): Result<AuthToken> =
        repository.login(email, password)
}


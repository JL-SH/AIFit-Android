package com.jlsh.aifit.feature.auth.domain.usecase

import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.feature.auth.domain.model.AuthToken
import com.jlsh.aifit.feature.auth.domain.repository.AuthRepository
import javax.inject.Inject

/**
 * Caso de uso que registra una cuenta nueva y devuelve la sesión inicial.
 *
 * @param repository Repositorio de autenticación.
 */
class RegisterUseCase @Inject constructor(
    private val repository: AuthRepository,
) {
    /**
     * Crea un usuario con email, contraseña y nombre visible.
     *
     * @param email Correo electrónico del nuevo usuario.
     * @param password Contraseña elegida por el usuario.
     * @param name Nombre para mostrar en el perfil.
     * @return [Result.Success] con el token de sesión tras el registro, o [Result.Error] si falla.
     */
    suspend operator fun invoke(email: String, password: String, name: String): Result<AuthToken> =
        repository.register(email, password, name)
}


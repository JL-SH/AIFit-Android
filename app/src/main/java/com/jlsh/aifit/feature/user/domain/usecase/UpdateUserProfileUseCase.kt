package com.jlsh.aifit.feature.user.domain.usecase

import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.feature.user.domain.model.UpdateUserProfileRequest
import com.jlsh.aifit.feature.user.domain.model.UserProfile
import com.jlsh.aifit.feature.user.domain.repository.UserRepository
import javax.inject.Inject

/**
 * Caso de uso que persiste cambios parciales del perfil en el backend.
 *
 * @param repository Repositorio de usuario.
 */
class UpdateUserProfileUseCase @Inject constructor(
    private val repository: UserRepository,
) {
    /**
     * Actualiza el perfil con los campos presentes en la petición.
     *
     * @param request Campos a modificar (los nulos o ausentes no se envían).
     * @return [Result.Success] con el perfil actualizado, o [Result.Error] si falla la API.
     */
    suspend operator fun invoke(request: UpdateUserProfileRequest): Result<UserProfile> =
        repository.updateProfile(request)
}


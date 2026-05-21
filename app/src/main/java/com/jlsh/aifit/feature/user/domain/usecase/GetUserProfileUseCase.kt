package com.jlsh.aifit.feature.user.domain.usecase

import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.feature.user.domain.model.UserProfile
import com.jlsh.aifit.feature.user.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Caso de uso que observa el perfil del usuario autenticado.
 *
 * El flujo puede emitir caché local primero y luego el resultado de red.
 *
 * @param repository Repositorio de usuario.
 */
class GetUserProfileUseCase @Inject constructor(
    private val repository: UserRepository,
) {
    /**
     * Obtiene el perfil como flujo reactivo.
     *
     * @return [Flow] de [Result] con [UserProfile]; incluye [Result.Loading] al inicio.
     */
    operator fun invoke(): Flow<Result<UserProfile>> = repository.getProfile()
}


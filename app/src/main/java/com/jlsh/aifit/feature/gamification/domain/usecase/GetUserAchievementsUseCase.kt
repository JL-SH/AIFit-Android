package com.jlsh.aifit.feature.gamification.domain.usecase

import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.feature.gamification.domain.model.UserAchievement
import com.jlsh.aifit.feature.gamification.domain.repository.GamificationRepository
import javax.inject.Inject

/**
 * Caso de uso que obtiene los logros desbloqueados del usuario autenticado.
 *
 * @param repository Repositorio de gamificación.
 */
class GetUserAchievementsUseCase @Inject constructor(
    private val repository: GamificationRepository,
) {
    /**
     * Recupera la lista de logros conseguidos por el usuario.
     *
     * @return [Result.Success] con los logros desbloqueados, o [Result.Error] si falla la petición.
     */
    suspend operator fun invoke(): Result<List<UserAchievement>> =
        repository.getAchievements()
}


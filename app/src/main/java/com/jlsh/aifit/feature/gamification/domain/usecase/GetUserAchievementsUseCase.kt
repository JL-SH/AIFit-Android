package com.jlsh.aifit.feature.gamification.domain.usecase

import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.feature.gamification.domain.model.UserAchievement
import com.jlsh.aifit.feature.gamification.domain.repository.GamificationRepository
import javax.inject.Inject

/**
 * Use case that gets the unlocked achievements from the authenticated user.
 *
 * @param repository Gamification repository.
 */
class GetUserAchievementsUseCase @Inject constructor(
    private val repository: GamificationRepository,
) {
    /**
     * Retrieves the list of achievements achieved by the user.
     *
     * @return [Result.Success] with achievements unlocked, or [Result.Error] if the request fails.
     */
    suspend operator fun invoke(): Result<List<UserAchievement>> =
        repository.getAchievements()
}


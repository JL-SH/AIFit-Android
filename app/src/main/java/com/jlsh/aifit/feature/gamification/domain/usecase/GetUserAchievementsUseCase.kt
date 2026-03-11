package com.jlsh.aifit.feature.gamification.domain.usecase

import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.feature.gamification.domain.model.UserAchievement
import com.jlsh.aifit.feature.gamification.domain.repository.GamificationRepository
import javax.inject.Inject

class GetUserAchievementsUseCase @Inject constructor(
    private val repository: GamificationRepository,
) {
    suspend operator fun invoke(): Result<List<UserAchievement>> =
        repository.getAchievements()
}


package com.jlsh.aifit.feature.gamification.domain.repository

import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.feature.gamification.domain.model.AchievementDefinition
import com.jlsh.aifit.feature.gamification.domain.model.PersonalRecord
import com.jlsh.aifit.feature.gamification.domain.model.ProgressExport
import com.jlsh.aifit.feature.gamification.domain.model.Streak
import com.jlsh.aifit.feature.gamification.domain.model.UserAchievement

interface GamificationRepository {
    suspend fun getStreaks(): Result<List<Streak>>
    suspend fun getAchievements(): Result<List<UserAchievement>>
    suspend fun getAllDefinitions(): Result<List<AchievementDefinition>>
    suspend fun getPersonalRecords(): Result<List<PersonalRecord>>
    suspend fun getExport(period: String): Result<ProgressExport>
}


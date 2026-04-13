package com.jlsh.aifit.feature.gamification.data.repository

import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.core.network.BaseRemoteDataSource
import com.jlsh.aifit.feature.gamification.data.api.GamificationApiService
import com.jlsh.aifit.feature.gamification.data.mapper.GamificationMapper.toDomain
import com.jlsh.aifit.feature.gamification.data.local.LocalAchievementDefinitions
import com.jlsh.aifit.feature.gamification.domain.model.AchievementDefinition
import com.jlsh.aifit.feature.gamification.domain.model.PersonalRecord
import com.jlsh.aifit.feature.gamification.domain.model.ProgressExport
import com.jlsh.aifit.feature.gamification.domain.model.Streak
import com.jlsh.aifit.feature.gamification.domain.model.UserAchievement
import com.jlsh.aifit.feature.gamification.domain.repository.GamificationRepository
import javax.inject.Inject

class GamificationRepositoryImpl @Inject constructor(
    private val apiService: GamificationApiService,
) : BaseRemoteDataSource(), GamificationRepository {

    override suspend fun getStreaks(): Result<List<Streak>> =
        when (val r = safeApiCall { apiService.getStreaks() }) {
            is Result.Success -> Result.Success(r.data.map { it.toDomain() })
            is Result.Error -> r
            else -> Result.Loading
        }

    override suspend fun getAchievements(): Result<List<UserAchievement>> =
        when (val r = safeApiCall { apiService.getAchievements() }) {
            is Result.Success -> Result.Success(r.data.map { it.toDomain() })
            is Result.Error -> r
            else -> Result.Loading
        }

    override suspend fun getAllDefinitions(): Result<List<AchievementDefinition>> {
        val r = safeApiCall { apiService.getAllAchievementDefinitions() }
        return when {
            r is Result.Success && r.data.isNotEmpty() ->
                Result.Success(r.data.map { it.toDomain() })
            // Si la API falla o devuelve vacío, usar las definiciones locales (datos estáticos
            // del juego, al estilo Steam). Siempre habrá logros que mostrar.
            else -> Result.Success(LocalAchievementDefinitions.all)
        }
    }

    override suspend fun getPersonalRecords(): Result<List<PersonalRecord>> =
        when (val r = safeApiCall { apiService.getPersonalRecords() }) {
            is Result.Success -> Result.Success(r.data.map { it.toDomain() })
            is Result.Error -> r
            else -> Result.Loading
        }

    override suspend fun getExport(period: String): Result<ProgressExport> =
        when (val r = safeApiCall { apiService.getExport(period) }) {
            is Result.Success -> Result.Success(r.data.toDomain())
            is Result.Error -> r
            else -> Result.Loading
        }
}


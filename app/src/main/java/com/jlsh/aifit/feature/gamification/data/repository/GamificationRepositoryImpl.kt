package com.jlsh.aifit.feature.gamification.data.repository

import android.util.Log
import com.jlsh.aifit.core.common.AppException
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

    override suspend fun getStreaks(): Result<List<Streak>> {
        return try {
            when (val r = safeApiCall { apiService.getStreaks() }) {
                is Result.Success -> Result.Success(r.data.map { it.toDomain() })
                is Result.Error -> r
                else -> Result.Loading
            }
        } catch (e: Exception) {
            Log.e("AIFIT_DEBUG", "getStreaks mapper error: ${e.message}", e)
            Result.Error(AppException.UnknownException(e.message ?: "Error al procesar rachas"))
        }
    }

    override suspend fun getAchievements(): Result<List<UserAchievement>> {
        return try {
            when (val r = safeApiCall { apiService.getAchievements() }) {
                is Result.Success -> Result.Success(r.data.map { it.toDomain() })
                is Result.Error -> r
                else -> Result.Loading
            }
        } catch (e: Exception) {
            Log.e("AIFIT_DEBUG", "getAchievements mapper error: ${e.message}", e)
            Result.Error(AppException.UnknownException(e.message ?: "Error al procesar logros"))
        }
    }

    override suspend fun getAllDefinitions(): Result<List<AchievementDefinition>> {
        return try {
            val r = safeApiCall { apiService.getAllAchievementDefinitions() }
            when {
                r is Result.Success && r.data.isNotEmpty() ->
                    Result.Success(r.data.map { it.toDomain() })
                // If the API fails or returns empty, use local definitions (static data
                // of the game, Steam style). There will always be achievements to show.
                else -> Result.Success(LocalAchievementDefinitions.all)
            }
        } catch (e: Exception) {
            Log.e("AIFIT_DEBUG", "getAllDefinitions mapper error: ${e.message}", e)
            Result.Success(LocalAchievementDefinitions.all) // fallback to local data on mapper error
        }
    }

    override suspend fun getPersonalRecords(): Result<List<PersonalRecord>> {
        return try {
            when (val r = safeApiCall { apiService.getPersonalRecords() }) {
                is Result.Success -> Result.Success(r.data.map { it.toDomain() })
                is Result.Error -> r
                else -> Result.Loading
            }
        } catch (e: Exception) {
            Log.e("AIFIT_DEBUG", "getPersonalRecords mapper error: ${e.message}", e)
            Result.Error(AppException.UnknownException(e.message ?: "Error al procesar récords"))
        }
    }

    override suspend fun getExport(period: String): Result<ProgressExport> {
        return try {
            when (val r = safeApiCall { apiService.getExport(period) }) {
                is Result.Success -> Result.Success(r.data.toDomain())
                is Result.Error -> r
                else -> Result.Loading
            }
        } catch (e: Exception) {
            Log.e("AIFIT_DEBUG", "getExport mapper error: ${e.message}", e)
            Result.Error(AppException.UnknownException(e.message ?: "Error al procesar el informe de progreso"))
        }
    }
}

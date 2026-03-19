package com.jlsh.aifit.feature.training.data.repository

import com.jlsh.aifit.core.common.AppException
import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.core.network.BaseRemoteDataSource
import com.jlsh.aifit.core.session.SessionManager
import com.jlsh.aifit.feature.training.data.api.TrainingApiService
import com.jlsh.aifit.feature.training.data.dto.GenerateAdaptiveTrainingPlanRequestDto
import com.jlsh.aifit.feature.training.data.dto.GenerateTrainingPlanRequestDto
import com.jlsh.aifit.feature.training.data.local.TrainingPlanDao
import com.jlsh.aifit.feature.training.data.mapper.TrainingMapper.toDomain
import com.jlsh.aifit.feature.training.data.mapper.TrainingMapper.toEntity
import com.jlsh.aifit.feature.training.domain.model.ExerciseSubstitution
import com.jlsh.aifit.feature.training.domain.model.TrainingPlan
import com.jlsh.aifit.feature.training.domain.model.WarmUpProtocol
import com.jlsh.aifit.feature.training.domain.repository.TrainingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class TrainingRepositoryImpl @Inject constructor(
    private val apiService: TrainingApiService,
    private val dao: TrainingPlanDao,
    private val sessionManager: SessionManager,
) : BaseRemoteDataSource(), TrainingRepository {

    override fun getTrainingPlans(): Flow<Result<List<TrainingPlan>>> = flow {
        emit(Result.Loading)

        val userId = sessionManager.getUserId()
        if (userId == null) {
            emit(Result.Error(AppException.UnknownException("No active session")))
            return@flow
        }

        val cached = dao.getAllByUserId(userId).map { it.toDomain() }
        if (cached.isNotEmpty()) {
            emit(Result.Success(cached))
        }

        when (val remote = safeApiCall { apiService.getTrainingPlans() }) {
            is Result.Success -> {
                val plans = remote.data.map { it.toDomain() }
                dao.upsertAll(plans.map { it.toEntity(userId) })
                emit(Result.Success(plans))
            }
            is Result.Error -> {
                if (cached.isEmpty()) emit(remote)
            }
            else -> Unit
        }
    }.distinctUntilChanged()

    override suspend fun getTrainingPlanDetail(planId: String): Result<TrainingPlan> {
        return when (val remote = safeApiCall { apiService.getTrainingPlanById(planId) }) {
            is Result.Success -> Result.Success(remote.data.toDomain())
            is Result.Error -> remote
            else -> Result.Loading
        }
    }

    override suspend fun generateTrainingPlan(
        request: GenerateTrainingPlanRequestDto,
    ): Result<TrainingPlan> {
        val userId = sessionManager.getUserId()
            ?: return Result.Error(AppException.UnknownException("No active session"))
        return when (val remote = safeApiCall { apiService.generateTrainingPlan(request) }) {
            is Result.Success -> {
                val plan = remote.data.toDomain()
                dao.upsertAll(listOf(plan.toEntity(userId)))
                Result.Success(plan)
            }
            is Result.Error -> remote
            else -> Result.Loading
        }
    }

    override suspend fun generateAdaptiveTrainingPlan(
        request: GenerateAdaptiveTrainingPlanRequestDto,
    ): Result<TrainingPlan> {
        val userId = sessionManager.getUserId()
            ?: return Result.Error(AppException.UnknownException("No active session"))
        return when (val remote = safeApiCall { apiService.generateAdaptiveTrainingPlan(request) }) {
            is Result.Success -> {
                val plan = remote.data.toDomain()
                dao.upsertAll(listOf(plan.toEntity(userId)))
                Result.Success(plan)
            }
            is Result.Error -> remote
            else -> Result.Loading
        }
    }

    override suspend fun deleteTrainingPlan(planId: String): Result<Unit> {
        return when (val remote = safeApiCall { apiService.deleteTrainingPlan(planId) }) {
            is Result.Success -> {
                dao.deleteById(planId)
                Result.Success(Unit)
            }
            is Result.Error -> remote
            else -> Result.Loading
        }
    }

    override suspend fun activatePlan(planId: String): Result<TrainingPlan> {
        val userId = sessionManager.getUserId()
            ?: return Result.Error(AppException.UnknownException("No active session"))
        return when (val remote = safeApiCall { apiService.activatePlan(planId) }) {
            is Result.Success -> {
                val plan = remote.data.toDomain()
                dao.upsertAll(listOf(plan.toEntity(userId)))
                Result.Success(plan)
            }
            is Result.Error -> remote
            else -> Result.Loading
        }
    }

    override suspend fun getWarmUpProtocol(planId: String, dayId: String): Result<WarmUpProtocol> {
        return when (val remote = safeApiCall { apiService.getWarmUpProtocol(planId, dayId) }) {
            is Result.Success -> Result.Success(remote.data.toDomain())
            is Result.Error -> remote
            else -> Result.Loading
        }
    }

    override suspend fun getExerciseSubstitutions(exerciseId: String): Result<List<ExerciseSubstitution>> {
        return when (val remote = safeApiCall { apiService.getExerciseSubstitutions(exerciseId) }) {
            is Result.Success -> Result.Success(remote.data.map { it.toDomain() })
            is Result.Error -> remote
            else -> Result.Loading
        }
    }
}

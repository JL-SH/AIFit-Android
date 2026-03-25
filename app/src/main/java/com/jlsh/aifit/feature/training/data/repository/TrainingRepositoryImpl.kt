package com.jlsh.aifit.feature.training.data.repository

import android.util.Log
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
            // TODO: remove diagnostic log below
            Log.d("AIFIT_PLANS", "EMIT ERROR — userId is null")
            emit(Result.Error(AppException.UnknownException("No active session")))
            return@flow
        }

        val cached = dao.getAllByUserId(userId).map { it.toDomain() }
        if (cached.isNotEmpty()) {
            // TODO: remove diagnostic log below
            Log.d("AIFIT_PLANS", "EMIT CACHE — count=${cached.size} ids=${cached.map { it.id }.take(3)}")
            // AIFIT_DEBUG: status de cada plan en caché
            cached.forEach { p ->
                Log.d("AIFIT_DEBUG", "[REPO][CACHE] plan id=${p.id} status=${p.status} days=${p.days.size}")
            }
            emit(Result.Success(cached))
        }

        when (val remote = safeApiCall { apiService.getTrainingPlans() }) {
            is Result.Success -> {
                val plans = remote.data.map { it.toDomain() }
                dao.upsertAll(plans.map { it.toEntity(userId) })
                // TODO: remove diagnostic log below
                Log.d("AIFIT_PLANS", "EMIT NETWORK — count=${plans.size} ids=${plans.map { it.id }.take(3)}")
                // AIFIT_DEBUG: status de cada plan recibido de red
                plans.forEach { p ->
                    Log.d("AIFIT_DEBUG", "[REPO][NETWORK] plan id=${p.id} status=${p.status} days=${p.days.size}")
                }
                emit(Result.Success(plans))
            }
            is Result.Error -> {
                if (cached.isEmpty()) emit(remote)
            }
            else -> Unit
        }
    }.distinctUntilChanged()

    override suspend fun getTrainingPlanDetail(planId: String): Result<TrainingPlan> {
        Log.d("AIFIT_DEBUG", "[REPO][DETAIL] START planId=$planId")
        return when (val remote = safeApiCall { apiService.getTrainingPlanById(planId) }) {
            is Result.Success -> {
                val plan = remote.data.toDomain()
                Log.d("AIFIT_DEBUG", "[REPO][DETAIL] OK planId=${plan.id} status=${plan.status} days=${plan.days.size} totalExercises=${plan.days.sumOf { it.exercises.size }}")
                // Persist detail so future cache fallbacks include this plan
                val userId = sessionManager.getUserId()
                if (userId != null) {
                    dao.upsertAll(listOf(plan.toEntity(userId)))
                }
                Result.Success(plan)
            }
            is Result.Error -> {
                Log.e("AIFIT_DEBUG", "[REPO][DETAIL] ERROR planId=$planId — ${remote.exception.message}")
                // Room entity never stores days, so returning the cached entity
                // as a "success" would give the caller a plan with days=emptyList(),
                // which silently breaks deriveTodayTraining(). Propagate the error
                // so the caller can retry or fall back gracefully.
                remote
            }
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
        return when (val remote = safeEmptyApiCall { apiService.deleteTrainingPlan(planId) }) {
            is Result.Success -> {
                // TODO: remove diagnostic logs below
                Log.d("AIFIT_DELETE", "API delete SUCCESS — planId=$planId")
                Log.d("AIFIT_DELETE", "dao.deleteById BEFORE — planId=$planId")
                dao.deleteById(planId)
                Log.d("AIFIT_DELETE", "dao.deleteById AFTER — planId=$planId")
                Result.Success(Unit)
            }
            is Result.Error -> {
                // TODO: remove diagnostic log below
                Log.d("AIFIT_DELETE", "API delete ERROR — $planId — ${remote.exception.message}")
                remote
            }
            else -> Result.Loading
        }
    }

    override suspend fun activatePlan(planId: String): Result<TrainingPlan> {
        val userId = sessionManager.getUserId()
            ?: return Result.Error(AppException.UnknownException("No active session"))

        // Read the plan that is currently ACTIVE (and is not the one being activated)
        // so we can demote it to PAUSED in the local cache after the API call succeeds,
        // preventing a transient two-active-plan flash.
        val previouslyActivePlan = dao.getAllByUserId(userId)
            .firstOrNull { it.status.equals("ACTIVE", ignoreCase = true) && it.id != planId }

        return when (val remote = safeApiCall { apiService.activatePlan(planId) }) {
            is Result.Success -> {
                val plan = remote.data.toDomain()
                // (1) Demote the old active plan to PAUSED before upserting the new one
                if (previouslyActivePlan != null) {
                    dao.upsertAll(listOf(previouslyActivePlan.copy(status = "PAUSED")))
                }
                // (2) Upsert the newly activated plan
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

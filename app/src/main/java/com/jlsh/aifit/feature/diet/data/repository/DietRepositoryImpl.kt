package com.jlsh.aifit.feature.diet.data.repository

import com.jlsh.aifit.core.common.AppException
import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.core.network.BaseRemoteDataSource
import com.jlsh.aifit.core.session.SessionManager
import com.jlsh.aifit.feature.diet.data.api.DietApiService
import com.jlsh.aifit.feature.diet.data.dto.GenerateAdaptiveDietPlanRequestDto
import com.jlsh.aifit.feature.diet.data.dto.GenerateDietPlanRequestDto
import com.jlsh.aifit.feature.diet.data.local.DietPlanDao
import com.jlsh.aifit.feature.diet.data.mapper.DietMapper.toDomain
import com.jlsh.aifit.feature.diet.data.mapper.DietMapper.toEntity
import com.jlsh.aifit.feature.diet.domain.model.DietPlan
import com.jlsh.aifit.feature.diet.domain.repository.DietRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class DietRepositoryImpl @Inject constructor(
    private val apiService: DietApiService,
    private val dao: DietPlanDao,
    private val sessionManager: SessionManager,
) : BaseRemoteDataSource(), DietRepository {

    override fun getDietPlans(): Flow<Result<List<DietPlan>>> = flow {
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

        when (val remote = safeApiCall { apiService.getDietPlans() }) {
            is Result.Success -> {
                val plans = remote.data.map { it.toDomain() }
                dao.upsertAll(plans.map { it.toEntity(userId) })
                // Reconciliation: remove any cached row that the server no longer returns.
                val networkIds = plans.map { it.id }
                if (networkIds.isEmpty()) {
                    dao.deleteAllByUserId(userId)
                } else {
                    dao.deleteAllNotInIds(userId, networkIds)
                }
                emit(Result.Success(plans))
            }
            is Result.Error -> {
                if (cached.isEmpty()) emit(remote)
            }
            else -> Unit
        }
    }.distinctUntilChanged()

    override suspend fun getDietPlanDetail(planId: String): Result<DietPlan> {
        return when (val remote = safeApiCall { apiService.getDietPlanById(planId) }) {
            is Result.Success -> Result.Success(remote.data.toDomain())
            is Result.Error -> remote
            else -> Result.Loading
        }
    }

    override suspend fun generateDietPlan(
        request: GenerateDietPlanRequestDto,
    ): Result<DietPlan> {
        val userId = sessionManager.getUserId()
            ?: return Result.Error(AppException.UnknownException("No active session"))
        return when (val remote = safeApiCall { apiService.generateDietPlan(request) }) {
            is Result.Success -> {
                val plan = remote.data.toDomain()
                dao.upsertAll(listOf(plan.toEntity(userId)))
                Result.Success(plan)
            }
            is Result.Error -> remote
            else -> Result.Loading
        }
    }

    override suspend fun generateAdaptiveDietPlan(
        request: GenerateAdaptiveDietPlanRequestDto,
    ): Result<DietPlan> {
        val userId = sessionManager.getUserId()
            ?: return Result.Error(AppException.UnknownException("No active session"))
        return when (val remote = safeApiCall { apiService.generateAdaptiveDietPlan(request) }) {
            is Result.Success -> {
                val plan = remote.data.toDomain()
                dao.upsertAll(listOf(plan.toEntity(userId)))
                Result.Success(plan)
            }
            is Result.Error -> remote
            else -> Result.Loading
        }
    }

    override suspend fun setActiveDietPlan(planId: String): Result<DietPlan> {
        val userId = sessionManager.getUserId()
            ?: return Result.Error(AppException.UnknownException("No active session"))

        // Read the plan that is currently ACTIVE (and is not the one being activated)
        // so we can demote it to PAUSED in the local cache after the API call succeeds.
        val previouslyActivePlan = dao.getAllByUserId(userId)
            .firstOrNull { it.status.equals("ACTIVE", ignoreCase = true) && it.id != planId }

        return when (val remote = safeApiCall { apiService.activateDietPlan(planId) }) {
            is Result.Success -> {
                val activatedPlan = remote.data.toDomain()
                // (1) Demote the old active plan to PAUSED before upserting the new one
                if (previouslyActivePlan != null) {
                    dao.upsertAll(listOf(previouslyActivePlan.copy(status = "PAUSED")))
                }
                // (2) Upsert the newly activated plan
                dao.upsertAll(listOf(activatedPlan.toEntity(userId)))
                Result.Success(activatedPlan)
            }
            is Result.Error -> remote
            else -> Result.Loading
        }
    }

    override suspend fun pauseDietPlan(planId: String): Result<DietPlan> {
        val userId = sessionManager.getUserId()
            ?: return Result.Error(AppException.UnknownException("No active session"))
        return when (val remote = safeApiCall { apiService.pauseDietPlan(planId) }) {
            is Result.Success -> {
                val pausedPlan = remote.data.toDomain()
                val existing = dao.getAllByUserId(userId)
                val updated = existing.map { entity ->
                    if (entity.id == planId) pausedPlan.toEntity(userId)
                    else entity
                }
                dao.upsertAll(updated)
                Result.Success(pausedPlan)
            }
            is Result.Error -> remote
            else -> Result.Loading
        }
    }

    override suspend fun deleteDietPlan(planId: String): Result<Unit> {
        // Delete from Room FIRST so any concurrent getDietPlans() cache emission
        // does not re-surface the deleted item. Rollback if the API call fails.
        val backup = dao.getById(planId)
        dao.deleteById(planId)
        return try {
            val response = apiService.deleteDietPlan(planId)
            if (response.success) {
                Result.Success(Unit)
            } else {
                // Rollback local delete
                if (backup != null) dao.upsertAll(listOf(backup))
                Result.Error(
                    AppException.UnknownException(
                        response.message ?: "Error al eliminar plan"
                    )
                )
            }
        } catch (e: Exception) {
            // Rollback local delete
            if (backup != null) dao.upsertAll(listOf(backup))
            Result.Error(com.jlsh.aifit.core.network.NetworkErrorMapper.map(e))
        }
    }
}


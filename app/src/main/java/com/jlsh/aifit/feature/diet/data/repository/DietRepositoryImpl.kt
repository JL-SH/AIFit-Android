package com.jlsh.aifit.feature.diet.data.repository

import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.core.network.BaseRemoteDataSource
import com.jlsh.aifit.feature.diet.data.api.DietApiService
import com.jlsh.aifit.feature.diet.data.dto.GenerateAdaptiveDietPlanRequestDto
import com.jlsh.aifit.feature.diet.data.dto.GenerateDietPlanRequestDto
import com.jlsh.aifit.feature.diet.data.local.DietPlanDao
import com.jlsh.aifit.feature.diet.data.mapper.DietMapper.toDomain
import com.jlsh.aifit.feature.diet.data.mapper.DietMapper.toEntity
import com.jlsh.aifit.feature.diet.domain.model.DietPlan
import com.jlsh.aifit.feature.diet.domain.repository.DietRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class DietRepositoryImpl @Inject constructor(
    private val apiService: DietApiService,
    private val dao: DietPlanDao,
) : BaseRemoteDataSource(), DietRepository {

    override fun getDietPlans(): Flow<Result<List<DietPlan>>> = flow {
        emit(Result.Loading)

        val cached = dao.getAll().map { it.toDomain() }
        emit(Result.Success(cached))

        when (val remote = safeApiCall { apiService.getDietPlans() }) {
            is Result.Success -> {
                val plans = remote.data.map { it.toDomain() }
                dao.upsertAll(plans.map { it.toEntity() })
                emit(Result.Success(plans))
            }
            is Result.Error -> {
                if (cached.isEmpty()) emit(remote)
            }
            else -> Unit
        }
    }

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
        return when (val remote = safeApiCall { apiService.generateDietPlan(request) }) {
            is Result.Success -> {
                val plan = remote.data.toDomain()
                dao.upsertAll(listOf(plan.toEntity()))
                Result.Success(plan)
            }
            is Result.Error -> remote
            else -> Result.Loading
        }
    }

    override suspend fun generateAdaptiveDietPlan(
        request: GenerateAdaptiveDietPlanRequestDto,
    ): Result<DietPlan> {
        return when (val remote = safeApiCall { apiService.generateAdaptiveDietPlan(request) }) {
            is Result.Success -> {
                val plan = remote.data.toDomain()
                dao.upsertAll(listOf(plan.toEntity()))
                Result.Success(plan)
            }
            is Result.Error -> remote
            else -> Result.Loading
        }
    }

    override suspend fun deleteDietPlan(planId: String): Result<Unit> {
        return when (val remote = safeApiCall { apiService.deleteDietPlan(planId) }) {
            is Result.Success -> {
                dao.deleteById(planId)
                Result.Success(Unit)
            }
            is Result.Error -> remote
            else -> Result.Loading
        }
    }
}


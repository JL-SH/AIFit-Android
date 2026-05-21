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

/**
 * Implementación de [DietRepository] con estrategia cache-first en Room y sincronización con la API.
 *
 * Gestiona condiciones de carrera en borrados concurrentes mediante [recentlyDeletedIds].
 */
class DietRepositoryImpl @Inject constructor(
    private val apiService: DietApiService,
    private val dao: DietPlanDao,
    private val sessionManager: SessionManager,
) : BaseRemoteDataSource(), DietRepository {

    /**
     * Plan IDs locally deleted while the server delete may still be in-flight.
     * Prevents race-condition reinjection during concurrent [getDietPlans] emissions.
     */
    @Volatile
    private var recentlyDeletedIds = emptySet<String>()

    /**
     * Emite la lista de planes: primero caché local del usuario, luego sincroniza con el servidor.
     *
     * @return Flujo de [Result]; [Result.Error] si no hay sesión activa.
     */
    override fun getDietPlans(): Flow<Result<List<DietPlan>>> = flow {
        emit(Result.Loading)

        val userId = sessionManager.getUserId()
        if (userId == null) {
            emit(Result.Error(AppException.UnknownException("No active session")))
            return@flow
        }

        val cached = dao.getAllByUserId(userId)
            .map { it.toDomain() }
            .filter { it.id !in recentlyDeletedIds }
        if (cached.isNotEmpty()) {
            emit(Result.Success(cached))
        }

        when (val remote = safeApiCall { apiService.getDietPlans() }) {
            is Result.Success -> {
                val allNetworkPlans = remote.data.map { it.toDomain() }
                val serverPlanIds = allNetworkPlans.map { it.id }.toSet()

                recentlyDeletedIds = recentlyDeletedIds intersect serverPlanIds

                val plans = allNetworkPlans.filter { it.id !in recentlyDeletedIds }

                dao.upsertAll(plans.map { it.toEntity(userId) })
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

    /**
     * Obtiene el detalle de un plan por id y actualiza la caché local.
     *
     * @param planId Identificador del plan.
     * @return [Result.Success] con el plan y sus días, o [Result.Error].
     */
    override suspend fun getDietPlanDetail(planId: String): Result<DietPlan> {
        return when (val remote = safeApiCall { apiService.getDietPlanById(planId) }) {
            is Result.Success -> {
                val plan = remote.data.toDomain()
                val userId = sessionManager.getUserId()
                if (userId != null) {
                    dao.upsertAll(listOf(plan.toEntity(userId)))
                }
                Result.Success(plan)
            }
            is Result.Error -> remote
            else -> Result.Loading
        }
    }

    /**
     * Genera un plan estándar en el servidor y lo persiste en Room.
     *
     * @param request Parámetros de generación.
     * @return [Result.Success] con el plan creado, o [Result.Error] (p. ej. sin sesión).
     */
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

    /**
     * Genera un plan adaptativo en el servidor y lo persiste en Room.
     *
     * @param request Parámetros adaptativos (perfil, historial, feedback).
     * @return [Result.Success] con el plan creado, o [Result.Error].
     */
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

    /**
     * Activa un plan en el servidor; pausa el plan activo previo en Room.
     *
     * @param planId Identificador del plan a activar.
     * @return [Result.Success] con el plan activado, o [Result.Error].
     */
    override suspend fun setActiveDietPlan(planId: String): Result<DietPlan> {
        val userId = sessionManager.getUserId()
            ?: return Result.Error(AppException.UnknownException("No active session"))

        val previouslyActivePlan = dao.getAllByUserId(userId)
            .firstOrNull { it.status.equals("ACTIVE", ignoreCase = true) && it.id != planId }

        return when (val remote = safeApiCall { apiService.activateDietPlan(planId) }) {
            is Result.Success -> {
                val activatedPlan = remote.data.toDomain()
                if (previouslyActivePlan != null) {
                    dao.upsertAll(listOf(previouslyActivePlan.copy(status = "PAUSED")))
                }
                dao.upsertAll(listOf(activatedPlan.toEntity(userId)))
                Result.Success(activatedPlan)
            }
            is Result.Error -> remote
            else -> Result.Loading
        }
    }

    /**
     * Pausa un plan en el servidor y actualiza su estado en Room.
     *
     * @param planId Identificador del plan a pausar.
     * @return [Result.Success] con el plan pausado, o [Result.Error].
     */
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

    /**
     * Elimina un plan: borrado optimista en Room, confirmación en API y rollback si falla la red.
     *
     * @param planId Identificador del plan a eliminar.
     * @return [Result.Success] tras confirmación del servidor, o [Result.Error] con restauración de caché.
     */
    override suspend fun deleteDietPlan(planId: String): Result<Unit> {
        // 1. Snapshot for rollback if the network call fails.
        val planSnapshot = dao.getById(planId)

        // 2. Register in guard set — prevents concurrent getDietPlans() from reinserting
        //    this plan via upsertAll while the delete API is in-flight.
        recentlyDeletedIds = recentlyDeletedIds + planId

        // 3. Remove from Room immediately so cache emissions never resurrect the plan
        //    during the API window (~1–4 s). Mirrors TrainingRepositoryImpl.deleteTrainingPlan.
        dao.deleteById(planId)

        // 4. Confirm deletion with the server.
        return when (val remote = safeUnitApiCall { apiService.deleteDietPlan(planId) }) {
            is Result.Success -> Result.Success(Unit)
            is Result.Error -> {
                planSnapshot?.let { dao.upsertAll(listOf(it)) }
                recentlyDeletedIds = recentlyDeletedIds - planId
                remote
            }
            else -> Result.Loading
        }
    }

    private suspend fun safeUnitApiCall(apiCall: suspend () -> com.jlsh.aifit.core.network.ApiResponse<Unit>): Result<Unit> {
        return try {
            val response = apiCall()
            if (response.success) {
                Result.Success(Unit)
            } else {
                Result.Error(
                    AppException.UnknownException(
                        response.message ?: "Unknown server error"
                    )
                )
            }
        } catch (e: Exception) {
            Result.Error(com.jlsh.aifit.core.network.NetworkErrorMapper.map(e))
        }
    }
}

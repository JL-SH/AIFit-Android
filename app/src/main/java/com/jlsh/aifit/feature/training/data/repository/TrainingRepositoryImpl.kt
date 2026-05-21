package com.jlsh.aifit.feature.training.data.repository

import android.util.Log
import com.jlsh.aifit.core.common.AppException
import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.core.network.BaseRemoteDataSource
import com.jlsh.aifit.core.session.SessionManager
import com.jlsh.aifit.feature.training.data.api.TrainingApiService
import com.jlsh.aifit.feature.training.data.dto.GenerateAdaptiveTrainingPlanRequestDto
import com.jlsh.aifit.feature.training.data.dto.GenerateTrainingPlanRequestDto
import com.jlsh.aifit.feature.training.data.dto.TrainingPlanResponseDto
import com.jlsh.aifit.feature.training.data.local.TrainingPlanDao
import com.jlsh.aifit.feature.training.data.local.TrainingPlanDetailCacheDao
import com.jlsh.aifit.feature.training.data.local.TrainingPlanDetailCacheEntity
import com.jlsh.aifit.feature.training.data.mapper.TrainingMapper.toDomain
import com.jlsh.aifit.feature.training.data.mapper.TrainingMapper.toEntity
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import com.jlsh.aifit.feature.training.domain.model.ExerciseSubstitution
import com.jlsh.aifit.feature.training.domain.model.TrainingPlan
import com.jlsh.aifit.feature.training.domain.model.WarmUpProtocol
import com.jlsh.aifit.feature.training.domain.repository.TrainingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

/**
 * Implementación de [TrainingRepository] con caché Room, sincronización de red
 * y protección ante condiciones de carrera en borrados locales.
 */
class TrainingRepositoryImpl @Inject constructor(
    private val apiService: TrainingApiService,
    private val dao: TrainingPlanDao,
    private val detailCacheDao: TrainingPlanDetailCacheDao,
    private val sessionManager: SessionManager,
) : BaseRemoteDataSource(), TrainingRepository {

    private val detailJson = Json { ignoreUnknownKeys = true }

    /**
     * Plan IDs that have been locally deleted but whose server-side delete may still be
     * in-flight. While a plan ID is present here:
     *  - getTrainingPlans() skips it when upserting network results → prevents race-condition
     *    reinjection during the API call window (~1–4 s).
     *  - The guard auto-lifts the first time getTrainingPlans() receives a network response
     *    that no longer contains the plan (server has committed the delete).
     * @Volatile ensures the reference is always fresh across coroutines/threads.
     */
    @Volatile
    private var recentlyDeletedIds = emptySet<String>()

    /**
     * Emite la lista de planes del usuario: primero caché local y luego reconciliación con red.
     *
     * @return Flujo que emite [Result.Loading], luego caché si existe, y finalmente datos de red
     *   o error si no hay caché.
     */
    override fun getTrainingPlans(): Flow<Result<List<TrainingPlan>>> = flow {
        emit(Result.Loading)

        val userId = sessionManager.getUserId()
        if (userId == null) {
            // TODO: remove diagnostic log below
            Log.d("AIFIT_PLANS", "EMIT ERROR — userId is null")
            emit(Result.Error(AppException.UnknownException("No active session")))
            return@flow
        }

        val cached = dao.getAllByUserId(userId)
            .map { it.toDomain() }
            .filter { it.id !in recentlyDeletedIds }
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
                val allNetworkPlans = remote.data.map { it.toDomain() }
                val serverPlanIds = allNetworkPlans.map { it.id }.toSet()

                // Lift the guard for any plan the server has confirmed is gone
                // (absent from this response means delete was committed server-side).
                recentlyDeletedIds = recentlyDeletedIds intersect serverPlanIds

                // Filter out locally-deleted plans that the server might still echo back
                // during the delete API call window (race condition).
                val plans = allNetworkPlans.filter { it.id !in recentlyDeletedIds }

                dao.upsertAll(plans.map { it.toEntity(userId) })
                // Reconciliation: remove any cached row that the server no longer returns.
                // This eliminates ghost plans (soft-deleted server-side but still in Room),
                // corrupt statuses (stale cache overriding server truth), and phantom dual-active
                // artifacts — all caused by the previous additive-only sync strategy.
                val networkIds = plans.map { it.id }
                if (networkIds.isEmpty()) {
                    dao.deleteAllByUserId(userId)
                } else {
                    dao.deleteAllNotInIds(userId, networkIds)
                }
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

    /**
     * Obtiene el detalle de un plan desde la caché de detalle JSON, sin llamada de red.
     *
     * @param planId Identificador del plan.
     * @return Plan de dominio o null si no hay entrada en caché o el JSON es inválido.
     */
    override suspend fun getCachedTrainingPlanDetail(planId: String): TrainingPlan? =
        detailCacheDao.getById(planId)?.let { entity ->
            runCatching {
                detailJson.decodeFromString<TrainingPlanResponseDto>(entity.detailJson).toDomain()
            }.getOrNull()
        }

    /**
     * Carga el detalle completo del plan desde red y actualiza caché y resumen en Room.
     *
     * @param planId Identificador del plan.
     * @return [Result.Success] con días y ejercicios, caché en error de red, o [Result.Error].
     */
    override suspend fun getTrainingPlanDetail(planId: String): Result<TrainingPlan> {
        Log.d("AIFIT_DEBUG", "[REPO][DETAIL] START planId=$planId")
        val cached = getCachedTrainingPlanDetail(planId)
        return when (val remote = safeApiCall { apiService.getTrainingPlanById(planId) }) {
            is Result.Success -> {
                val dto = remote.data
                val plan = dto.toDomain()
                detailCacheDao.upsert(
                    TrainingPlanDetailCacheEntity(
                        planId = planId,
                        detailJson = detailJson.encodeToString(dto),
                        cachedAt = System.currentTimeMillis(),
                    ),
                )
                Log.d("AIFIT_DEBUG", "[REPO][DETAIL] OK planId=${plan.id} status=${plan.status} days=${plan.days.size} totalExercises=${plan.days.sumOf { it.exercises.size }}")
                val userId = sessionManager.getUserId()
                if (userId != null) {
                    dao.upsertAll(listOf(plan.toEntity(userId)))
                }
                Result.Success(plan)
            }
            is Result.Error -> {
                Log.e("AIFIT_DEBUG", "[REPO][DETAIL] ERROR planId=$planId — ${remote.exception.message}")
                if (cached != null) {
                    Log.d("AIFIT_DEBUG", "[REPO][DETAIL] using detail cache for planId=$planId")
                    Result.Success(cached)
                } else {
                    remote
                }
            }
            else -> Result.Loading
        }
    }

    /**
     * Genera un plan estándar vía API y lo persiste en Room.
     *
     * @param request Parámetros de generación.
     * @return [Result.Error] si no hay sesión activa o falla la petición.
     */
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

    /**
     * Genera un plan adaptativo vía API y lo persiste en Room.
     *
     * @param request Parámetros adaptativos.
     * @return [Result.Error] si no hay sesión activa o falla la petición.
     */
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

    /**
     * Elimina un plan localmente de inmediato y confirma con el servidor, con rollback en error.
     *
     * @param planId Identificador del plan a eliminar.
     * @return [Result.Success] tras confirmación del servidor, o [Result.Error] con restauración local.
     */
    override suspend fun deleteTrainingPlan(planId: String): Result<Unit> {
        // 1. Snapshot for rollback if the network call fails.
        val planSnapshot = dao.getById(planId)

        // 2. Register in guard set — prevents any concurrent getTrainingPlans() emission
        //    from reinserting this plan via upsertAll while the delete API is in-flight.
        recentlyDeletedIds = recentlyDeletedIds + planId

        // 3. Remove from Room immediately so cache emissions never resurrect the plan
        //    during the API window (~1–4 s).
        dao.deleteById(planId)

        // 4. Confirm deletion with the server.
        return when (val remote = safeEmptyApiCall { apiService.deleteTrainingPlan(planId) }) {
            is Result.Success -> {
                Result.Success(Unit)
            }
            is Result.Error -> {
                planSnapshot?.let { dao.upsertAll(listOf(it)) }
                recentlyDeletedIds = recentlyDeletedIds - planId
                remote
            }
            else -> Result.Loading
        }
    }

    /**
     * Activa un plan en servidor y demota el plan activo previo a PAUSED en Room.
     *
     * @param planId Identificador del plan a activar.
     * @return [Result.Error] si no hay sesión activa o falla la activación.
     */
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

    /**
     * Obtiene el protocolo de calentamiento recomendado para un día concreto del plan.
     *
     * @param planId Identificador del plan.
     * @param dayId Identificador del día de entrenamiento.
     */
    override suspend fun getWarmUpProtocol(planId: String, dayId: String): Result<WarmUpProtocol> {
        return when (val remote = safeApiCall { apiService.getWarmUpProtocol(planId, dayId) }) {
            is Result.Success -> Result.Success(remote.data.toDomain())
            is Result.Error -> remote
            else -> Result.Loading
        }
    }

    /**
     * Lista sustituciones de ejercicio sugeridas por el backend para un ejercicio dado.
     *
     * @param exerciseId Identificador del ejercicio de entrenamiento.
     */
    override suspend fun getExerciseSubstitutions(exerciseId: String): Result<List<ExerciseSubstitution>> {
        return when (val remote = safeApiCall { apiService.getExerciseSubstitutions(exerciseId) }) {
            is Result.Success -> Result.Success(remote.data.map { it.toDomain() })
            is Result.Error -> remote
            else -> Result.Loading
        }
    }
}

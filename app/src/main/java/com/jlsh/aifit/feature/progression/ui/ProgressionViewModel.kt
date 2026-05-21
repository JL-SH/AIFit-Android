package com.jlsh.aifit.feature.progression.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.core.common.toMessage
import com.jlsh.aifit.feature.progression.domain.model.PlanProgressionSummary
import com.jlsh.aifit.feature.progression.domain.model.ProgressionRecommendation
import com.jlsh.aifit.feature.progression.domain.usecase.GetExerciseProgressionRecommendationUseCase
import com.jlsh.aifit.feature.progression.domain.usecase.GetFullPlanProgressionRecommendationsUseCase
import com.jlsh.aifit.feature.workout.domain.usecase.GetWorkoutHistoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Estado de la recomendación de progresión de un ejercicio concreto.
 */
sealed class RecommendationState {
    /** Sin petición activa (p. ej. sheet cerrado). */
    data object Idle : RecommendationState()

    /** Carga de la recomendación del ejercicio. */
    data object Loading : RecommendationState()

    /**
     * Recomendación disponible.
     *
     * @property data Carga, repeticiones y justificación sugeridas.
     */
    data class Success(val data: ProgressionRecommendation) : RecommendationState()

    /**
     * Error al cargar la recomendación.
     *
     * @property message Texto para mostrar al usuario.
     */
    data class Error(val message: String) : RecommendationState()
}

/**
 * Estado del resumen de progresión de un plan completo.
 */
sealed class PlanSummaryState {
    /** Sin petición activa. */
    data object Idle : PlanSummaryState()

    /** Carga del resumen del plan. */
    data object Loading : PlanSummaryState()

    /**
     * Resumen del plan disponible.
     *
     * @property data Recomendaciones agrupadas por ejercicio.
     */
    data class Success(val data: PlanProgressionSummary) : PlanSummaryState()

    /**
     * Error al cargar el resumen.
     *
     * @property message Texto para mostrar al usuario.
     */
    data class Error(val message: String) : PlanSummaryState()
}

/**
 * ViewModel de recomendaciones de progresión por ejercicio y por plan.
 *
 * **UiState expuesto** (sin canal de eventos; la UI observa los StateFlow):
 * - [recommendationState] — [RecommendationState]: recomendación de un ejercicio.
 * - [planSummaryState] — [PlanSummaryState]: resumen de todo el plan.
 * - [sessionCount]: número de sesiones completadas (contexto para la UI).
 *
 * @param getExerciseRecommendationUseCase Recomendación individual.
 * @param getPlanRecommendationsUseCase Resumen del plan.
 * @param getWorkoutHistoryUseCase Conteo de sesiones para contexto.
 */
@HiltViewModel
class ProgressionViewModel @Inject constructor(
    private val getExerciseRecommendationUseCase: GetExerciseProgressionRecommendationUseCase,
    private val getPlanRecommendationsUseCase: GetFullPlanProgressionRecommendationsUseCase,
    private val getWorkoutHistoryUseCase: GetWorkoutHistoryUseCase,
) : ViewModel() {

    private val _recommendationState = MutableStateFlow<RecommendationState>(RecommendationState.Idle)
    /** Estado de la recomendación del ejercicio seleccionado. */
    val recommendationState: StateFlow<RecommendationState> = _recommendationState.asStateFlow()

    private val _planSummaryState = MutableStateFlow<PlanSummaryState>(PlanSummaryState.Idle)
    /** Estado del resumen de progresión del plan. */
    val planSummaryState: StateFlow<PlanSummaryState> = _planSummaryState.asStateFlow()

    private val _sessionCount = MutableStateFlow<Int?>(null)
    /** Número de sesiones de entrenamiento registradas, o `null` mientras se calcula. */
    val sessionCount: StateFlow<Int?> = _sessionCount.asStateFlow()

    init {
        loadSessionCount()
    }

    private fun loadSessionCount() {
        viewModelScope.launch {
            when (val result = getWorkoutHistoryUseCase().first { it !is Result.Loading }) {
                is Result.Success -> _sessionCount.value = result.data.size
                is Result.Error -> _sessionCount.value = 0
                else -> Unit
            }
        }
    }

    /**
     * Carga la recomendación de progresión para un ejercicio.
     *
     * @param exerciseId Identificador del ejercicio.
     */
    fun loadExerciseRecommendation(exerciseId: String) {
        viewModelScope.launch {
            _recommendationState.value = RecommendationState.Loading
            when (val result = getExerciseRecommendationUseCase(exerciseId)) {
                is Result.Success -> _recommendationState.value = RecommendationState.Success(result.data)
                is Result.Error -> _recommendationState.value = RecommendationState.Error(result.exception.toMessage())
                else -> Unit
            }
        }
    }

    /**
     * Carga el resumen de progresión de todo el plan.
     *
     * @param planId Identificador del plan de entrenamiento.
     */
    fun loadPlanRecommendations(planId: String) {
        viewModelScope.launch {
            _planSummaryState.value = PlanSummaryState.Loading
            when (val result = getPlanRecommendationsUseCase(planId)) {
                is Result.Success -> _planSummaryState.value = PlanSummaryState.Success(result.data)
                is Result.Error -> _planSummaryState.value = PlanSummaryState.Error(result.exception.toMessage())
                else -> Unit
            }
        }
    }

    /** Restablece [recommendationState] a [RecommendationState.Idle] (p. ej. al cerrar el sheet). */
    fun resetRecommendationState() {
        _recommendationState.value = RecommendationState.Idle
    }

    /** Restablece [planSummaryState] a [PlanSummaryState.Idle]. */
    fun resetPlanSummaryState() {
        _planSummaryState.value = PlanSummaryState.Idle
    }
}


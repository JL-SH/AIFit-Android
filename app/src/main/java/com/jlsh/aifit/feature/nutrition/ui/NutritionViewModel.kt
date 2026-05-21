package com.jlsh.aifit.feature.nutrition.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.core.common.toMessage
import com.jlsh.aifit.feature.diet.domain.model.DietPlan
import com.jlsh.aifit.feature.diet.domain.usecase.DeleteDietPlanUseCase
import com.jlsh.aifit.feature.diet.domain.usecase.GetDietPlansUseCase
import com.jlsh.aifit.feature.diet.domain.usecase.SetActiveDietPlanUseCase
import com.jlsh.aifit.feature.nutrition.data.dto.AnalyzeMealFromTextRequestDto
import com.jlsh.aifit.feature.nutrition.data.dto.TrackMealRequestDto
import com.jlsh.aifit.feature.nutrition.data.dto.UpdateNutritionTargetRequestDto
import com.jlsh.aifit.feature.nutrition.domain.usecase.AnalyzeMealFromTextUseCase
import com.jlsh.aifit.feature.nutrition.domain.usecase.DeleteMealLogUseCase
import com.jlsh.aifit.feature.nutrition.domain.usecase.GetCurrentNutritionTargetUseCase
import com.jlsh.aifit.feature.nutrition.domain.usecase.GetNutritionLogUseCase
import com.jlsh.aifit.feature.nutrition.domain.usecase.TrackMealUseCase
import com.jlsh.aifit.feature.nutrition.domain.usecase.UpdateNutritionTargetUseCase
import com.jlsh.aifit.feature.nutrition.ui.state.NutritionHubUiState
import com.jlsh.aifit.feature.nutrition.ui.state.NutritionTargetUiState
import com.jlsh.aifit.feature.nutrition.ui.state.NutritionUiEvent
import com.jlsh.aifit.feature.nutrition.ui.state.TodayState
import com.jlsh.aifit.feature.nutrition.ui.state.TrackMealUiState
import com.jlsh.aifit.feature.training.domain.model.PlanStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

/**
 * ViewModel del módulo de nutrición: hub diario, registro de comidas y objetivos.
 *
 * Expone:
 * - [hubState]: [NutritionHubUiState] (hoy, planes de dieta, pestaña y filtros).
 * - [trackMealState]: [TrackMealUiState] para guardar o analizar comidas.
 * - [targetState]: [NutritionTargetUiState] para editar objetivos.
 * - [selectedTabIndex]: índice de pestaña del hub (0 = Hoy, 1 = Plan, 2 = Compras).
 * - [events]: flujo de [NutritionUiEvent] (navegación, sheet, snackbars, [NutritionUiEvent.MealDeleted]).
 */
@OptIn(FlowPreview::class)
@HiltViewModel
class NutritionViewModel @Inject constructor(
    private val getNutritionLogUseCase: GetNutritionLogUseCase,
    private val getCurrentNutritionTargetUseCase: GetCurrentNutritionTargetUseCase,
    private val getDietPlansUseCase: GetDietPlansUseCase,
    private val trackMealUseCase: TrackMealUseCase,
    private val analyzeMealFromTextUseCase: AnalyzeMealFromTextUseCase,
    private val deleteMealLogUseCase: DeleteMealLogUseCase,
    private val updateNutritionTargetUseCase: UpdateNutritionTargetUseCase,
    private val setActiveDietPlanUseCase: SetActiveDietPlanUseCase,
    private val deleteDietPlanUseCase: DeleteDietPlanUseCase,
) : ViewModel() {

    private val _hubState = MutableStateFlow<NutritionHubUiState>(NutritionHubUiState.Loading)

    /** Estado observable del hub de nutrición ([NutritionHubUiState]). */
    val hubState: StateFlow<NutritionHubUiState> = _hubState.asStateFlow()

    private val _trackMealState = MutableStateFlow<TrackMealUiState>(TrackMealUiState.Idle)

    /** Estado observable del registro/análisis de comida ([TrackMealUiState]). */
    val trackMealState: StateFlow<TrackMealUiState> = _trackMealState.asStateFlow()

    private val _targetState = MutableStateFlow<NutritionTargetUiState>(NutritionTargetUiState.Loading)

    /** Estado observable de la pantalla de objetivos ([NutritionTargetUiState]). */
    val targetState: StateFlow<NutritionTargetUiState> = _targetState.asStateFlow()

    private val _selectedTabIndex = MutableStateFlow(0)

    /** Pestaña activa del hub: 0 Hoy, 1 Plan de dieta, 2 Compras. */
    val selectedTabIndex: StateFlow<Int> = _selectedTabIndex.asStateFlow()

    private val _events = Channel<NutritionUiEvent>(Channel.BUFFERED)

    /**
     * Eventos de UI de un solo consumo: navegación, [NutritionUiEvent.ShowTrackMealSheet],
     * [NutritionUiEvent.ShowSnackbar], [NutritionUiEvent.MealDeleted], etc.
     */
    val events = _events.receiveAsFlow()

    private var fetchDietPlansJob: Job? = null
    private var isDeletingPlan = false

    private val _refreshTrigger = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    init {
        fetchHubData()
        viewModelScope.launch {
            _refreshTrigger
                .debounce(500L)
                .collect { fetchHubData() }
        }
    }

    // ===== HUB =====

    private fun fetchHubData() {
        fetchDietPlansJob?.cancel()
        fetchDietPlansJob = viewModelScope.launch {
            val logDeferred = async {
                getNutritionLogUseCase(LocalDate.now())
                    .first { it !is Result.Loading }
                    .let { r -> if (r is Result.Success) r.data else null }
            }
            val targetDeferred = async {
                getCurrentNutritionTargetUseCase()
                    .first { it !is Result.Loading }
                    .let { r -> if (r is Result.Success) r.data else null }
            }

            val log = logDeferred.await()
            val target = targetDeferred.await()

            if (_hubState.value is NutritionHubUiState.Loading) {
                _hubState.value = NutritionHubUiState.Loading
            }

            getDietPlansUseCase().collect { result ->
                when (result) {
                    is Result.Success -> {
                        val plans = sortDietPlans(result.data)
                        Log.d("AIFIT_DEBUG", "[NutritionVM] fetchHubData: dietPlans=${plans.size}")
                        val current = _hubState.value
                        val filter = (current as? NutritionHubUiState.Success)?.selectedDietPlanFilter
                        _hubState.value = NutritionHubUiState.Success(
                            todayState = TodayState(nutritionLog = log, target = target),
                            dietPlans = plans,
                            selectedTabIndex = _selectedTabIndex.value,
                            selectedDietPlanFilter = filter,
                            isActivatingPlan = (current as? NutritionHubUiState.Success)?.isActivatingPlan == true,
                        )
                    }
                    is Result.Error -> {
                        if (_hubState.value is NutritionHubUiState.Loading) {
                            _hubState.value = NutritionHubUiState.Error(result.exception.toMessage())
                        }
                    }
                    is Result.Loading -> {
                        if (_hubState.value !is NutritionHubUiState.Success) {
                            _hubState.value = NutritionHubUiState.Loading
                        }
                    }
                }
            }
        }
    }

    private fun sortDietPlans(plans: List<DietPlan>): List<DietPlan> =
        plans.sortedByDescending { it.createdAt }

    /**
     * Indica si el usuario tiene al menos un plan de dieta en estado [PlanStatus.ACTIVE].
     *
     * @return true si existe un plan activo en [hubState] exitoso.
     */
    fun hasActiveDietPlan(): Boolean {
        val state = _hubState.value as? NutritionHubUiState.Success ?: return false
        return state.dietPlans.any { it.status == PlanStatus.ACTIVE }
    }

    /**
     * Cambia la pestaña del hub y sincroniza [selectedTabIndex] con [hubState].
     *
     * @param index Índice de pestaña (0–2).
     */
    fun onTabSelected(index: Int) {
        _selectedTabIndex.value = index
        val current = _hubState.value
        if (current is NutritionHubUiState.Success) {
            _hubState.value = current.copy(selectedTabIndex = index)
        }
    }

    /**
     * Aplica filtro por estado de plan en la pestaña de planes de dieta.
     *
     * @param status Estado a filtrar; null muestra todos los planes.
     */
    fun onDietPlanFilterChanged(status: PlanStatus?) {
        val current = _hubState.value
        if (current is NutritionHubUiState.Success) {
            _hubState.value = current.copy(selectedDietPlanFilter = status)
        }
    }

    /** Recarga log del día, objetivos y planes de dieta (con debounce interno). */
    fun onRefresh() {
        if (isDeletingPlan) return
        _refreshTrigger.tryEmit(Unit)
    }

    /**
     * Elimina una comida del diario y refresca el hub; emite [NutritionUiEvent.MealDeleted].
     *
     * @param mealId Identificador de la comida registrada.
     */
    fun onDeleteMeal(mealId: String) {
        viewModelScope.launch {
            when (val result = deleteMealLogUseCase(mealId)) {
                is Result.Success -> {
                    emitEvent(NutritionUiEvent.MealDeleted)
                    emitEvent(NutritionUiEvent.ShowSnackbar("Comida eliminada"))
                    fetchHubData()
                }
                is Result.Error -> emitEvent(NutritionUiEvent.ShowSnackbar(result.exception.toMessage()))
                else -> Unit
            }
        }
    }

    // ===== TRACK MEAL =====

    /**
     * Registra una comida manual y emite [NutritionUiEvent.NavigateToHome] al guardar.
     *
     * @param request Datos de la comida y alimentos.
     */
    fun onTrackMeal(request: TrackMealRequestDto) {
        viewModelScope.launch {
            _trackMealState.value = TrackMealUiState.Saving
            when (val result = trackMealUseCase(request)) {
                is Result.Success -> {
                    _trackMealState.value = TrackMealUiState.Saved
                    emitEvent(NutritionUiEvent.NavigateToHome)
                }
                is Result.Error -> {
                    _trackMealState.value = TrackMealUiState.Error(result.exception.toMessage())
                    emitEvent(NutritionUiEvent.ShowSnackbar(result.exception.toMessage()))
                }
                else -> Unit
            }
        }
    }

    /**
     * Analiza texto libre con IA, registra la comida y navega al hub al completar.
     *
     * @param request Texto, tipo, hora y fecha de la comida.
     */
    fun onAnalyzeMealFromText(request: AnalyzeMealFromTextRequestDto) {
        viewModelScope.launch {
            _trackMealState.value = TrackMealUiState.Analyzing
            val startTime = System.currentTimeMillis()

            when (val result = analyzeMealFromTextUseCase(request)) {
                is Result.Success -> {
                    ensureMinAnimationDuration(startTime)
                    _trackMealState.value = TrackMealUiState.Saved
                    emitEvent(NutritionUiEvent.NavigateToHome)
                }
                is Result.Error -> {
                    ensureMinAnimationDuration(startTime)
                    _trackMealState.value = TrackMealUiState.Error(result.exception.toMessage())
                    emitEvent(NutritionUiEvent.ShowSnackbar(result.exception.toMessage()))
                }
                else -> Unit
            }
        }
    }

    /** Restablece [trackMealState] a [TrackMealUiState.Idle] al entrar en la pantalla de registro. */
    fun resetTrackMealState() {
        _trackMealState.value = TrackMealUiState.Idle
    }

    // ===== NUTRITION TARGET =====

    /** Carga objetivos actuales y actualiza [targetState] a [NutritionTargetUiState.Ready] o error. */
    fun loadNutritionTarget() {
        viewModelScope.launch {
            _targetState.value = NutritionTargetUiState.Loading
            getCurrentNutritionTargetUseCase().collect { result ->
                when (result) {
                    is Result.Success -> {
                        val t = result.data
                        _targetState.value = NutritionTargetUiState.Ready(
                            calorieTarget = t.calorieTarget.toString(),
                            proteinTarget = t.proteinTarget.toInt().toString(),
                            carbsTarget = t.carbsTarget.toInt().toString(),
                            fatTarget = t.fatTarget.toInt().toString(),
                            setBy = t.setBy.name.replace("_", " "),
                        )
                    }
                    is Result.Error -> {
                        _targetState.value = NutritionTargetUiState.Error(result.exception.toMessage())
                    }
                    else -> Unit
                }
            }
        }
    }

    /**
     * Persiste nuevos objetivos nutricionales y emite [NutritionUiEvent.NavigateBack] al éxito.
     *
     * @param calories Objetivo calórico como texto.
     * @param protein Objetivo de proteína (g) como texto.
     * @param carbs Objetivo de carbohidratos (g) como texto.
     * @param fat Objetivo de grasas (g) como texto.
     */
    fun onUpdateTarget(calories: String, protein: String, carbs: String, fat: String) {
        viewModelScope.launch {
            val current = _targetState.value
            if (current is NutritionTargetUiState.Ready) {
                _targetState.value = current.copy(isSaving = true)
            }

            val request = UpdateNutritionTargetRequestDto(
                calorieTarget = calories.toIntOrNull(),
                proteinTarget = protein.toDoubleOrNull(),
                carbsTarget = carbs.toDoubleOrNull(),
                fatTarget = fat.toDoubleOrNull(),
            )

            when (val result = updateNutritionTargetUseCase(request)) {
                is Result.Success -> {
                    fetchHubData()
                    emitEvent(NutritionUiEvent.NavigateBack)
                    emitEvent(NutritionUiEvent.ShowSnackbar("Objetivos actualizados"))
                }
                is Result.Error -> {
                    if (current is NutritionTargetUiState.Ready) {
                        _targetState.value = current.copy(isSaving = false)
                    }
                    emitEvent(NutritionUiEvent.ShowSnackbar(result.exception.toMessage()))
                }
                else -> Unit
            }
        }
    }

    // ===== NAVIGATION =====

    /** Emite [NutritionUiEvent.ShowTrackMealSheet] para elegir modo de registro de comida. */
    fun onFabClicked() {
        emitEvent(NutritionUiEvent.ShowTrackMealSheet)
    }

    /**
     * Emite [NutritionUiEvent.NavigateToDietDetail] con el plan seleccionado.
     *
     * @param planId Identificador del plan.
     */
    fun onDietPlanClicked(planId: String) {
        emitEvent(NutritionUiEvent.NavigateToDietDetail(planId))
    }

    /**
     * Activa un plan con actualización optimista en [hubState] y sincronización con servidor.
     *
     * @param planId Identificador del plan a activar.
     */
    fun onActivateDietPlan(planId: String) {
        val current = _hubState.value as? NutritionHubUiState.Success ?: return
        val previousState = current

        val optimisticPlans = sortDietPlans(
            current.dietPlans.map { plan ->
                when {
                    plan.id == planId -> plan.copy(status = PlanStatus.ACTIVE)
                    plan.status == PlanStatus.ACTIVE -> plan.copy(status = PlanStatus.PAUSED)
                    else -> plan
                }
            },
        )
        _hubState.value = current.copy(dietPlans = optimisticPlans, isActivatingPlan = true)

        viewModelScope.launch {
            when (val result = setActiveDietPlanUseCase(planId)) {
                is Result.Success -> {
                    fetchHubData()
                    fetchDietPlansJob?.join()
                    _hubState.update { state ->
                        if (state is NutritionHubUiState.Success) {
                            state.copy(isActivatingPlan = false)
                        } else state
                    }
                }
                is Result.Error -> {
                    _hubState.value = previousState.copy(isActivatingPlan = false)
                    emitEvent(NutritionUiEvent.ShowSnackbar(result.exception.toMessage()))
                }
                else -> Unit
            }
        }
    }

    /**
     * Elimina un plan no activo con UI optimista; revierte y muestra snackbar si falla la red.
     *
     * @param planId Identificador del plan a eliminar.
     */
    fun onDeleteDietPlan(planId: String) {
        val current = _hubState.value as? NutritionHubUiState.Success ?: return
        val plan = current.dietPlans.firstOrNull { it.id == planId } ?: return

        if (plan.status == PlanStatus.ACTIVE) {
            emitEvent(NutritionUiEvent.ShowSnackbar("No puedes eliminar un plan activo. Activa otro plan primero."))
            return
        }

        isDeletingPlan = true
        fetchDietPlansJob?.cancel()
        _hubState.value = current.copy(dietPlans = current.dietPlans.filter { it.id != planId })

        viewModelScope.launch {
            when (val result = deleteDietPlanUseCase(planId)) {
                is Result.Success -> {
                    emitEvent(NutritionUiEvent.ShowSnackbar("Plan eliminado"))
                    fetchHubData()
                    fetchDietPlansJob?.join()
                    isDeletingPlan = false
                }
                is Result.Error -> {
                    _hubState.value = current
                    fetchHubData()
                    isDeletingPlan = false
                    emitEvent(NutritionUiEvent.ShowSnackbar(result.exception.toMessage()))
                }
                else -> Unit
            }
        }
    }

    /** Emite [NutritionUiEvent.NavigateToGenerateDiet] para crear un nuevo plan. */
    fun onGenerateDietClicked() {
        emitEvent(NutritionUiEvent.NavigateToGenerateDiet)
    }

    /** Emite [NutritionUiEvent.NavigateToNutritionTarget] para editar objetivos. */
    fun onNavigateToTarget() {
        emitEvent(NutritionUiEvent.NavigateToNutritionTarget)
    }

    // ===== HELPERS =====

    private fun emitEvent(event: NutritionUiEvent) {
        viewModelScope.launch { _events.send(event) }
    }

    private suspend fun ensureMinAnimationDuration(startTime: Long) {
        val elapsed = System.currentTimeMillis() - startTime
        if (elapsed < MIN_ANIMATION_DURATION) {
            delay(MIN_ANIMATION_DURATION - elapsed)
        }
    }

    companion object {
        private const val MIN_ANIMATION_DURATION = 2000L
    }
}

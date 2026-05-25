package com.jlsh.aifit.feature.nutrition.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.core.common.lastSuccessOrNull
import com.jlsh.aifit.core.common.toMessage
import com.jlsh.aifit.feature.diet.domain.model.DietPlan
import com.jlsh.aifit.feature.diet.domain.model.Meal
import com.jlsh.aifit.feature.diet.domain.usecase.DeleteDietPlanUseCase
import com.jlsh.aifit.feature.diet.domain.usecase.GetDietPlanDetailUseCase
import com.jlsh.aifit.feature.diet.domain.usecase.GetDietPlansUseCase
import com.jlsh.aifit.feature.diet.domain.usecase.SetActiveDietPlanUseCase
import com.jlsh.aifit.feature.diet.domain.util.mealsForToday
import com.jlsh.aifit.feature.nutrition.data.dto.AnalyzeMealFromTextRequestDto
import com.jlsh.aifit.feature.nutrition.data.dto.TrackMealRequestDto
import com.jlsh.aifit.feature.nutrition.data.dto.UpdateNutritionTargetRequestDto
import com.jlsh.aifit.feature.nutrition.domain.usecase.AnalyzeMealFromTextUseCase
import com.jlsh.aifit.feature.nutrition.domain.usecase.DeleteMealLogUseCase
import com.jlsh.aifit.feature.nutrition.domain.usecase.GetCurrentNutritionTargetUseCase
import com.jlsh.aifit.feature.nutrition.domain.usecase.GetNutritionLogUseCase
import com.jlsh.aifit.feature.nutrition.domain.usecase.TrackMealUseCase
import com.jlsh.aifit.feature.nutrition.domain.usecase.UpdateNutritionTargetUseCase
import com.jlsh.aifit.feature.nutrition.domain.util.toTrackMealRequestDto
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
 * ViewModel of the nutrition module: daily hub, meal log and goals.
 *
 * Expone:
 * - [hubState]: [NutritionHubUiState] (today, diet plans, tab and filters).
 * - [trackMealState]: [TrackMealUiState] to save or analyze meals.
 * - [targetState]: [NutritionTargetUiState] to edit targets.
 * - [selectedTabIndex]: hub tab index (0 = Today, 1 = Plan, 2 = Purchases).
 * - [events]: [NutritionUiEvent] flow (navigation, sheet, snackbars, [NutritionUiEvent.MealDeleted]).
 */
@OptIn(FlowPreview::class)
@HiltViewModel
class NutritionViewModel @Inject constructor(
    private val getNutritionLogUseCase: GetNutritionLogUseCase,
    private val getCurrentNutritionTargetUseCase: GetCurrentNutritionTargetUseCase,
    private val getDietPlansUseCase: GetDietPlansUseCase,
    private val getDietPlanDetailUseCase: GetDietPlanDetailUseCase,
    private val trackMealUseCase: TrackMealUseCase,
    private val analyzeMealFromTextUseCase: AnalyzeMealFromTextUseCase,
    private val deleteMealLogUseCase: DeleteMealLogUseCase,
    private val updateNutritionTargetUseCase: UpdateNutritionTargetUseCase,
    private val setActiveDietPlanUseCase: SetActiveDietPlanUseCase,
    private val deleteDietPlanUseCase: DeleteDietPlanUseCase,
) : ViewModel() {

    private val _hubState = MutableStateFlow<NutritionHubUiState>(NutritionHubUiState.Loading)

    /** Nutrition hub observable state ([NutritionHubUiState]).*/
    val hubState: StateFlow<NutritionHubUiState> = _hubState.asStateFlow()

    private val _trackMealState = MutableStateFlow<TrackMealUiState>(TrackMealUiState.Idle)

    /** Observable state of the food log/analysis ([TrackMealUiState]).*/
    val trackMealState: StateFlow<TrackMealUiState> = _trackMealState.asStateFlow()

    private val _targetState = MutableStateFlow<NutritionTargetUiState>(NutritionTargetUiState.Loading)

    /** Target screen observable state ([NutritionTargetUiState]).*/
    val targetState: StateFlow<NutritionTargetUiState> = _targetState.asStateFlow()

    private val _selectedTabIndex = MutableStateFlow(0)

    /** Active hub tab: 0 Today, 1 Diet Plan, 2 Purchases.*/
    val selectedTabIndex: StateFlow<Int> = _selectedTabIndex.asStateFlow()

    private val _events = Channel<NutritionUiEvent>(Channel.BUFFERED)

    /**
     * One-time UI Events: Navigation, [NutritionUiEvent.ShowTrackMealSheet],
     * [NutritionUiEvent.ShowSnackbar], [NutritionUiEvent.MealDeleted], etc.
     */
    val events = _events.receiveAsFlow()

    private val _planPickerMeals = MutableStateFlow<List<Meal>>(emptyList())

    /** Active plan meals for today; They are loaded when opening the picker from the plan.*/
    val planPickerMeals: StateFlow<List<Meal>> = _planPickerMeals.asStateFlow()

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
                getNutritionLogUseCase(LocalDate.now()).lastSuccessOrNull()
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
     * Indicates whether the user has at least one diet plan in status [PlanStatus.ACTIVE].
     *
     * @return true if an active plan exists in [hubState] successful.
     */
    fun hasActiveDietPlan(): Boolean {
        val state = _hubState.value as? NutritionHubUiState.Success ?: return false
        return state.dietPlans.any { it.status == PlanStatus.ACTIVE }
    }

    /**
     * Change the hub tab and sync [selectedTabIndex] with [hubState].
     *
     * @param index Tab index (0–2).
     */
    fun onTabSelected(index: Int) {
        _selectedTabIndex.value = index
        val current = _hubState.value
        if (current is NutritionHubUiState.Success) {
            _hubState.value = current.copy(selectedTabIndex = index)
        }
    }

    /**
     * Apply filter by plan status in the diet plans tab.
     *
     * @param status State to filter; null shows all plans.
     */
    fun onDietPlanFilterChanged(status: PlanStatus?) {
        val current = _hubState.value
        if (current is NutritionHubUiState.Success) {
            _hubState.value = current.copy(selectedDietPlanFilter = status)
        }
    }

    /** Reload log of the day, goals and diet plans (with internal debounce).*/
    fun onRefresh() {
        if (isDeletingPlan) return
        _refreshTrigger.tryEmit(Unit)
    }

    /**
     * Eliminate a meal from the daily and refresh the hub; emits [NutritionUiEvent.MealDeleted].
     *
     * @param mealId Identifier of the registered meal.
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
     * Registers a manual meal and emits [NutritionUiEvent.NavigateToHome] when saving.
     *
     * @param request Food and meal data.
     */
    fun onTrackMeal(request: TrackMealRequestDto) {
        viewModelScope.launch {
            _trackMealState.value = TrackMealUiState.Saving
            when (val result = trackMealUseCase(request)) {
                is Result.Success -> {
                    _trackMealState.value = TrackMealUiState.Saved
                    fetchHubData()
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
     * Analyze free text with AI, log food, and navigate to hub upon completion.
     *
     * @param request Text, type, time and date of the meal.
     */
    fun onAnalyzeMealFromText(request: AnalyzeMealFromTextRequestDto) {
        viewModelScope.launch {
            _trackMealState.value = TrackMealUiState.Analyzing
            val startTime = System.currentTimeMillis()

            when (val result = analyzeMealFromTextUseCase(request)) {
                is Result.Success -> {
                    ensureMinAnimationDuration(startTime)
                    _trackMealState.value = TrackMealUiState.Saved
                    fetchHubData()
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

    /** Resets [trackMealState] to [TrackMealUiState.Idle] upon entering the registration screen.*/
    fun resetTrackMealState() {
        _trackMealState.value = TrackMealUiState.Idle
    }

    // ===== NUTRITION TARGET =====

    /** Loads current targets and updates [targetState] to [NutritionTargetUiState.Ready] or error.*/
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
     * Persist new nutritional goals and cast [NutritionUiEvent.NavigateBack] to success.
     *
     * @param calories Calorie target as text.
     * @param protein Target protein (g) as text.
     * @param carbs Target carbs (g) as text.
     * @param fat Target fat (g) as text.
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

    /** Issue [NutritionUiEvent.ShowTrackMealSheet] to choose meal tracking mode.*/
    fun onFabClicked() {
        emitEvent(NutritionUiEvent.ShowTrackMealSheet)
    }

    /** Load today's meals from the active plan and open the picker.*/
    fun onShowPlanMealPicker() {
        viewModelScope.launch {
            _planPickerMeals.value = loadActivePlanMealsForToday()
            emitEvent(NutritionUiEvent.ShowPlanMealPicker)
        }
    }

    /**
     * Log a meal from the active diet plan and refresh the hub.
     *
     * @param meal Food selected in the picker.
     */
    fun onTrackMealFromPlan(meal: Meal) {
        viewModelScope.launch {
            when (val result = trackMealUseCase(meal.toTrackMealRequestDto())) {
                is Result.Success -> {
                    fetchHubData()
                    emitEvent(NutritionUiEvent.ShowSnackbar("Comida del plan registrada"))
                }
                is Result.Error -> {
                    emitEvent(NutritionUiEvent.ShowSnackbar(result.exception.toMessage()))
                }
                is Result.Loading -> Unit
            }
        }
    }

    /**
     * Issues [NutritionUiEvent.NavigateToDietDetail] with the selected plan.
     *
     * @param planId Plan identifier.
     */
    fun onDietPlanClicked(planId: String) {
        emitEvent(NutritionUiEvent.NavigateToDietDetail(planId))
    }

    /**
     * Activate a plan with optimistic update in [hubState] and synchronization with server.
     *
     * @param planId Identifier of the plan to activate.
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
     * Delete a non-active plan with optimistic UI; reverts and shows snackbar if network fails.
     *
     * @param planId Identifier of the plan to delete.
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

    /** Issue [NutritionUiEvent.NavigateToGenerateDiet] to create a new plan.*/
    fun onGenerateDietClicked() {
        emitEvent(NutritionUiEvent.NavigateToGenerateDiet)
    }

    /** Issues [NutritionUiEvent.NavigateToNutritionTarget] to edit targets.*/
    fun onNavigateToTarget() {
        emitEvent(NutritionUiEvent.NavigateToNutritionTarget)
    }

    // ===== HELPERS =====

    private suspend fun loadActivePlanMealsForToday(): List<Meal> {
        val hub = _hubState.value as? NutritionHubUiState.Success ?: return emptyList()
        val activePlan = hub.dietPlans.firstOrNull { it.status == PlanStatus.ACTIVE }
            ?: return emptyList()
        val planWithDays = if (activePlan.days.isNotEmpty()) {
            activePlan
        } else {
            when (val result = getDietPlanDetailUseCase(activePlan.id)) {
                is Result.Success -> result.data
                else -> return emptyList()
            }
        }
        return planWithDays.mealsForToday()
    }

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

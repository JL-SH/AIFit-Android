package com.jlsh.aifit.feature.home.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jlsh.aifit.core.common.AppException
import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.feature.home.domain.model.HomeBootstrap
import com.jlsh.aifit.feature.home.domain.usecase.GetCachedHomeBootstrapUseCase
import com.jlsh.aifit.feature.home.domain.usecase.GetHomeBootstrapUseCase
import com.jlsh.aifit.feature.diet.domain.model.DietPlan
import com.jlsh.aifit.feature.diet.domain.model.Meal
import com.jlsh.aifit.feature.diet.domain.model.MealType
import com.jlsh.aifit.feature.diet.domain.DietActivePlanNotifier
import com.jlsh.aifit.feature.diet.domain.usecase.GetCachedDietPlansUseCase
import com.jlsh.aifit.feature.diet.domain.usecase.GetDietPlanDetailUseCase
import com.jlsh.aifit.feature.diet.domain.usecase.GetDietPlansUseCase
import com.jlsh.aifit.feature.gamification.domain.model.AchievementDefinition
import com.jlsh.aifit.feature.gamification.domain.model.Streak
import com.jlsh.aifit.feature.gamification.domain.model.StreakType
import com.jlsh.aifit.feature.gamification.domain.model.UserAchievement
import com.jlsh.aifit.feature.gamification.domain.usecase.GetAllAchievementDefinitionsUseCase
import com.jlsh.aifit.feature.gamification.domain.usecase.GetUserAchievementsUseCase
import com.jlsh.aifit.feature.gamification.domain.usecase.GetUserStreaksUseCase
import com.jlsh.aifit.feature.home.ui.state.HomeUiEvent
import com.jlsh.aifit.feature.home.ui.state.HomeUiState
import com.jlsh.aifit.feature.home.ui.state.ActivePlanSummary
import com.jlsh.aifit.feature.home.ui.state.NextMealState
import com.jlsh.aifit.feature.home.ui.state.TodayNutritionState
import com.jlsh.aifit.feature.home.ui.state.TodayTrainingState
import com.jlsh.aifit.feature.nutrition.domain.NutritionLogChangeNotifier
import com.jlsh.aifit.feature.nutrition.domain.model.NutritionLog
import com.jlsh.aifit.feature.nutrition.domain.model.NutritionTarget
import com.jlsh.aifit.feature.nutrition.domain.usecase.GetCurrentNutritionTargetUseCase
import com.jlsh.aifit.feature.nutrition.domain.usecase.GetNutritionLogUseCase
import com.jlsh.aifit.feature.nutrition.domain.usecase.TrackMealUseCase
import com.jlsh.aifit.feature.nutrition.data.dto.TrackFoodItemRequestDto
import com.jlsh.aifit.feature.nutrition.data.dto.TrackMealRequestDto
import com.jlsh.aifit.feature.progress.data.dto.LogBodyWeightRequestDto
import com.jlsh.aifit.feature.progress.domain.model.BodyWeightLog
import com.jlsh.aifit.feature.progress.domain.model.WeeklyProgressSummary
import com.jlsh.aifit.feature.progress.domain.usecase.GetBodyWeightHistoryUseCase
import com.jlsh.aifit.feature.progress.domain.usecase.GetWeeklyProgressSummaryUseCase
import com.jlsh.aifit.feature.progress.domain.usecase.LogBodyWeightUseCase
import com.jlsh.aifit.feature.training.domain.model.PlanStatus
import com.jlsh.aifit.feature.training.domain.model.TrainingDayType
import com.jlsh.aifit.feature.training.domain.model.TrainingPlan
import com.jlsh.aifit.feature.training.domain.ActiveTrainingPlanChange
import com.jlsh.aifit.feature.training.domain.TrainingActivePlanNotifier
import com.jlsh.aifit.feature.training.domain.usecase.GetCachedTrainingPlansUseCase
import com.jlsh.aifit.feature.training.domain.usecase.GetTrainingPlanDetailUseCase
import com.jlsh.aifit.feature.training.domain.usecase.GetTrainingPlansUseCase
import com.jlsh.aifit.feature.training.domain.usecase.PrefetchTrainingPlanDetailsUseCase
import com.jlsh.aifit.feature.user.data.mapper.UserMapper.pickBestProfilePictureUrl
import com.jlsh.aifit.feature.user.domain.model.UserProfile
import com.jlsh.aifit.feature.user.domain.usecase.GetUserProfileUseCase
import com.jlsh.aifit.feature.workout.domain.WorkoutHistoryNotifier
import com.jlsh.aifit.feature.workout.domain.model.WorkoutLog
import com.jlsh.aifit.feature.workout.domain.usecase.GetCachedWorkoutLogsUseCase
import com.jlsh.aifit.feature.workout.domain.usecase.GetWorkoutHistoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject

/**
 * ViewModel of the home dashboard: profile, today's training and nutrition, streaks and weight.
 *
 * **UiState exposed** ([uiState] — [HomeUiState]):
 * - [HomeUiState.Loading]: initial loading of the dashboard; skeleton until full snapshot.
 * - [HomeUiState.Error]: critical failure (e.g. profile not loaded).
 * - [HomeUiState.Success]: dashboard with training, nutrition, next meal, weight and gamification cards.
 *
 * **Emitted events** ([events] — [HomeUiEvent]):
 * - [HomeUiEvent.NavigateToWorkoutSession]: start training session (planId, dayId).
 * - [HomeUiEvent.NavigateToTrainingDetail]: training plan detail.
 * - [HomeUiEvent.ShowTrackMealSheet]: Open food record mode selector.
 * - [HomeUiEvent.NavigateToProgressDashboard]: Weekly progress dashboard.
 * - [HomeUiEvent.NavigateToBodyWeight]: Body weight history.
 * - [HomeUiEvent.NavigateToGamification]: Achievements/streaks screen (tab).
 * - [HomeUiEvent.NavigateToProfile]: user profile.
 * - [HomeUiEvent.NavigateToGeneratePlan]: generate new training plan.
 * - [HomeUiEvent.ShowLogWeightSheet]: Open modal sheet to log weight.
 * - [HomeUiEvent.ShowSnackbar]: Temporary message (success or error saving weight).
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getUserProfileUseCase: GetUserProfileUseCase,
    private val getTrainingPlansUseCase: GetTrainingPlansUseCase,
    private val getTrainingPlanDetailUseCase: GetTrainingPlanDetailUseCase,
    private val getDietPlansUseCase: GetDietPlansUseCase,
    private val getDietPlanDetailUseCase: GetDietPlanDetailUseCase,
    private val getNutritionLogUseCase: GetNutritionLogUseCase,
    private val getCurrentNutritionTargetUseCase: GetCurrentNutritionTargetUseCase,
    private val getWeeklyProgressSummaryUseCase: GetWeeklyProgressSummaryUseCase,
    private val getUserStreaksUseCase: GetUserStreaksUseCase,
    private val getUserAchievementsUseCase: GetUserAchievementsUseCase,
    private val getAllDefinitionsUseCase: GetAllAchievementDefinitionsUseCase,
    private val getBodyWeightHistoryUseCase: GetBodyWeightHistoryUseCase,
    private val getWorkoutHistoryUseCase: GetWorkoutHistoryUseCase,
    private val logBodyWeightUseCase: LogBodyWeightUseCase,
    private val trackMealUseCase: TrackMealUseCase,
    private val getHomeBootstrapUseCase: GetHomeBootstrapUseCase,
    private val getCachedHomeBootstrapUseCase: GetCachedHomeBootstrapUseCase,
    private val getCachedTrainingPlansUseCase: GetCachedTrainingPlansUseCase,
    private val trainingActivePlanNotifier: TrainingActivePlanNotifier,
    private val prefetchTrainingPlanDetailsUseCase: PrefetchTrainingPlanDetailsUseCase,
    private val getCachedDietPlansUseCase: GetCachedDietPlansUseCase,
    private val dietActivePlanNotifier: DietActivePlanNotifier,
    private val workoutHistoryNotifier: WorkoutHistoryNotifier,
    private val nutritionLogChangeNotifier: NutritionLogChangeNotifier,
    private val getCachedWorkoutLogsUseCase: GetCachedWorkoutLogsUseCase,
) : ViewModel() {

    /** Guards bootstrap from overwriting a fresher local/optimistic training card. */
    private var localTrainingPlanRevisionMs: Long = 0L
    private var lastLocalTrainingPlanId: String? = null

    /** Guards bootstrap from clearing a just-completed workout on the home card. */
    private var localWorkoutCompletionRevisionMs: Long = 0L

    private var bootstrapJob: Job? = null

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)

    /** Home dashboard status; observe with `collectAsStateWithLifecycle`.*/
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _events = Channel<HomeUiEvent>(Channel.BUFFERED)

    /** Single-use navigation and UI events; consume on [HomeScreen].*/
    val events = _events.receiveAsFlow()

    private val _planPickerMeals = MutableStateFlow<List<Meal>>(emptyList())

    /** Meals from the active plan for the current day; They are loaded when you open the picker.*/
    val planPickerMeals: StateFlow<List<Meal>> = _planPickerMeals.asStateFlow()

    private var loadJob: Job? = null
    private var resumeDebounceJob: Job? = null
    @Volatile
    private var hasCompletedInitialLoad = false

    /** Skips the first [onResumed] after cold-start [loadAll] to avoid a duplicate network refresh. */
    private var suppressResumeRefreshOnce = false
    private var cachedActivePlanDetail: TrainingPlan? = null
    private var cachedActivePlanId: String? = null       // Fix 2: survives a null cachedActivePlanDetail
    private var cachedActivePlanSummary: ActivePlanSummary? = null
    private var cachedWeeklySummary: WeeklyProgressSummary? = null
    private var cachedActiveDietDetail: DietPlan? = null
    private var cachedActiveDietId: String? = null

    private val perfMarks = mutableListOf<Pair<String, Long>>()
    private var perfDumpedForSession = false

    init {
        loadAll()
        viewModelScope.launch {
            trainingActivePlanNotifier.activePlanChanges.collect { change ->
                when {
                    change.isRevert -> refreshTrainingFromLocalCache()
                    change.isOptimistic -> applyOptimisticActiveTrainingPlan(
                        planId = change.planId,
                        planName = change.planName,
                    )
                    else -> refreshTrainingFromLocalCache()
                }
            }
        }
        viewModelScope.launch {
            dietActivePlanNotifier.activePlanChanges.collect {
                refreshNutritionFromLocalCache()
            }
        }
        viewModelScope.launch {
            workoutHistoryNotifier.changes.collect { change ->
                applyWorkoutFinalized(change.log)
            }
        }
        viewModelScope.launch {
            nutritionLogChangeNotifier.changes.collect { log ->
                applyNutritionLogChanged(log)
            }
        }
    }

    /** Reload home: Room cache first, then a single bootstrap API call for network data.*/
    fun loadAll() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            if (_uiState.value is HomeUiState.Success) {
                scheduleBootstrapRefresh()
                return@launch
            }

            perfMarks.clear()
            mark("loadAll_start")
            _uiState.value = HomeUiState.Loading

            val bootstrapCacheSnapshot = loadBootstrapCacheSnapshot()
            if (bootstrapCacheSnapshot != null) {
                onFirstSuccessEmitted()
                applySuccessState(bootstrapCacheSnapshot, "loadAll_bootstrap_cache")
                hasCompletedInitialLoad = true
                suppressResumeRefreshOnce = true
                refreshBootstrapInBackground()
                return@launch
            }

            val cacheSnapshot = loadCacheFirstSnapshot()
            if (cacheSnapshot != null) {
                onFirstSuccessEmitted()
                applySuccessState(cacheSnapshot, "loadAll_cache")
                hasCompletedInitialLoad = true
                suppressResumeRefreshOnce = true
                refreshBootstrapInBackground()
                return@launch
            }

            val networkSnapshot = refreshFromBootstrapInternal()
            when {
                networkSnapshot != null -> {
                    onFirstSuccessEmitted()
                    applySuccessState(networkSnapshot, "loadAll_bootstrap")
                    hasCompletedInitialLoad = true
                    suppressResumeRefreshOnce = true
                }
                else -> {
                    _uiState.value = HomeUiState.Error("No se pudo cargar el perfil")
                }
            }
        }
    }

    /**
     * Start the day's training session if [TodayTrainingState] is available.
     *
     * @param planId Identifier of the active plan.
     */
    fun onStartSession(planId: String) {
        val state = _uiState.value as? HomeUiState.Success ?: return
        val dayId = state.todayTraining?.dayId ?: return
        emitEvent(HomeUiEvent.NavigateToWorkoutSession(planId, dayId))
    }

    /**
     * Navigate to the details of the training plan.
     *
     * @param planId Plan identifier.
     */
    fun onViewTrainingDetail(planId: String) {
        emitEvent(HomeUiEvent.NavigateToTrainingDetail(planId))
    }

    /** Open the bottom sheet to choose the food recording mode.*/
    fun onLogMeal() {
        emitEvent(HomeUiEvent.ShowTrackMealSheet)
    }

    /** Load today's meals from the active plan and open the picker.*/
    fun onShowPlanMealPicker() {
        viewModelScope.launch {
            _planPickerMeals.value = resolveTodayPlanMeals()
            emitEvent(HomeUiEvent.ShowPlanMealPicker)
        }
    }

    /**
     * Record a meal from the active diet plan and update the home nutritional summary.
     *
     * @param meal Food selected in the picker.
     */
    fun onTrackMealFromPlan(meal: Meal) {
        viewModelScope.launch {
            when (val result = trackMealUseCase(mealToTrackMealRequestDto(meal))) {
                is Result.Success -> {
                    val nutritionPair = loadNutritionCacheFirst()
                    val todayNutrition = deriveNutrition(nutritionPair.first, nutritionPair.second)
                    updateSuccessIfSuccess("track_meal_from_plan") { current ->
                        current.copy(todayNutrition = todayNutrition)
                    }
                    emitEvent(HomeUiEvent.ShowSnackbar("Comida del plan registrada"))
                }
                is Result.Error -> {
                    emitEvent(HomeUiEvent.ShowSnackbar(result.exception.userMessage()))
                }
                is Result.Loading -> Unit
            }
        }
    }

    /** Open the modal sheet to record body weight.*/
    fun onLogWeight() {
        emitEvent(HomeUiEvent.ShowLogWeightSheet)
    }

    /**
     * Persists a new weight record and updates [HomeUiState.Success.weightEntries].
     *
     * @param weight Weight in kilograms.
     */
    fun onSaveWeight(weight: Double) {
        viewModelScope.launch {
            val request = LogBodyWeightRequestDto(
                weight = weight,
                date = LocalDate.now().toString(),
            )
            when (val result = logBodyWeightUseCase(request)) {
                is Result.Success -> {
                    val newWeightEntries = firstSuccessWeightHistory()
                    updateSuccessIfSuccess("save_weight") { current ->
                        current.copy(weightEntries = newWeightEntries)
                    }
                    emitEvent(HomeUiEvent.ShowSnackbar("✓ Peso guardado"))
                    emitEvent(HomeUiEvent.WeightLoggedSuccessfully)
                }
                is Result.Error -> {
                    emitEvent(HomeUiEvent.ShowSnackbar(result.exception.userMessage()))
                }
                is Result.Loading -> Unit
            }
        }
    }

    /** Navigate to the weekly progress dashboard.*/
    fun onProgressDashboard() {
        emitEvent(HomeUiEvent.NavigateToProgressDashboard)
    }

    /** Navigate to the body weight history screen.*/
    fun onBodyWeight() {
        emitEvent(HomeUiEvent.NavigateToBodyWeight)
    }

    /**
     * Navigate to gamification (achievements or streaks).
     *
     * @param tab Target tab (e.g. `"ACHIEVEMENTS"`).
     */
    fun onGamification(tab: String) {
        emitEvent(HomeUiEvent.NavigateToGamification(tab))
    }

    /** Navigate to the user's profile.*/
    fun onProfile() {
        emitEvent(HomeUiEvent.NavigateToProfile)
    }

    /** Navigate to the flow of generating a new training plan.*/
    fun onCreatePlan() {
        emitEvent(HomeUiEvent.NavigateToGeneratePlan)
    }

    /** Silent refresh when returning to the screen (RESUMED cycle).*/
    fun onResumed() {
        if (hasCompletedInitialLoad && _uiState.value is HomeUiState.Success) {
            viewModelScope.launch {
                refreshTrainingFromLocalCache()
                refreshNutritionFromLocalCache()
                refreshTodayTrainingFromLocalCache()
            }
        }
        resumeDebounceJob?.cancel()
        resumeDebounceJob = viewModelScope.launch {
            delay(RESUME_DEBOUNCE_MS)
            if (!hasCompletedInitialLoad) return@launch
            if (suppressResumeRefreshOnce) {
                suppressResumeRefreshOnce = false
                return@launch
            }
            if (loadJob?.isActive == true) return@launch
            refreshProfileHeader()
            scheduleBootstrapRefresh()
        }
    }

    // ── Private helpers ──

    private fun markLocalTrainingPlanChange(planId: String) {
        lastLocalTrainingPlanId = planId
        localTrainingPlanRevisionMs = System.currentTimeMillis()
    }

    private fun shouldPreserveLocalTraining(incoming: HomeUiState.Success): Boolean {
        val guardPlanId = lastLocalTrainingPlanId ?: cachedActivePlanId ?: return false
        if (incoming.activePlan?.id == guardPlanId) return false
        val ageMs = System.currentTimeMillis() - localTrainingPlanRevisionMs
        return localTrainingPlanRevisionMs > 0L && ageMs in 0..LOCAL_TRAINING_GUARD_MS
    }

    private fun markLocalWorkoutCompletion() {
        localWorkoutCompletionRevisionMs = System.currentTimeMillis()
    }

    private fun shouldPreserveLocalWorkoutCompletion(incoming: HomeUiState.Success): Boolean {
        val current = _uiState.value as? HomeUiState.Success ?: return false
        val wasCompleted = current.todayTraining?.isCompleted == true
        val incomingCompleted = incoming.todayTraining?.isCompleted == true
        if (!wasCompleted || incomingCompleted) return false
        val ageMs = System.currentTimeMillis() - localWorkoutCompletionRevisionMs
        return localWorkoutCompletionRevisionMs > 0L && ageMs in 0..LOCAL_WORKOUT_GUARD_MS
    }

    /**
     * Marks today's session complete immediately when a workout is finalized (Room already updated).
     */
    private fun applyNutritionLogChanged(log: NutritionLog) {
        if (log.date != LocalDate.now()) return
        val current = _uiState.value as? HomeUiState.Success ?: return
        val previous = current.todayNutrition
        val updated = deriveNutrition(log, null)?.let { fresh ->
            if (previous == null) return@let fresh
            fresh.copy(
                calorieTarget = previous.calorieTarget,
                proteinTarget = previous.proteinTarget,
                carbsTarget = previous.carbsTarget,
                fatTarget = previous.fatTarget,
            )
        } ?: return
        if (current.todayNutrition == updated) return
        updateSuccessIfSuccess("nutrition_log_changed") { state ->
            state.copy(todayNutrition = updated)
        }
    }

    private fun applyWorkoutFinalized(log: WorkoutLog) {
        if (!log.isLocked) {
            viewModelScope.launch { refreshTodayTrainingFromLocalCache() }
            return
        }
        val current = _uiState.value as? HomeUiState.Success ?: return
        val training = current.todayTraining
        if (training != null &&
            log.trainingPlanId == training.planId &&
            log.trainingDayId == training.dayId
        ) {
            markLocalWorkoutCompletion()
            updateSuccessIfSuccess("workout_finalized") { state ->
                state.copy(todayTraining = training.copy(isCompleted = true))
            }
            return
        }
        viewModelScope.launch { refreshTodayTrainingFromLocalCache() }
    }

    /** Rebuilds [TodayTrainingState] from Room workout logs (no network). */
    private suspend fun refreshTodayTrainingFromLocalCache() {
        val current = _uiState.value as? HomeUiState.Success ?: return
        val today = LocalDate.now().toString()
        val todayLogs = getCachedWorkoutLogsUseCase(from = today, to = today)
        val planDetail = cachedActivePlanDetail
            ?: cachedActivePlanId?.let { getTrainingPlanDetailUseCase.fromCache(it) }
        val updatedTodayTraining = deriveTodayTraining(
            planDetail,
            current.weeklySummary,
            todayLogs,
        ) ?: return
        if (updatedTodayTraining.isCompleted) {
            markLocalWorkoutCompletion()
        }
        if (current.todayTraining == updatedTodayTraining) return
        updateSuccessIfSuccess("workout_cache_refresh") { state ->
            state.copy(todayTraining = updatedTodayTraining)
        }
    }

    /**
     * Instant Home update when the user taps activate in Training (before API/Room confirm).
     */
    private suspend fun applyOptimisticActiveTrainingPlan(planId: String, planName: String?) {
        val current = _uiState.value as? HomeUiState.Success ?: return
        markLocalTrainingPlanChange(planId)

        val cachedDetail = getTrainingPlanDetailUseCase.fromCache(planId)
        val summary = ActivePlanSummary(
            id = planId,
            name = planName?.takeIf { it.isNotBlank() } ?: cachedDetail?.name ?: "",
        )
        cachedActivePlanId = planId
        cachedActivePlanSummary = summary
        cachedActivePlanDetail = cachedDetail?.takeIf { it.days.isNotEmpty() }

        val detailForTraining = cachedDetail?.takeIf { it.days.isNotEmpty() }
        val todayWorkoutLogs = loadTodayWorkoutHistoryCacheFirst()
        val todayTraining = deriveTodayTraining(
            detailForTraining,
            current.weeklySummary,
            todayWorkoutLogs,
        )

        updateSuccessIfSuccess("training_optimistic") { state ->
            state.copy(
                activePlan = summary,
                todayTraining = todayTraining,
                isTrainingHydrating = detailForTraining == null,
            )
        }

        viewModelScope.launch {
            prefetchTrainingPlanDetailsUseCase(listOf(planId))
            applyCachedTrainingDetailIfStillActive(planId)
        }
    }

    /** Applies warmed detail cache without re-reading Room active status (avoids reverting optimistic UI). */
    private suspend fun applyCachedTrainingDetailIfStillActive(planId: String) {
        val current = _uiState.value as? HomeUiState.Success ?: return
        if (current.activePlan?.id != planId) return
        val cachedDetail = getTrainingPlanDetailUseCase.fromCache(planId) ?: return
        if (cachedDetail.days.isEmpty()) return

        cachedActivePlanDetail = cachedDetail
        val todayWorkoutLogs = loadTodayWorkoutHistoryCacheFirst()
        val todayTraining = deriveTodayTraining(
            cachedDetail,
            current.weeklySummary,
            todayWorkoutLogs,
        )
        updateSuccessIfSuccess("training_detail_prefetch") { state ->
            state.copy(
                todayTraining = todayTraining,
                isTrainingHydrating = false,
            )
        }
    }

    /**
     * Updates training card fields from Room after active-plan changes (no bootstrap wait).
     */
    private suspend fun refreshTrainingFromLocalCache() {
        val current = _uiState.value as? HomeUiState.Success ?: return
        val trainingPlans = getCachedTrainingPlansUseCase()
        val roomActive = trainingPlans.find { it.status == PlanStatus.ACTIVE }
        val optimisticId = lastLocalTrainingPlanId
        val inOptimisticWindow = optimisticId != null &&
            localTrainingPlanRevisionMs > 0L &&
            System.currentTimeMillis() - localTrainingPlanRevisionMs < LOCAL_TRAINING_GUARD_MS

        val activePlanId = when {
            inOptimisticWindow && roomActive?.id != optimisticId -> optimisticId
            roomActive != null -> roomActive.id
            inOptimisticWindow -> optimisticId
            else -> null
        }

        if (activePlanId == null) {
            if (cachedActivePlanId == null) return
            cachedActivePlanId = null
            cachedActivePlanDetail = null
            cachedActivePlanSummary = null
            lastLocalTrainingPlanId = null
            localTrainingPlanRevisionMs = 0L
            updateSuccessIfSuccess("training_cache_clear") { state ->
                state.copy(
                    activePlan = null,
                    todayTraining = null,
                    isTrainingHydrating = false,
                )
            }
            return
        }

        val planId = activePlanId
        if (planId == cachedActivePlanId && !cachedActivePlanDetail?.days.isNullOrEmpty()) {
            return
        }

        markLocalTrainingPlanChange(planId)

        val roomSummary = roomActive?.takeIf { it.id == planId }
        val planDetail = resolveActivePlanDetail(planId = planId, awaitNetwork = false) ?: roomSummary
        if (planDetail == null) {
            val summary = current.activePlan?.takeIf { it.id == planId } ?: return
            markLocalTrainingPlanChange(planId)
            cachedActivePlanId = planId
            cachedActivePlanSummary = summary
            updateSuccessIfSuccess("training_cache_summary_only") { state ->
                state.copy(activePlan = summary, isTrainingHydrating = true)
            }
            prefetchPlanDetail(planId)
            return
        }

        cachedActivePlanId = planId
        cachedActivePlanDetail = planDetail.takeIf { it.days.isNotEmpty() }
        cachedActivePlanSummary = ActivePlanSummary(planDetail.id, planDetail.name)

        val detailForTraining = planDetail.takeIf { it.days.isNotEmpty() }
        val todayWorkoutLogs = loadTodayWorkoutHistoryCacheFirst()
        val todayTraining = deriveTodayTraining(
            detailForTraining,
            current.weeklySummary,
            todayWorkoutLogs,
        )
        val isTrainingHydrating = detailForTraining == null

        updateSuccessIfSuccess("training_cache_refresh") { state ->
            state.copy(
                activePlan = cachedActivePlanSummary,
                todayTraining = todayTraining,
                isTrainingHydrating = isTrainingHydrating,
            )
        }

        if (detailForTraining == null) {
            prefetchPlanDetail(planId)
        }
    }

    /** Updates next-meal card from Room after active diet plan changes. */
    private suspend fun refreshNutritionFromLocalCache() {
        if (_uiState.value !is HomeUiState.Success) return
        val dietPlans = getCachedDietPlansUseCase()
        val activeDiet = dietPlans.find { it.status == PlanStatus.ACTIVE }

        if (activeDiet == null) {
            if (cachedActiveDietId == null) return
            cachedActiveDietId = null
            cachedActiveDietDetail = null
            updateSuccessIfSuccess("diet_cache_clear") { state ->
                state.copy(nextMeal = NextMealState.NoPlan)
            }
            return
        }

        val planId = activeDiet.id
        if (planId == cachedActiveDietId && !cachedActiveDietDetail?.days.isNullOrEmpty()) {
            return
        }

        val planDetail = getDietPlanDetailUseCase.fromCache(planId)
            ?: activeDiet.takeIf { it.days.isNotEmpty() }

        cachedActiveDietId = planId
        cachedActiveDietDetail = planDetail?.takeIf { it.days.isNotEmpty() }

        val detailForMeal = planDetail?.takeIf { it.days.isNotEmpty() }
        val nextMeal = when {
            detailForMeal != null -> deriveNextMeal(detailForMeal)
            else -> NextMealState.NoPlan
        }

        updateSuccessIfSuccess("diet_cache_refresh") { state ->
            state.copy(nextMeal = nextMeal)
        }

        if (detailForMeal == null) {
            viewModelScope.launch {
                when (val result = getDietPlanDetailUseCase(planId)) {
                    is Result.Success -> {
                        cachedActiveDietDetail = result.data
                        updateSuccessIfSuccess("diet_detail_prefetch") { state ->
                            state.copy(nextMeal = deriveNextMeal(result.data))
                        }
                    }
                    else -> Unit
                }
            }
        }
    }

    private fun mark(label: String) {
        perfMarks.add(label to System.currentTimeMillis())
    }

    private fun dumpPerf() {
        val base = perfMarks.firstOrNull()?.second ?: return
        perfMarks.forEach { (label, t) ->
            Log.d(TAG_PERF, "+${t - base}ms — $label")
        }
        perfMarks.clear()
    }

    private fun onFirstSuccessEmitted() {
        mark("first_success_emitted")
        if (!perfDumpedForSession) {
            dumpPerf()
            perfDumpedForSession = true
        }
    }

    private suspend fun loadBootstrapCacheSnapshot(): HomeUiState.Success? {
        val bootstrap = getCachedHomeBootstrapUseCase() ?: return null
        mark("bootstrap_cache_hit")
        return buildSnapshotFromBootstrap(bootstrap)
    }

    /** Refreshes dashboard data from the network without blocking the initial cache paint. */
    private fun refreshBootstrapInBackground() {
        bootstrapJob?.cancel()
        bootstrapJob = viewModelScope.launch {
            val networkSnapshot = refreshFromBootstrapInternal()
            if (networkSnapshot == null) {
                if (_uiState.value is HomeUiState.Success) {
                    scheduleBootstrapRefresh()
                }
                return@launch
            }
            val current = _uiState.value as? HomeUiState.Success
            val merged = current?.let { mergeBootstrapWithCurrent(it, networkSnapshot) } ?: networkSnapshot
            if (current == null || hasHomeStateChange(current, merged)) {
                applySuccessState(merged, "loadAll_bootstrap")
            }
        }
    }

    private suspend fun buildSnapshotFromBootstrap(bootstrap: HomeBootstrap): HomeUiState.Success {
        val trainingPlans = bootstrap.activeTrainingPlan?.let { listOf(it) } ?: emptyList()
        val dietPlans = bootstrap.activeDietPlan?.let { listOf(it) } ?: emptyList()
        val todayWorkoutLogs = mergeTodayWorkoutLogsWithLocalCache(bootstrap.todayWorkouts)
        return buildHomeSnapshot(
            profile = bootstrap.profile,
            nutritionPair = bootstrap.nutritionLog to bootstrap.nutritionTarget,
            weightEntries = weightEntriesFromWeeklySummary(bootstrap.weeklySummary),
            trainingPlans = trainingPlans,
            dietPlans = dietPlans,
            todayWorkoutLogs = todayWorkoutLogs,
            weeklySummary = bootstrap.weeklySummary,
            streaks = bootstrap.streaks,
            userAchievements = bootstrap.achievements,
            allDefinitions = bootstrap.achievementDefinitions,
            planDetail = bootstrap.activeTrainingPlan,
            dietDetail = bootstrap.activeDietPlan,
        )
    }

    private fun scheduleBootstrapRefresh() {
        if (!hasCompletedInitialLoad) return
        bootstrapJob?.cancel()
        bootstrapJob = viewModelScope.launch {
            delay(BOOTSTRAP_DEBOUNCE_MS)
            if (_uiState.value !is HomeUiState.Success) return@launch
            val updated = refreshFromBootstrapInternal() ?: return@launch
            val current = _uiState.value as? HomeUiState.Success ?: return@launch
            val merged = mergeBootstrapWithCurrent(current, updated)
            if (!hasHomeStateChange(current, merged)) return@launch
            applySuccessState(merged, "bootstrap_refresh")
        }
    }

    private fun mergeBootstrapWithCurrent(
        current: HomeUiState.Success,
        bootstrap: HomeUiState.Success,
    ): HomeUiState.Success {
        val withAvatar = bootstrap.copy(
            avatarUrl = pickBestProfilePictureUrl(bootstrap.avatarUrl, current.avatarUrl),
        )
        val preservedTraining = if (shouldPreserveLocalTraining(withAvatar)) {
            withAvatar.copy(
                activePlan = current.activePlan,
                todayTraining = current.todayTraining,
                isTrainingHydrating = current.isTrainingHydrating,
            )
        } else {
            withAvatar
        }
        return if (shouldPreserveLocalWorkoutCompletion(preservedTraining)) {
            preservedTraining.copy(todayTraining = current.todayTraining)
        } else {
            preservedTraining
        }
    }

    /** Prefer locked logs from Room when bootstrap/history responses are still stale. */
    private suspend fun mergeTodayWorkoutLogsWithLocalCache(
        remoteLogs: List<WorkoutLog>,
    ): List<WorkoutLog> {
        val today = LocalDate.now().toString()
        val cached = getCachedWorkoutLogsUseCase(from = today, to = today)
        if (cached.isEmpty()) return remoteLogs
        val byId = remoteLogs.associateBy { it.id }.toMutableMap()
        for (log in cached) {
            if (log.isLocked) {
                byId[log.id] = log
            }
        }
        return byId.values.toList()
    }

    private fun applySuccessState(state: HomeUiState.Success, @Suppress("UNUSED_PARAMETER") source: String) {
        val current = _uiState.value as? HomeUiState.Success
        if (state.todayTraining?.isCompleted == true && current?.todayTraining?.isCompleted != true) {
            markLocalWorkoutCompletion()
        }
        var resolved = when {
            current != null && shouldPreserveLocalTraining(state) -> state.copy(
                activePlan = current.activePlan,
                todayTraining = current.todayTraining,
                isTrainingHydrating = current.isTrainingHydrating,
                avatarUrl = pickBestProfilePictureUrl(state.avatarUrl, current.avatarUrl),
            )
            else -> state
        }
        if (current != null && shouldPreserveLocalWorkoutCompletion(resolved)) {
            resolved = resolved.copy(todayTraining = current.todayTraining)
        }
        if (resolved.activePlan?.id == lastLocalTrainingPlanId &&
            source in setOf("bootstrap_refresh", "loadAll_bootstrap", "training_cache_refresh")
        ) {
            localTrainingPlanRevisionMs = 0L
        }
        if (resolved.todayTraining?.isCompleted == true &&
            source in setOf("bootstrap_refresh", "loadAll_bootstrap", "workout_cache_refresh", "workout_finalized")
        ) {
            localWorkoutCompletionRevisionMs = 0L
        }
        _uiState.value = resolved
    }

    private fun updateSuccessIfSuccess(
        source: String,
        transform: (HomeUiState.Success) -> HomeUiState.Success,
    ) {
        val current = _uiState.value as? HomeUiState.Success ?: return
        val updated = transform(current)
        if (updated == current) return
        applySuccessState(updated, source)
    }

    private fun hasHomeStateChange(
        current: HomeUiState.Success,
        updated: HomeUiState.Success,
    ): Boolean = current != updated

    /**
     * Re-reads name and avatar from the profile repository (Room cache + API).
     * Called when returning to Home so a photo uploaded on [UserProfileScreen] is reflected
     * in the greeting header without restarting the app.
     */
    private suspend fun refreshProfileHeader() {
        if (_uiState.value !is HomeUiState.Success) return
        var bestAvatar: String? = (_uiState.value as HomeUiState.Success).avatarUrl
        getUserProfileUseCase().collect { result ->
            if (result is Result.Success) {
                val profile = result.data
                bestAvatar = pickBestProfilePictureUrl(profile.profilePictureUrl, bestAvatar)
                updateSuccessIfSuccess("profile_header_refresh") { current ->
                    current.copy(
                        userName = profile.name,
                        avatarUrl = bestAvatar,
                    )
                }
            }
        }
    }

    /**
     * Resolves training plan detail from Room JSON cache, optionally waiting for network.
     *
     * @param awaitNetwork When false (initial [loadAll]), returns cache immediately so Success
     * is not blocked; network reconciliation runs via [refreshFromBootstrapInternal] / [prefetchPlanDetail].
     */
    private suspend fun resolveActivePlanDetail(
        planId: String,
        awaitNetwork: Boolean = true,
        networkTimeoutMs: Long = PLAN_DETAIL_NETWORK_TIMEOUT_MS,
    ): TrainingPlan? {
        val cached = getTrainingPlanDetailUseCase.fromCache(planId)
        if (cached != null && cached.days.isNotEmpty()) {
            mark("plan_detail_cache_hit")
            return cached
        }
        mark("plan_detail_cache_miss")
        if (!awaitNetwork) {
            prefetchPlanDetail(planId)
            return cached
        }
        val fromNetwork = withTimeoutOrNull(networkTimeoutMs) {
            loadPlanDetail(planId)
        }
        mark("plan_detail_network_done")
        return when {
            fromNetwork != null && fromNetwork.days.isNotEmpty() -> fromNetwork
            cached != null -> cached
            else -> fromNetwork
        }
    }

    /** Warms the plan detail JSON cache without blocking the home snapshot. */
    private fun prefetchPlanDetail(planId: String) {
        viewModelScope.launch {
            getTrainingPlanDetailUseCase(planId)
        }
    }

    private suspend fun buildHomeSnapshot(
        profile: UserProfile,
        nutritionPair: Pair<NutritionLog?, NutritionTarget?>,
        weightEntries: List<BodyWeightLog>,
        trainingPlans: List<TrainingPlan>,
        dietPlans: List<DietPlan>,
        todayWorkoutLogs: List<WorkoutLog>,
        weeklySummary: WeeklyProgressSummary?,
        streaks: List<Streak>,
        userAchievements: List<UserAchievement>,
        allDefinitions: List<AchievementDefinition>,
        planDetail: TrainingPlan?,
        dietDetail: DietPlan?,
    ): HomeUiState.Success {
        val initialActivePlan = trainingPlans.find { it.status == PlanStatus.ACTIVE }
        val initialActiveDiet = dietPlans.find { it.status == PlanStatus.ACTIVE }

        cachedActivePlanId = initialActivePlan?.id
        cachedActivePlanDetail = planDetail
        cachedActivePlanSummary = when {
            planDetail != null -> ActivePlanSummary(planDetail.id, planDetail.name)
            initialActivePlan != null -> ActivePlanSummary(initialActivePlan.id, initialActivePlan.name)
            else -> null
        }

        cachedActiveDietId = initialActiveDiet?.id
        cachedActiveDietDetail = dietDetail

        cachedWeeklySummary = weeklySummary

        val motivation = deriveMotivation(userAchievements, allDefinitions, streaks)
        val todayTraining = deriveTodayTraining(planDetail, weeklySummary, todayWorkoutLogs)
        val isTrainingHydrating = initialActivePlan != null &&
            (planDetail == null || planDetail.days.isEmpty())
        val nextMeal = when {
            initialActiveDiet == null -> NextMealState.NoPlan
            else -> deriveNextMeal(dietDetail)
        }

        return HomeUiState.Success(
            userName = profile.name,
            avatarUrl = profile.profilePictureUrl,
            activePlan = cachedActivePlanSummary,
            todayTraining = todayTraining,
            todayNutrition = deriveNutrition(nutritionPair.first, nutritionPair.second),
            nextMeal = nextMeal,
            streaks = streaks,
            weeklySummary = weeklySummary,
            weightEntries = weightEntries,
            lastAchievement = motivation.lastAchievement,
            nextAchievement = motivation.nextAchievement,
            trainingStreakDays = motivation.trainingStreakDays,
            isTrainingHydrating = isTrainingHydrating,
        )
    }

    /** Builds [HomeUiState.Success] from Room only (no network); returns null if profile cache is missing.*/
    private suspend fun loadCacheFirstSnapshot(): HomeUiState.Success? {
        val profile = firstSuccessProfile()
        if (profile != null) {
            mark("profile_cache_hit")
        } else {
            mark("profile_cache_miss")
            return null
        }
        return coroutineScope {
            val nutritionDeferred = async { loadNutritionCacheFirst() }
            val trainingPlansDeferred = async { firstSuccessTrainingPlans() }
            val workoutDeferred = async { loadTodayWorkoutHistoryCacheFirst() }
            val trainingPlans = trainingPlansDeferred.await()
            val activePlan = trainingPlans.find { it.status == PlanStatus.ACTIVE }
            if (activePlan != null) {
                mark("active_plan_found")
            } else {
                mark("no_active_plan")
            }
            val planDetail = activePlan?.let {
                resolveActivePlanDetail(planId = it.id, awaitNetwork = false)
            }
            buildHomeSnapshot(
                profile = profile,
                nutritionPair = nutritionDeferred.await(),
                weightEntries = emptyList(),
                trainingPlans = trainingPlans,
                dietPlans = emptyList(),
                todayWorkoutLogs = workoutDeferred.await(),
                weeklySummary = null,
                streaks = emptyList(),
                userAchievements = emptyList(),
                allDefinitions = emptyList(),
                planDetail = planDetail,
                dietDetail = cachedActiveDietDetail,
            )
        }
    }

    /**
     * Single network round-trip for home data ([GET /home/bootstrap]).
     * Does not call legacy list endpoints (diet-plans, training-plans, achievements/all, body-weight).
     */
    private suspend fun refreshFromBootstrapInternal(): HomeUiState.Success? {
        val networkStart = System.currentTimeMillis()
        val bootstrap = when (val result = getHomeBootstrapUseCase()) {
            is Result.Success -> result.data
            else -> return refreshFromLegacyInternal()
        }
        mark("profile_network_done")
        mark("workout_history_network_done")
        if (bootstrap.activeTrainingPlan != null) {
            mark("plans_network_emit")
            mark("active_plan_found")
        } else {
            mark("no_active_plan")
        }
        val snapshot = buildSnapshotFromBootstrap(bootstrap)
        perfLog("bootstrap_network", networkStart)
        return snapshot
    }

    /**
     * Legacy fallback path used when bootstrap fails:
     * builds a full snapshot from independent use-cases using last-success semantics.
     */
    private suspend fun refreshFromLegacyInternal(): HomeUiState.Success? = coroutineScope {
        val profile = loadProfileLastSuccess() ?: return@coroutineScope null
        val nutritionDeferred = async { loadNutritionLastSuccess() }
        val trainingPlansDeferred = async { awaitFreshPlans() }
        val dietPlansDeferred = async { awaitFreshDietPlans() }
        val weeklyDeferred = async { loadWeeklySummary() ?: cachedWeeklySummary }
        val streaksDeferred = async { loadStreaks() }
        val achievementsDeferred = async { loadUserAchievements() }
        val definitionsDeferred = async { loadAchievementDefinitions() }
        val weightEntriesDeferred = async { firstSuccessWeightHistory() }
        val workoutsDeferred = async { loadTodayWorkoutHistoryFresh() }

        val trainingPlans = trainingPlansDeferred.await()
        val activePlan = trainingPlans.find { it.status == PlanStatus.ACTIVE }
        val planDetail = when (activePlan) {
            null -> null
            else -> resolveActivePlanDetail(planId = activePlan.id, awaitNetwork = true) ?: activePlan
        }

        val dietPlans = dietPlansDeferred.await()
        val activeDiet = dietPlans.find { it.status == PlanStatus.ACTIVE }
        val dietDetail = when (activeDiet) {
            null -> null
            else -> loadDietPlanDetail(activeDiet.id) ?: activeDiet
        }

        buildHomeSnapshot(
            profile = profile,
            nutritionPair = nutritionDeferred.await(),
            weightEntries = weightEntriesDeferred.await(),
            trainingPlans = trainingPlans,
            dietPlans = dietPlans,
            todayWorkoutLogs = workoutsDeferred.await(),
            weeklySummary = weeklyDeferred.await(),
            streaks = streaksDeferred.await(),
            userAchievements = achievementsDeferred.await(),
            allDefinitions = definitionsDeferred.await(),
            planDetail = planDetail,
            dietDetail = dietDetail,
        )
    }

    /** Latest weight from bootstrap weekly summary (full history stays on body-weight screen). */
    private fun weightEntriesFromWeeklySummary(
        weekly: WeeklyProgressSummary?,
    ): List<BodyWeightLog> {
        val weight = weekly?.bodyWeight ?: return emptyList()
        val today = LocalDate.now()
        return listOf(
            BodyWeightLog(
                id = "bootstrap-latest",
                weight = weight,
                date = today,
                notes = null,
                createdAt = today,
            ),
        )
    }

    private suspend fun loadProfileLastSuccess(): UserProfile? {
        var profile: UserProfile? = null
        getUserProfileUseCase().collect { result ->
            if (result is Result.Success) profile = result.data
        }
        return profile
    }

    private suspend fun firstSuccessProfile(): UserProfile? {
        var profile: UserProfile? = null
        getUserProfileUseCase().collect { result ->
            if (result is Result.Success) {
                profile = result.data
                return@collect
            }
        }
        return profile
    }

    private suspend fun loadPlanDetail(planId: String): TrainingPlan? =
        when (val r = getTrainingPlanDetailUseCase(planId)) {
            is Result.Success -> r.data
            else -> null
        }

    /** First [Result.Success] from the plans flow (Room cache when available); does not wait for network. */
    private suspend fun firstSuccessTrainingPlans(): List<TrainingPlan> {
        var latest = emptyList<TrainingPlan>()
        getTrainingPlansUseCase().collect { result ->
            if (result is Result.Success) {
                latest = result.data
                mark("plans_cache_emit")
                return@collect
            }
        }
        return latest
    }

    /** First [Result.Success] from the diet plans flow (Room cache when available); does not wait for network. */
    private suspend fun firstSuccessDietPlans(): List<DietPlan> {
        var latest = emptyList<DietPlan>()
        getDietPlansUseCase().collect { result ->
            if (result is Result.Success) {
                latest = result.data
                return@collect
            }
        }
        return latest
    }

    /**
     * Collects the full getTrainingPlansUseCase() flow (cache emission + network emission)
     * and returns the last [Result.Success] data, or an empty list if every emission failed.
     */
    private suspend fun awaitFreshPlans(): List<TrainingPlan> {
        var latest = emptyList<TrainingPlan>()
        getTrainingPlansUseCase().collect { result ->
            if (result is Result.Success) latest = result.data
        }
        return latest
    }

    /**
     * Same as [awaitFreshPlans] but for diet plans.
     */
    private suspend fun awaitFreshDietPlans(): List<DietPlan> {
        var latest = emptyList<DietPlan>()
        getDietPlansUseCase().collect { result ->
            if (result is Result.Success) latest = result.data
        }
        return latest
    }

    private suspend fun loadDietPlanDetail(planId: String): DietPlan? =
        when (val r = getDietPlanDetailUseCase(planId)) {
            is Result.Success -> r.data
            else -> null
        }

    /** Cache-first: first non-[Result.Loading] emission per flow (does not wait for flow completion). */
    private suspend fun loadNutritionCacheFirst(): Pair<NutritionLog?, NutritionTarget?> = coroutineScope {
        val totalStart = System.currentTimeMillis()
        val today = LocalDate.now()
        val logDeferred = async {
            val t0 = System.currentTimeMillis()
            val data = when (val r = getNutritionLogUseCase(today).first { it !is Result.Loading }) {
                is Result.Success -> r.data
                else -> null
            }
            perfLog("nutrition_log_cache", t0)
            data
        }
        val targetDeferred = async {
            val t0 = System.currentTimeMillis()
            val data = when (val r = getCurrentNutritionTargetUseCase().first { it !is Result.Loading }) {
                is Result.Success -> r.data
                else -> null
            }
            data
        }
        val pair = logDeferred.await() to targetDeferred.await()
        mark("nutrition_target_done")
        perfLog("nutrition_parallel_total", totalStart)
        pair
    }

    /** Last-success semantics for refreshes (collects cache + network emissions). */
    private suspend fun loadNutritionLastSuccess(): Pair<NutritionLog?, NutritionTarget?> = coroutineScope {
        val today = LocalDate.now()
        val logDeferred = async {
            var log: NutritionLog? = null
            getNutritionLogUseCase(today).collect { result ->
                if (result is Result.Success) log = result.data
            }
            log
        }
        val targetDeferred = async {
            var target: NutritionTarget? = null
            getCurrentNutritionTargetUseCase().collect { result ->
                if (result is Result.Success) target = result.data
            }
            target
        }
        logDeferred.await() to targetDeferred.await()
    }

    private suspend fun loadWeeklySummary(): WeeklyProgressSummary? =
        when (val r = getWeeklyProgressSummaryUseCase()) {
            is Result.Success -> r.data
            else -> null
        }

    private suspend fun loadStreaks(): List<Streak> =
        when (val r = getUserStreaksUseCase()) {
            is Result.Success -> r.data
            else -> emptyList()
        }

    private suspend fun firstSuccessWeightHistory(): List<BodyWeightLog> {
        val today = LocalDate.now()
        return when (
            val r = getBodyWeightHistoryUseCase(
                today.minusDays(30).toString(),
                today.toString(),
            ).first { it !is Result.Loading }
        ) {
            is Result.Success -> r.data.takeLast(7)
            else -> emptyList()
        }
    }

    /** Reads today's workout logs from Room only (no network). */
    private suspend fun loadTodayWorkoutHistoryCacheFirst(): List<WorkoutLog> {
        val today = LocalDate.now().toString()
        val result = getCachedWorkoutLogsUseCase(from = today, to = today)
        mark("workout_history_cache_emit")
        return result
    }

    /** Waits for cache + network so completion / isLocked reflect the server. */
    private suspend fun loadTodayWorkoutHistoryFresh(): List<WorkoutLog> {
        val today = LocalDate.now().toString()
        val totalStart = System.currentTimeMillis()
        var result = emptyList<WorkoutLog>()
        var loggedCache = false
        getWorkoutHistoryUseCase(from = today, to = today)
            .collect { r ->
                if (r is Result.Success) {
                    if (!loggedCache) {
                        perfLog("workout_history_cache", totalStart)
                        loggedCache = true
                    }
                    result = r.data
                }
            }
        perfLog("workout_history_network", totalStart)
        return result
    }

    private fun deriveTodayTraining(
        activePlan: TrainingPlan?,
        weeklySummary: WeeklyProgressSummary?,
        todayWorkoutLogs: List<WorkoutLog>,
    ): TodayTrainingState? {
        if (activePlan == null) return null
        if (activePlan.days.isEmpty()) return null

        val today = java.time.DayOfWeek.from(LocalDate.now())

        // Step 1: try to match by dayOfWeek (populated for plans generated after the
        // day_of_week column was added to the database).
        var todayTrainingDay = activePlan.days.find { it.dayOfWeek == today }

        // Step 2: fallback for legacy plans where every dayOfWeek is null — distribute
        // training days across the week using the ISO day-of-week index (1=Mon…7=Sun).
        if (todayTrainingDay == null) {
            val nonRestDays = activePlan.days.filter {
                it.dayType != TrainingDayType.REST && it.exercises.isNotEmpty()
            }
            if (nonRestDays.isEmpty()) return null
            todayTrainingDay = nonRestDays[(today.value - 1) % nonRestDays.size]
        }

        if (todayTrainingDay.dayType == TrainingDayType.REST) return null

        val isCompleted = todayWorkoutLogs.any {
            it.trainingPlanId == activePlan.id &&
                it.trainingDayId == todayTrainingDay.id &&
                it.isLocked
        }
        val adherence = if (weeklySummary != null && weeklySummary.workoutsTarget > 0) {
            (weeklySummary.workoutsThisWeek.toFloat() / weeklySummary.workoutsTarget * 100f)
                .coerceIn(0f, 100f)
        } else 0f

        return TodayTrainingState(
            planId = activePlan.id,
            dayId = todayTrainingDay.id,
            planName = activePlan.name,
            dayName = todayTrainingDay.name,
            exerciseCount = todayTrainingDay.exercises.size,
            exerciseNames = todayTrainingDay.exercises.map { it.name }.take(4),
            adherencePercentage = adherence,
            isCompleted = isCompleted,
        )
    }

    private fun deriveNextMeal(
        activeDietPlan: DietPlan?,
    ): NextMealState {
        if (activeDietPlan == null || activeDietPlan.days.isEmpty()) return NextMealState.NoPlan

        val dayOfWeek = LocalDate.now().dayOfWeek.value // 1=Monday … 7=Sunday
        val todayDietDay = activeDietPlan.days.getOrNull(
            (dayOfWeek - 1) % activeDietPlan.days.size,
        ) ?: return NextMealState.NoPlan

        val now = LocalTime.now()

        val sortedMeals = todayDietDay.meals
            .sortedBy { parseMealTime(it.time, it.mealType) }

        val nextMeal = sortedMeals
            .firstOrNull { parseMealTime(it.time, it.mealType).isAfter(now) }
            ?: return NextMealState.AllDone

        return NextMealState.Upcoming(
            mealName = nextMeal.name,
            estimatedTime = nextMeal.time.ifBlank { estimatedTimeForMealType(nextMeal.mealType) },
            calories = nextMeal.calories,
            proteinG = nextMeal.proteinGrams.toDouble(),
            carbsG = nextMeal.carbsGrams.toDouble(),
            fatG = nextMeal.fatGrams.toDouble(),
        )
    }

    /**
     * Parses a meal's time string (e.g. "13:00", "8:00") into a [LocalTime].
     * Falls back to the estimated time for the meal type if parsing fails.
     */
    private fun parseMealTime(time: String, mealType: MealType): LocalTime {
        return try {
            LocalTime.parse(
                time.trim().let { if (it.length == 4 && it[1] == ':') "0$it" else it },
            )
        } catch (_: Exception) {
            LocalTime.parse(
                estimatedTimeForMealType(mealType).let { if (it.length == 4 && it[1] == ':') "0$it" else it },
            )
        }
    }

    private fun deriveNutrition(
        log: NutritionLog?,
        target: NutritionTarget?,
    ): TodayNutritionState? {
        if (log == null && target == null) return null
        return TodayNutritionState(
            caloriesConsumed = log?.totalCalories ?: 0,
            calorieTarget = target?.calorieTarget ?: 0,
            proteinConsumed = log?.totalProteinGrams ?: 0.0,
            proteinTarget = target?.proteinTarget ?: 0.0,
            carbsConsumed = log?.totalCarbsGrams ?: 0.0,
            carbsTarget = target?.carbsTarget ?: 0.0,
            fatConsumed = log?.totalFatGrams ?: 0.0,
            fatTarget = target?.fatTarget ?: 0.0,
        )
    }

    private suspend fun resolveTodayPlanMeals(): List<Meal> {
        val activeId = cachedActiveDietId
            ?: getCachedDietPlansUseCase().find { it.status == PlanStatus.ACTIVE }?.id
        val plan = activeId?.let { getDietPlanDetailUseCase.fromCache(it) }
            ?: cachedActiveDietDetail
            ?: activeId?.let { planId ->
                loadDietPlanDetail(planId)?.also { cachedActiveDietDetail = it }
            }
        return plan?.let { mealsForToday(it) } ?: emptyList()
    }

    private fun emitEvent(event: HomeUiEvent) {
        viewModelScope.launch { _events.send(event) }
    }

    // ── Gamification helpers (BUG-026) ───────────────────────────────────────

    private suspend fun loadUserAchievements(): List<UserAchievement> =
        when (val r = getUserAchievementsUseCase()) {
            is Result.Success -> r.data
            else -> emptyList()
        }

    private suspend fun loadAchievementDefinitions(): List<AchievementDefinition> =
        when (val r = getAllDefinitionsUseCase()) {
            is Result.Success -> r.data
            else -> emptyList()
        }

    private data class MotivationData(
        val lastAchievement: UserAchievement?,
        val nextAchievement: AchievementDefinition?,
        val trainingStreakDays: Int,
    )

    private fun deriveMotivation(
        achievements: List<UserAchievement>,
        definitions: List<AchievementDefinition>,
        streaks: List<Streak>,
    ): MotivationData {
        // Last achievement (within last 7 days)
        val recentCutoff = LocalDate.now().minusDays(7).toString()
        val lastAchievement = achievements
            .sortedByDescending { it.unlockedAt }
            .firstOrNull { it.unlockedAt >= recentCutoff }

        // Next achievement to unlock
        val unlockedIds = achievements.map { it.achievement.id }.toSet()
        val nextAchievement = definitions.firstOrNull { it.id !in unlockedIds }

        // Training streak
        val trainingStreak = streaks
            .filter { it.type == StreakType.TRAINING }
            .maxByOrNull { it.currentCount }
        val trainingStreakDays = trainingStreak?.currentCount ?: 0

        return MotivationData(
            lastAchievement = lastAchievement,
            nextAchievement = nextAchievement,
            trainingStreakDays = trainingStreakDays,
        )
    }

    private fun mealsForToday(plan: DietPlan): List<Meal> {
        if (plan.days.isEmpty()) return emptyList()
        val dayOfWeek = LocalDate.now().dayOfWeek.value
        val todayDietDay = plan.days.getOrNull((dayOfWeek - 1) % plan.days.size) ?: return emptyList()
        return todayDietDay.meals
    }

    private fun mealToTrackMealRequestDto(meal: Meal, date: LocalDate = LocalDate.now()): TrackMealRequestDto {
        val resolvedTime = meal.time.trim().let { raw ->
            if (raw.isNotBlank()) raw else estimatedTimeForMealType(meal.mealType)
        }
        val items = if (meal.items.isNotEmpty()) {
            meal.items.map { item ->
                TrackFoodItemRequestDto(
                    name = item.name,
                    quantity = item.quantity.toDouble(),
                    unit = item.unit,
                    calories = item.calories,
                    proteinGrams = item.proteinGrams.toDouble(),
                    carbsGrams = item.carbsGrams.toDouble(),
                    fatGrams = item.fatGrams.toDouble(),
                    macrosPer100g = false,
                )
            }
        } else {
            listOf(
                TrackFoodItemRequestDto(
                    name = meal.name,
                    quantity = 1.0,
                    unit = "unit",
                    calories = meal.calories,
                    proteinGrams = meal.proteinGrams.toDouble(),
                    carbsGrams = meal.carbsGrams.toDouble(),
                    fatGrams = meal.fatGrams.toDouble(),
                    macrosPer100g = false,
                ),
            )
        }
        return TrackMealRequestDto(
            date = date.toString(),
            mealType = meal.mealType.name,
            name = meal.name,
            time = resolvedTime,
            items = items,
        )
    }

    private fun AppException.userMessage(): String = when (this) {
        is AppException.NetworkException -> "Sin conexión. Comprueba tu internet."
        is AppException.UnauthorizedException -> "Sesión expirada. Vuelve a iniciar sesión."
        is AppException.ForbiddenException -> "No tienes permisos para realizar esta acción."
        is AppException.NotFoundException -> "No se encontró $resource."
        is AppException.ValidationException -> errors.values.firstOrNull() ?: "Datos inválidos."
        is AppException.ConflictException -> "El recurso ya existe o hay un conflicto."
        is AppException.ServerException -> "Error del servidor. Inténtalo más tarde."
        is AppException.AiOverloadedException -> AppException.AI_OVERLOADED_MESSAGE
        is AppException.UnknownException -> message.ifBlank { "Error inesperado. Inténtalo de nuevo." }
        is AppException.InsufficientDataException -> "Necesitas más datos para realizar este análisis. Registra al menos 2 semanas de peso y entrenamientos."
    }

    companion object {

        private const val TAG_PERF = "HOME_PERF"
        private const val PLAN_DETAIL_NETWORK_TIMEOUT_MS = 10_000L
        private const val BOOTSTRAP_DEBOUNCE_MS = 800L
        private const val RESUME_DEBOUNCE_MS = 300L
        private const val LOCAL_TRAINING_GUARD_MS = 30_000L
        private const val LOCAL_WORKOUT_GUARD_MS = 30_000L

        private fun perfLog(label: String, startMs: Long) {
            Log.d(TAG_PERF, "$label: ${System.currentTimeMillis() - startMs}ms")
        }

        /**
         * Greeting based on the device's local time.
         *
         * @return `"Good morning"`, `"Good afternoon"` or `"Good evening"`.
         */
        fun greetingForTime(): String {
            val hour = LocalTime.now().hour
            return when {
                hour < 12 -> "Buenos días"
                hour < 20 -> "Buenas tardes"
                else -> "Buenas noches"
            }
        }

        private fun estimatedTimeForMealType(type: MealType): String = when (type) {
            MealType.BREAKFAST -> "8:00"
            MealType.MID_MORNING -> "10:30"
            MealType.LUNCH -> "13:00"
            MealType.AFTERNOON_SNACK -> "16:30"
            MealType.DINNER -> "20:00"
            MealType.PRE_WORKOUT -> "17:00"
            MealType.POST_WORKOUT -> "19:00"
            MealType.UNKNOWN -> "12:00"
        }
    }
}

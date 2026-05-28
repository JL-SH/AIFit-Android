package com.jlsh.aifit.feature.home.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jlsh.aifit.core.common.AppException
import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.feature.home.domain.model.HomeBootstrap
import com.jlsh.aifit.feature.home.domain.usecase.GetHomeBootstrapUseCase
import com.jlsh.aifit.feature.diet.domain.model.DietPlan
import com.jlsh.aifit.feature.diet.domain.model.Meal
import com.jlsh.aifit.feature.diet.domain.model.MealType
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
import com.jlsh.aifit.feature.training.domain.usecase.GetTrainingPlanDetailUseCase
import com.jlsh.aifit.feature.training.domain.usecase.GetTrainingPlansUseCase
import com.jlsh.aifit.feature.user.data.mapper.UserMapper.pickBestProfilePictureUrl
import com.jlsh.aifit.feature.user.domain.model.UserProfile
import com.jlsh.aifit.feature.user.domain.usecase.GetUserProfileUseCase
import com.jlsh.aifit.feature.workout.domain.model.WorkoutLog
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
) : ViewModel() {

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

            val cacheSnapshot = loadCacheFirstSnapshot()
            if (cacheSnapshot != null) {
                onFirstSuccessEmitted()
                applySuccessState(cacheSnapshot, "loadAll_cache")
                hasCompletedInitialLoad = true
                suppressResumeRefreshOnce = true
            }

            val networkSnapshot = refreshFromBootstrapInternal()
            when {
                networkSnapshot != null -> {
                    if (cacheSnapshot == null) {
                        onFirstSuccessEmitted()
                    }
                    applySuccessState(networkSnapshot, "loadAll_bootstrap")
                    if (!hasCompletedInitialLoad) {
                        hasCompletedInitialLoad = true
                        suppressResumeRefreshOnce = true
                    }
                }
                cacheSnapshot == null -> {
                    _uiState.value = HomeUiState.Error("No se pudo cargar el perfil")
                    return@launch
                }
                else -> {
                    // Cache snapshot shown; bootstrap failed (e.g. 500) — retry in background.
                    scheduleBootstrapRefresh()
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

    private fun scheduleBootstrapRefresh() {
        if (!hasCompletedInitialLoad) return
        bootstrapJob?.cancel()
        bootstrapJob = viewModelScope.launch {
            delay(BOOTSTRAP_DEBOUNCE_MS)
            if (_uiState.value !is HomeUiState.Success) return@launch
            val updated = refreshFromBootstrapInternal() ?: return@launch
            val current = _uiState.value as? HomeUiState.Success ?: return@launch
            val merged = updated.copy(
                avatarUrl = pickBestProfilePictureUrl(updated.avatarUrl, current.avatarUrl),
            )
            if (!hasHomeStateChange(current, merged)) return@launch
            applySuccessState(merged, "bootstrap_refresh")
        }
    }

    private fun applySuccessState(state: HomeUiState.Success, @Suppress("UNUSED_PARAMETER") source: String) {
        _uiState.value = state
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
        val trainingPlans = bootstrap.activeTrainingPlan?.let { listOf(it) } ?: emptyList()
        val dietPlans = bootstrap.activeDietPlan?.let { listOf(it) } ?: emptyList()
        val snapshot = buildHomeSnapshot(
            profile = bootstrap.profile,
            nutritionPair = bootstrap.nutritionLog to bootstrap.nutritionTarget,
            weightEntries = weightEntriesFromWeeklySummary(bootstrap.weeklySummary),
            trainingPlans = trainingPlans,
            dietPlans = dietPlans,
            todayWorkoutLogs = bootstrap.todayWorkouts,
            weeklySummary = bootstrap.weeklySummary,
            streaks = bootstrap.streaks,
            userAchievements = bootstrap.achievements,
            allDefinitions = bootstrap.achievementDefinitions,
            planDetail = bootstrap.activeTrainingPlan,
            dietDetail = bootstrap.activeDietPlan,
        )
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

    /** First non-[Result.Loading] emission (Room cache when available); does not wait for network. */
    private suspend fun loadTodayWorkoutHistoryCacheFirst(): List<WorkoutLog> {
        val today = LocalDate.now().toString()
        val result = when (
            val r = getWorkoutHistoryUseCase(from = today, to = today)
                .first { it !is Result.Loading }
        ) {
            is Result.Success -> r.data
            else -> emptyList()
        }
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
        val plan = cachedActiveDietDetail
            ?: cachedActiveDietId?.let { planId ->
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

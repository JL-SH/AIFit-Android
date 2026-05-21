package com.jlsh.aifit.feature.home.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.util.Log
import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.core.common.toMessage
import com.jlsh.aifit.feature.diet.domain.model.DietPlan
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
import com.jlsh.aifit.feature.user.domain.model.UserProfile
import com.jlsh.aifit.feature.user.domain.usecase.GetUserProfileUseCase
import com.jlsh.aifit.feature.workout.domain.model.WorkoutLog
import com.jlsh.aifit.feature.workout.domain.usecase.GetWorkoutHistoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject

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
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _events = Channel<HomeUiEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    private var loadJob: Job? = null
    private var cachedActivePlanDetail: TrainingPlan? = null
    private var cachedActivePlanId: String? = null       // Fix 2: survives a null cachedActivePlanDetail
    private var cachedActivePlanSummary: ActivePlanSummary? = null
    private var cachedWeeklySummary: WeeklyProgressSummary? = null
    private var cachedActiveDietDetail: DietPlan? = null
    private var cachedActiveDietId: String? = null

    init {
        loadAll()
    }

    fun loadAll() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _uiState.value = HomeUiState.Loading

            // ── Critical path: cache-first, parallel ──
            val profile: UserProfile?
            val nutritionPair: Pair<NutritionLog?, NutritionTarget?>
            val weightEntries: List<BodyWeightLog>
            val cachedTrainingPlans: List<TrainingPlan>
            val cachedDietPlans: List<DietPlan>

            coroutineScope {
                val profileDeferred = async { loadProfile() }
                val nutritionDeferred = async { loadNutrition() }
                val weightDeferred = async { loadWeightHistory() }
                val trainingPlansDeferred = async { firstSuccessTrainingPlans() }
                val dietPlansDeferred = async { firstSuccessDietPlans() }

                profile = profileDeferred.await()
                nutritionPair = nutritionDeferred.await()
                weightEntries = weightDeferred.await()
                cachedTrainingPlans = trainingPlansDeferred.await()
                cachedDietPlans = dietPlansDeferred.await()
            }

            if (profile == null) {
                _uiState.value = HomeUiState.Error("No se pudo cargar el perfil")
                return@launch
            }

            val todayNutrition = deriveNutrition(nutritionPair.first, nutritionPair.second)
            val initialActivePlan = cachedTrainingPlans.find { it.status == PlanStatus.ACTIVE }
            val initialActiveDiet = cachedDietPlans.find { it.status == PlanStatus.ACTIVE }

            cachedActivePlanId = initialActivePlan?.id
            cachedActivePlanSummary = initialActivePlan?.let {
                ActivePlanSummary(id = it.id, name = it.name)
            }
            cachedActiveDietId = initialActiveDiet?.id

            Log.d("AIFIT_DEBUG", "[VM][loadAll] cache-first Success — plans=${cachedTrainingPlans.size} activePlan=${initialActivePlan?.id}")

            // ── First paint: skeleton from Room / first flow emissions ──
            _uiState.value = HomeUiState.Success(
                userName = profile.name,
                avatarUrl = profile.profilePictureUrl,
                activePlan = cachedActivePlanSummary,
                todayTraining = null,
                todayNutrition = todayNutrition,
                nextMeal = NextMealState.NoPlan,
                streaks = emptyList(),
                weeklySummary = null,
                weightEntries = weightEntries,
                isRefreshingPlan = initialActivePlan != null,
                isRefreshingMeal = initialActiveDiet != null,
            )
            // ── Background: network reconciliation, then secondary cards (avoids state races) ──
            launch {
                enrichTrainingAndDietFromNetwork(
                    cachedTrainingPlans = cachedTrainingPlans,
                    cachedDietPlans = cachedDietPlans,
                )
                loadSecondaryHomeData()
            }
        }
    }

    fun onStartSession(planId: String) {
        val state = _uiState.value as? HomeUiState.Success ?: return
        val dayId = state.todayTraining?.dayId ?: return
        emitEvent(HomeUiEvent.NavigateToWorkoutSession(planId, dayId))
    }

    fun onViewTrainingDetail(planId: String) {
        emitEvent(HomeUiEvent.NavigateToTrainingDetail(planId))
    }

    fun onLogMeal() {
        emitEvent(HomeUiEvent.NavigateToTrackMeal)
    }

    fun onLogWeight() {
        emitEvent(HomeUiEvent.ShowLogWeightSheet)
    }

    fun onSaveWeight(weight: Double) {
        viewModelScope.launch {
            val request = LogBodyWeightRequestDto(
                weight = weight,
                date = LocalDate.now().toString(),
            )
            when (val result = logBodyWeightUseCase(request)) {
                is Result.Success -> {
                    val newWeightEntries = loadWeightHistory()
                    _uiState.update { current ->
                        if (current is HomeUiState.Success) current.copy(weightEntries = newWeightEntries) else current
                    }
                    emitEvent(HomeUiEvent.ShowSnackbar("Peso registrado ✓"))
                }
                is Result.Error -> {
                    emitEvent(HomeUiEvent.ShowSnackbar(result.exception.toMessage()))
                }
                is Result.Loading -> Unit
            }
        }
    }

    fun onProgressDashboard() {
        emitEvent(HomeUiEvent.NavigateToProgressDashboard)
    }

    fun onBodyWeight() {
        emitEvent(HomeUiEvent.NavigateToBodyWeight)
    }

    fun onGamification(tab: String) {
        emitEvent(HomeUiEvent.NavigateToGamification(tab))
    }

    fun onProfile() {
        emitEvent(HomeUiEvent.NavigateToProfile)
    }

    fun onCreatePlan() {
        emitEvent(HomeUiEvent.NavigateToGeneratePlan)
    }

    fun onResumed() {
        if (loadJob?.isActive == true) {
            Log.d("AIFIT_HOME", "onResumed skipped — initial load in progress")
            return
        }
        Log.d("AIFIT_HOME", "onResumed called")
        viewModelScope.launch {
            _uiState.update { cur ->
                if (cur is HomeUiState.Success) {
                    cur.copy(isRefreshingPlan = true, isRefreshingMeal = true)
                } else cur
            }

            kotlinx.coroutines.coroutineScope {
                launch { refreshUserProfileOnResume() }
                launch { syncTrainingPlansOnResume() }
                launch { syncDietPlansOnResume() }
            }

            _uiState.update { cur ->
                if (cur is HomeUiState.Success) {
                    cur.copy(isRefreshingPlan = false, isRefreshingMeal = false)
                } else cur
            }
        }
    }

    private suspend fun syncTrainingPlansOnResume() {
        getTrainingPlansUseCase().collect { result ->
            if (result !is Result.Success) return@collect

            val resolvedActivePlan = result.data.find { it.status == PlanStatus.ACTIVE }

            when {
                resolvedActivePlan != null && resolvedActivePlan.id != cachedActivePlanId -> {
                    Log.d("AIFIT_HOME", "onResumed — new active training plan id=${resolvedActivePlan.id}")
                    cachedActivePlanSummary = ActivePlanSummary(resolvedActivePlan.id, resolvedActivePlan.name)
                    cachedActivePlanId = resolvedActivePlan.id
                    cachedActivePlanDetail = null
                    _uiState.update { cur ->
                        if (cur is HomeUiState.Success) {
                            cur.copy(
                                activePlan = cachedActivePlanSummary,
                                todayTraining = null,
                                isRefreshingPlan = true,
                            )
                        } else cur
                    }
                    val detail = loadPlanDetail(resolvedActivePlan.id)
                    if (detail != null) {
                        cachedActivePlanDetail = detail
                    }
                    refreshWorkoutStatus()
                }
                resolvedActivePlan != null && cachedActivePlanDetail == null -> {
                    Log.d("AIFIT_HOME", "onResumed — same training plan, retrying detail load")
                    cachedActivePlanSummary = ActivePlanSummary(resolvedActivePlan.id, resolvedActivePlan.name)
                    _uiState.update { cur ->
                        if (cur is HomeUiState.Success) {
                            cur.copy(activePlan = cachedActivePlanSummary, isRefreshingPlan = true)
                        } else cur
                    }
                    val detail = loadPlanDetail(resolvedActivePlan.id)
                    if (detail != null) {
                        cachedActivePlanDetail = detail
                    }
                    refreshWorkoutStatus()
                }
                resolvedActivePlan == null && cachedActivePlanId != null -> {
                    Log.d("AIFIT_HOME", "onResumed — active training plan cleared")
                    cachedActivePlanDetail = null
                    cachedActivePlanId = null
                    cachedActivePlanSummary = null
                    _uiState.update { current ->
                        if (current is HomeUiState.Success) {
                            current.copy(todayTraining = null, activePlan = null)
                        } else current
                    }
                }
                else -> refreshWorkoutStatus()
            }
        }
    }

    private suspend fun syncDietPlansOnResume() {
        getDietPlansUseCase().collect { result ->
            if (result !is Result.Success) return@collect

            val resolvedActiveDiet = result.data.find { it.status == PlanStatus.ACTIVE }

            when {
                resolvedActiveDiet != null && resolvedActiveDiet.id != cachedActiveDietId -> {
                    Log.d("AIFIT_HOME", "onResumed — new active diet plan id=${resolvedActiveDiet.id}")
                    cachedActiveDietId = resolvedActiveDiet.id
                    cachedActiveDietDetail = loadDietPlanDetail(resolvedActiveDiet.id)
                    refreshNextMeal()
                }
                resolvedActiveDiet != null && cachedActiveDietDetail == null -> {
                    Log.d("AIFIT_HOME", "onResumed — same diet plan, reloading detail")
                    cachedActiveDietId = resolvedActiveDiet.id
                    cachedActiveDietDetail = loadDietPlanDetail(resolvedActiveDiet.id)
                    refreshNextMeal()
                }
                resolvedActiveDiet == null && cachedActiveDietId != null -> {
                    Log.d("AIFIT_HOME", "onResumed — active diet plan cleared")
                    cachedActiveDietId = null
                    cachedActiveDietDetail = null
                    _uiState.update { cur ->
                        if (cur is HomeUiState.Success) {
                            cur.copy(nextMeal = NextMealState.NoPlan)
                        } else cur
                    }
                }
                else -> refreshNextMeal()
            }
        }
    }

    // ── Private helpers ──

    /**
     * Recalculates the "next meal" state using [cachedActiveDietDetail] and
     * the **current** [LocalTime.now], then pushes the update to the UI.
     * Called from [onResumed] so that stale meal suggestions are replaced
     * every time the user returns to the Home screen.
     */
    private fun refreshNextMeal() {
        val freshNextMeal = deriveNextMeal(cachedActiveDietDetail)
        _uiState.update { cur ->
            if (cur is HomeUiState.Success) cur.copy(nextMeal = freshNextMeal) else cur
        }
    }

    /**
     * Reconciles training/diet lists with the server, loads plan details in parallel,
     * and refreshes today's workout history. Runs after the cache-first [Success].
     */
    private suspend fun enrichTrainingAndDietFromNetwork(
        cachedTrainingPlans: List<TrainingPlan>,
        cachedDietPlans: List<DietPlan>,
    ) {
        val freshTrainingPlans: List<TrainingPlan>
        val freshDietPlans: List<DietPlan>
        var todayWorkoutLogs: List<WorkoutLog>

        coroutineScope {
            val trainingDeferred = async { awaitFreshPlans() }
            val dietDeferred = async { awaitFreshDietPlans() }
            val workoutDeferred = async { loadTodayWorkoutHistoryFresh() }
            freshTrainingPlans = trainingDeferred.await()
            freshDietPlans = dietDeferred.await()
            todayWorkoutLogs = workoutDeferred.await()
        }

        val resolvedActivePlan = freshTrainingPlans.find { it.status == PlanStatus.ACTIVE }
            ?: cachedTrainingPlans.find { it.status == PlanStatus.ACTIVE }
        val resolvedActiveDiet = freshDietPlans.find { it.status == PlanStatus.ACTIVE }
            ?: cachedDietPlans.find { it.status == PlanStatus.ACTIVE }

        // Stale-while-revalidate: show cached plan detail before network GET
        resolvedActivePlan?.let { plan ->
            getTrainingPlanDetailUseCase.fromCache(plan.id)?.let { cachedDetail ->
                cachedActivePlanDetail = cachedDetail
                cachedActivePlanSummary = ActivePlanSummary(cachedDetail.id, cachedDetail.name)
                val cachedTraining = deriveTodayTraining(
                    cachedDetail, cachedWeeklySummary, todayWorkoutLogs,
                )
                _uiState.update { cur ->
                    if (cur is HomeUiState.Success) {
                        cur.copy(
                            activePlan = cachedActivePlanSummary,
                            todayTraining = cachedTraining,
                            isRefreshingPlan = true,
                        )
                    } else cur
                }
            }
        }

        val planDetail: TrainingPlan?
        val dietDetail: DietPlan?

        coroutineScope {
            val planDetailDeferred = resolvedActivePlan?.let { plan ->
                async { loadPlanDetail(plan.id) }
            }
            val dietDetailDeferred = resolvedActiveDiet?.let { diet ->
                async { loadDietPlanDetail(diet.id) }
            }
            planDetail = planDetailDeferred?.await()
            dietDetail = dietDetailDeferred?.await()
        }

        cachedActivePlanId = resolvedActivePlan?.id
        cachedActivePlanDetail = planDetail
        cachedActivePlanSummary = when {
            planDetail != null -> ActivePlanSummary(planDetail.id, planDetail.name)
            resolvedActivePlan != null -> ActivePlanSummary(resolvedActivePlan.id, resolvedActivePlan.name)
            else -> null
        }
        cachedActiveDietId = resolvedActiveDiet?.id
        cachedActiveDietDetail = dietDetail

        val todayTraining = deriveTodayTraining(
            planDetail, cachedWeeklySummary, todayWorkoutLogs,
        )
        val nextMeal = deriveNextMeal(dietDetail)

        Log.d("AIFIT_DEBUG", "[VM][enrich] todayTraining=${todayTraining?.planId} nextMeal=${nextMeal::class.simpleName}")

        _uiState.update { cur ->
            if (cur is HomeUiState.Success) {
                cur.copy(
                    activePlan = cachedActivePlanSummary,
                    todayTraining = todayTraining,
                    nextMeal = nextMeal,
                    isRefreshingPlan = false,
                    isRefreshingMeal = false,
                )
            } else cur
        }
    }

    /** Loads gamification and weekly summary off the critical path (after [enrichTrainingAndDietFromNetwork]). */
    private suspend fun loadSecondaryHomeData() {
        val weeklySummary: WeeklyProgressSummary?
        val streaks: List<Streak>
        val userAchievements: List<UserAchievement>
        val allDefinitions: List<AchievementDefinition>

        coroutineScope {
            val weeklyDeferred = async { loadWeeklySummary() }
            val streaksDeferred = async { loadStreaks() }
            val achievementsDeferred = async { loadUserAchievements() }
            val definitionsDeferred = async { loadAchievementDefinitions() }
            weeklySummary = weeklyDeferred.await()
            streaks = streaksDeferred.await()
            userAchievements = achievementsDeferred.await()
            allDefinitions = definitionsDeferred.await()
        }

        cachedWeeklySummary = weeklySummary
        val motivation = deriveMotivation(userAchievements, allDefinitions, streaks)
        val workoutLogs = loadTodayWorkoutHistoryCached()
        val todayTraining = deriveTodayTraining(
            cachedActivePlanDetail, weeklySummary, workoutLogs,
        )

        _uiState.update { cur ->
            if (cur is HomeUiState.Success) {
                cur.copy(
                    weeklySummary = weeklySummary,
                    streaks = streaks,
                    lastAchievement = motivation.lastAchievement,
                    nextAchievement = motivation.nextAchievement,
                    trainingStreakDays = motivation.trainingStreakDays,
                    todayTraining = todayTraining ?: cur.todayTraining,
                )
            } else cur
        }
    }

    private suspend fun refreshWorkoutStatus() {
        if (_uiState.value !is HomeUiState.Success) return
        val freshWorkoutLogs = loadTodayWorkoutHistoryFresh()
        // AIFIT_DEBUG ── checkpoint 6: estado del caché al hacer refresh
        Log.d("AIFIT_DEBUG", "[VM][refresh] cachedActivePlanDetail=${
            if (cachedActivePlanDetail == null) "NULL"
            else "id=${cachedActivePlanDetail!!.id} days=${cachedActivePlanDetail!!.days.size}"
        }")
        // TODO: remove diagnostic logs below
        Log.d("AIFIT_HOME", "refreshWorkoutStatus — freshLogs count=${freshWorkoutLogs.size}")
        Log.d("AIFIT_HOME", "freshLogs detail — ${freshWorkoutLogs.map { "id=${it.id} isLocked=${it.isLocked} planId=${it.trainingPlanId}" }}")
        val todayTraining = deriveTodayTraining(
            cachedActivePlanDetail, cachedWeeklySummary, freshWorkoutLogs,
        )
        // Keep or derive ActivePlanSummary: use cached detail if available, otherwise
        // fall back to cached summary (covers the case where detail load failed but
        // cachedActivePlanId is set), then preserve whatever is already in state.
        val freshActivePlanSummary = cachedActivePlanDetail
            ?.let { ActivePlanSummary(it.id, it.name) }
            ?: cachedActivePlanSummary
            ?: (_uiState.value as? HomeUiState.Success)?.activePlan
        _uiState.update { current ->
            if (current is HomeUiState.Success) current.copy(
                todayTraining = todayTraining,
                activePlan = freshActivePlanSummary,
            ) else current
        }
    }

    private suspend fun loadProfile(): UserProfile? {
        var profile: UserProfile? = null
        getUserProfileUseCase().collect { result ->
            if (result is Result.Success) profile = result.data
        }
        return profile
    }

    /** Refreshes name and avatar after returning from profile (e.g. photo upload). */
    private suspend fun refreshUserProfileOnResume() {
        val profile = loadProfile() ?: return
        Log.d("AIFIT_HOME", "refreshUserProfileOnResume avatarUrl=${profile.profilePictureUrl}")
        _uiState.update { cur ->
            if (cur is HomeUiState.Success) {
                cur.copy(userName = profile.name, avatarUrl = profile.profilePictureUrl)
            } else cur
        }
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

    private suspend fun loadNutrition(): Pair<NutritionLog?, NutritionTarget?> = coroutineScope {
        val today = LocalDate.now()
        val logDeferred = async {
            getNutritionLogUseCase(today)
                .first { it !is Result.Loading }
                .let { r -> if (r is Result.Success) r.data else null }
        }
        val targetDeferred = async {
            getCurrentNutritionTargetUseCase()
                .first { it !is Result.Loading }
                .let { r -> if (r is Result.Success) r.data else null }
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

    private suspend fun loadWeightHistory(): List<BodyWeightLog> {
        val today = LocalDate.now()
        return getBodyWeightHistoryUseCase(
            today.minusDays(30).toString(),
            today.toString(),
        )
            .first { it !is Result.Loading }
            .let { r -> if (r is Result.Success) r.data.takeLast(7) else emptyList() }
    }

    /** Cache-first: first successful emission (Room before network); does not wait for network. */
    private suspend fun loadTodayWorkoutHistoryCached(): List<WorkoutLog> {
        val today = LocalDate.now().toString()
        var latest = emptyList<WorkoutLog>()
        getWorkoutHistoryUseCase(from = today, to = today).collect { result ->
            if (result is Result.Success) {
                latest = result.data
                return@collect
            }
        }
        return latest
    }

    /** Waits for cache + network so completion / isLocked reflect the server. */
    private suspend fun loadTodayWorkoutHistoryFresh(): List<WorkoutLog> {
        val today = LocalDate.now().toString()
        var result = emptyList<WorkoutLog>()
        getWorkoutHistoryUseCase(from = today, to = today)
            .collect { r ->
                Log.d("AIFIT_HOME", "loadTodayWorkoutHistoryFresh — ${r::class.simpleName}")
                if (r is Result.Success) result = r.data
            }
        return result
    }

    private fun deriveTodayTraining(
        activePlan: TrainingPlan?,
        weeklySummary: WeeklyProgressSummary?,
        todayWorkoutLogs: List<WorkoutLog>,
    ): TodayTrainingState? {
        // AIFIT_DEBUG ── checkpoint 5: razón exacta de retorno null
        if (activePlan == null) {
            Log.w("AIFIT_DEBUG", "[VM][derive] RETURN NULL — activePlan is null")
            return null
        }
        if (activePlan.days.isEmpty()) {
            Log.w("AIFIT_DEBUG", "[VM][derive] RETURN NULL — activePlan.days isEmpty (planId=${activePlan.id})")
            return null
        }

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
            if (nonRestDays.isEmpty()) {
                Log.d("AIFIT_DEBUG", "Today is $today. Found workout: Rest Day (no non-rest days in plan)")
                return null
            }
            todayTrainingDay = nonRestDays[(today.value - 1) % nonRestDays.size]
        }

        if (todayTrainingDay.dayType == TrainingDayType.REST) {
            Log.d("AIFIT_DEBUG", "Today is $today. Found workout: Rest Day (dayType=REST, day='${todayTrainingDay.name}')")
            return null
        }

        val isCompleted = todayWorkoutLogs.any { it.trainingPlanId == activePlan.id && it.isLocked }
        // TODO: remove diagnostic logs below
        Log.d("AIFIT_HOME", "deriveTodayTraining — activePlan=${activePlan.id} todayLogs=${todayWorkoutLogs.size} isCompleted=$isCompleted")
        Log.d("AIFIT_HOME", "todayLogs detail — ${todayWorkoutLogs.map { "id=${it.id} isLocked=${it.isLocked} planId=${it.trainingPlanId}" }}")
        Log.d("AIFIT_DEBUG", "Today is $today. Found workout: ${todayTrainingDay.name}")

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

        // AIFIT_DEBUG — BUG-B: log timezone and current time to diagnose emulator issues
        Log.d("AIFIT_DEBUG", "[NextMeal] systemZone=${java.time.ZoneId.systemDefault()} LocalTime.now()=$now meals=${todayDietDay.meals.map { "${it.name}@${it.time}" }}")

        // Sort meals by their scheduled time, then pick the first one strictly after now
        val sortedMeals = todayDietDay.meals
            .sortedBy { parseMealTime(it.time, it.mealType) }

        sortedMeals.forEach { meal ->
            val mealTime = parseMealTime(meal.time, meal.mealType)
            Log.d("AIFIT_DEBUG", "[NextMeal]   ${meal.name} parsed=$mealTime isAfterNow=${mealTime.isAfter(now)}")
        }

        val nextMeal = sortedMeals
            .firstOrNull { parseMealTime(it.time, it.mealType).isAfter(now) }
            ?: return NextMealState.AllDone

        Log.d("AIFIT_DEBUG", "[NextMeal] selected=${nextMeal.name}@${nextMeal.time}")

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

    companion object {
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

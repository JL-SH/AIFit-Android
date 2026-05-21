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

            // ── Phase 1: Load ALL data in parallel ──
            // awaitFreshPlans/awaitFreshDietPlans collect the full flow (cache + network)
            // so initialTrainingPlans/initialDietPlans always contain fresh server IDs,
            // not just the Room-cached ones.
            val profileDeferred = async { loadProfile() }
            val nutritionDeferred = async { loadNutrition() }
            val weeklyDeferred = async { loadWeeklySummary() }
            val streaksDeferred = async { loadStreaks() }
            val weightDeferred = async { loadWeightHistory() }
            val workoutHistoryDeferred = async { loadTodayWorkoutHistory() }
            val trainingPlansDeferred = async { awaitFreshPlans() }
            val dietPlansDeferred = async { awaitFreshDietPlans() }
            val achievementsDeferred = async { loadUserAchievements() }
            val definitionsDeferred = async { loadAchievementDefinitions() }

            val profile = profileDeferred.await()
            val nutritionPair = nutritionDeferred.await()
            val weeklySummary = weeklyDeferred.await()
            val streaks = streaksDeferred.await()
            val weightEntries = weightDeferred.await()
            val todayWorkoutLogs = workoutHistoryDeferred.await()
            val initialTrainingPlans = trainingPlansDeferred.await()
            val initialDietPlans = dietPlansDeferred.await()
            val userAchievements = achievementsDeferred.await()
            val allDefinitions = definitionsDeferred.await()

            // AIFIT_DEBUG ── checkpoint 1: qué devolvió awaitFreshPlans
            Log.d("AIFIT_DEBUG", "[VM][loadAll] awaitFreshPlans count=${initialTrainingPlans.size}")
            initialTrainingPlans.forEach { p ->
                Log.d("AIFIT_DEBUG", "[VM][loadAll]   plan id=${p.id} status=${p.status}")
            }

            if (profile == null) {
                _uiState.value = HomeUiState.Error("No se pudo cargar el perfil")
                return@launch
            }

            val todayNutrition = deriveNutrition(nutritionPair.first, nutritionPair.second)

            // Derive initial training state — IDs are guaranteed fresh from network
            val initialActivePlan = initialTrainingPlans.find { it.status == PlanStatus.ACTIVE }
            // AIFIT_DEBUG ── checkpoint 2: ¿se encontró un plan ACTIVE?
            if (initialActivePlan == null) {
                Log.w("AIFIT_DEBUG", "[VM][loadAll] initialActivePlan = NULL — ningún plan tiene status=ACTIVE en la lista de ${initialTrainingPlans.size} planes")
            } else {
                Log.d("AIFIT_DEBUG", "[VM][loadAll] initialActivePlan FOUND id=${initialActivePlan.id} status=${initialActivePlan.status}")
            }

            val initialActivePlanDetail = initialActivePlan?.let {
                val detail = loadPlanDetail(it.id)
                // AIFIT_DEBUG ── checkpoint 3: ¿loadPlanDetail tuvo éxito?
                if (detail == null) {
                    Log.w("AIFIT_HOME",
                        "loadAll — loadPlanDetail returned null for planId=${it.id}. " +
                        "API unavailable or stale ID. UI will show null training; onResumed() will retry.")
                } else {
                    Log.d("AIFIT_DEBUG", "[VM][loadAll] loadPlanDetail OK planId=${detail.id} days=${detail.days.size} exercises=${detail.days.sumOf { d -> d.exercises.size }}")
                }
                detail
            }
            cachedActivePlanDetail = initialActivePlanDetail
            cachedActivePlanId = initialActivePlan?.id
            cachedWeeklySummary = weeklySummary
            val initialTodayTraining = deriveTodayTraining(
                initialActivePlanDetail, weeklySummary, todayWorkoutLogs,
            )

            // Build ActivePlanSummary so the UI can distinguish "rest day" from "no plan"
            val activePlanSummary = when {
                initialActivePlanDetail != null ->
                    ActivePlanSummary(id = initialActivePlanDetail.id, name = initialActivePlanDetail.name)
                initialActivePlan != null ->
                    ActivePlanSummary(id = initialActivePlan.id, name = initialActivePlan.name)
                else -> null
            }
            cachedActivePlanSummary = activePlanSummary

            // Derive initial diet/meal state
            val initialActiveDiet = initialDietPlans.find { it.status == PlanStatus.ACTIVE }
            cachedActiveDietId = initialActiveDiet?.id
            val initialActiveDietDetail = initialActiveDiet?.let { loadDietPlanDetail(it.id) }
            cachedActiveDietDetail = initialActiveDietDetail
            val initialNextMeal = deriveNextMeal(initialActiveDietDetail)

            // ── Derive motivation data (BUG-026) ──
            val motivation = deriveMotivation(userAchievements, allDefinitions, streaks)

            // ── Phase 2: Emit initial state with real plan data ──
            _uiState.value = HomeUiState.Success(
                userName = profile.name,
                avatarUrl = profile.profilePictureUrl,
                activePlan = activePlanSummary,
                todayTraining = initialTodayTraining,
                todayNutrition = todayNutrition,
                nextMeal = initialNextMeal,
                streaks = streaks,
                weeklySummary = weeklySummary,
                weightEntries = weightEntries,
                lastAchievement = motivation.lastAchievement,
                nextAchievement = motivation.nextAchievement,
                trainingStreakDays = motivation.trainingStreakDays,
            )
            // AIFIT_DEBUG ── checkpoint 4: estado final emitido
            Log.d("AIFIT_DEBUG", "[VM][loadAll] _uiState = Success — todayTraining=${
                if (initialTodayTraining == null) "NULL"
                else "planId=${initialTodayTraining.planId} dayId=${initialTodayTraining.dayId} exercises=${initialTodayTraining.exerciseCount}"
            }")

            // ── Phase 3: Diet-only background sync ──
            // Training plans are NOT re-collected here: awaitFreshPlans() in Phase 1
            // already waited for the network response, so cachedActivePlanId is already
            // the fresh server ID. Re-collecting would only duplicate the API call.
            // Diet plans are still re-collected to catch server-side changes that could
            // have raced with the Phase 1 collection.
            launch {
                var latestDietPlans = initialDietPlans
                getDietPlansUseCase().collect { result ->
                    if (result is Result.Success) latestDietPlans = result.data
                }
                val freshActiveDiet = latestDietPlans.find { it.status == PlanStatus.ACTIVE }
                if (freshActiveDiet != null && freshActiveDiet.id != initialActiveDiet?.id) {
                    val detail = loadDietPlanDetail(freshActiveDiet.id)
                    cachedActiveDietDetail = detail  // BUG-B: keep cache in sync
                    val nextMeal = deriveNextMeal(detail)
                    _uiState.update { cur ->
                        if (cur is HomeUiState.Success) cur.copy(nextMeal = nextMeal) else cur
                    }
                }
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
        Log.d("AIFIT_HOME", "onResumed called")
        viewModelScope.launch {
            _uiState.update { cur ->
                if (cur is HomeUiState.Success) {
                    cur.copy(isRefreshingPlan = true, isRefreshingMeal = true)
                } else cur
            }

            kotlinx.coroutines.coroutineScope {
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

    private suspend fun refreshWorkoutStatus() {
        if (_uiState.value !is HomeUiState.Success) return
        val freshWorkoutLogs = loadTodayWorkoutHistory()
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

    private suspend fun loadProfile(): UserProfile? =
        getUserProfileUseCase()
            .first { it !is Result.Loading }
            .let { r -> if (r is Result.Success) r.data else null }


    private suspend fun loadPlanDetail(planId: String): TrainingPlan? =
        when (val r = getTrainingPlanDetailUseCase(planId)) {
            is Result.Success -> r.data
            else -> null
        }

    /**
     * Collects the full getTrainingPlansUseCase() flow (cache emission + network emission)
     * and returns the last [Result.Success] data, or an empty list if every emission failed.
     * Using this instead of .first { it !is Result.Loading } guarantees that the returned
     * list always contains the fresh server IDs, not the potentially stale Room-cached ones.
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

    private suspend fun loadNutrition(): Pair<NutritionLog?, NutritionTarget?> {
        val today = LocalDate.now()
        val log = getNutritionLogUseCase(today)
            .first { it !is Result.Loading }
            .let { r -> if (r is Result.Success) r.data else null }
        val target = getCurrentNutritionTargetUseCase()
            .first { it !is Result.Loading }
            .let { r -> if (r is Result.Success) r.data else null }
        return log to target
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

    private suspend fun loadTodayWorkoutHistory(): List<WorkoutLog> {
        val today = LocalDate.now().toString()
        var result = emptyList<WorkoutLog>()
        getWorkoutHistoryUseCase(from = today, to = today)
            .collect { r ->
                // TODO: remove diagnostic log below
                Log.d("AIFIT_HOME", "loadTodayWorkoutHistory emission — ${r::class.simpleName} data=${if (r is Result.Success) r.data.map { "id=${it.id} isLocked=${it.isLocked}" } else "N/A"}")
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

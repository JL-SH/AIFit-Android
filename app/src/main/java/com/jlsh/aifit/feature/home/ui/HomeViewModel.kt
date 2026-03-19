package com.jlsh.aifit.feature.home.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.core.common.toMessage
import com.jlsh.aifit.feature.diet.domain.model.DietPlan
import com.jlsh.aifit.feature.diet.domain.model.MealType
import com.jlsh.aifit.feature.diet.domain.usecase.GetDietPlanDetailUseCase
import com.jlsh.aifit.feature.diet.domain.usecase.GetDietPlansUseCase
import com.jlsh.aifit.feature.gamification.domain.model.Streak
import com.jlsh.aifit.feature.gamification.domain.usecase.GetUserStreaksUseCase
import com.jlsh.aifit.feature.home.ui.state.HomeUiEvent
import com.jlsh.aifit.feature.home.ui.state.HomeUiState
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
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
    private val getBodyWeightHistoryUseCase: GetBodyWeightHistoryUseCase,
    private val getWorkoutHistoryUseCase: GetWorkoutHistoryUseCase,
    private val logBodyWeightUseCase: LogBodyWeightUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _events = Channel<HomeUiEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    private var loadJob: Job? = null

    init {
        loadAll()
    }

    fun loadAll() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _uiState.value = HomeUiState.Loading

            // ── Phase 1: Load ALL data in parallel (including first plan emissions) ──
            val profileDeferred = async { loadProfile() }
            val nutritionDeferred = async { loadNutrition() }
            val weeklyDeferred = async { loadWeeklySummary() }
            val streaksDeferred = async { loadStreaks() }
            val weightDeferred = async { loadWeightHistory() }
            val workoutHistoryDeferred = async { loadTodayWorkoutHistory() }
            val trainingPlansDeferred = async {
                getTrainingPlansUseCase().first { it !is Result.Loading }
            }
            val dietPlansDeferred = async {
                getDietPlansUseCase().first { it !is Result.Loading }
            }

            val profile = profileDeferred.await()
            val nutritionPair = nutritionDeferred.await()
            val weeklySummary = weeklyDeferred.await()
            val streaks = streaksDeferred.await()
            val weightEntries = weightDeferred.await()
            val todayWorkoutLogs = workoutHistoryDeferred.await()
            val trainingPlansResult = trainingPlansDeferred.await()
            val dietPlansResult = dietPlansDeferred.await()

            if (profile == null) {
                _uiState.value = HomeUiState.Error("No se pudo cargar el perfil")
                return@launch
            }

            val todayNutrition = deriveNutrition(nutritionPair.first, nutritionPair.second)

            // Derive initial training state from the first plan emission
            val initialTrainingPlans = (trainingPlansResult as? Result.Success)?.data ?: emptyList()
            val initialActivePlan = initialTrainingPlans.find { it.status == PlanStatus.ACTIVE }
            val initialActivePlanDetail = initialActivePlan?.let { loadPlanDetail(it.id) }
            val initialTodayTraining = deriveTodayTraining(
                initialActivePlanDetail, weeklySummary, todayWorkoutLogs,
            )

            // Derive initial diet/meal state from the first plan emission
            val initialDietPlans = (dietPlansResult as? Result.Success)?.data ?: emptyList()
            val initialActiveDiet = initialDietPlans.find { it.status == PlanStatus.ACTIVE }
            val initialActiveDietDetail = initialActiveDiet?.let { loadDietPlanDetail(it.id) }
            val initialNextMeal = deriveNextMeal(initialActiveDietDetail, nutritionPair.first)

            // ── Phase 2: Emit initial state with real plan data ──
            _uiState.value = HomeUiState.Success(
                userName = profile.name,
                avatarUrl = profile.profilePictureUrl,
                todayTraining = initialTodayTraining,
                todayNutrition = todayNutrition,
                nextMeal = initialNextMeal,
                streaks = streaks,
                weeklySummary = weeklySummary,
                weightEntries = weightEntries,
            )

            // ── Phase 3: Reactively update from subsequent emissions (skip the first already consumed) ──
            launch {
                getTrainingPlansUseCase().drop(1).collect { result ->
                    if (result !is Result.Success) return@collect
                    val plans = result.data
                    val activePlan = plans.find { it.status == PlanStatus.ACTIVE }
                    val activePlanDetail = activePlan?.let { loadPlanDetail(it.id) }
                    val todayTraining = deriveTodayTraining(
                        activePlanDetail, weeklySummary, todayWorkoutLogs,
                    )
                    val current = _uiState.value
                    if (current is HomeUiState.Success) {
                        _uiState.value = current.copy(todayTraining = todayTraining)
                    }
                }
            }

            launch {
                getDietPlansUseCase().drop(1).collect { result ->
                    if (result !is Result.Success) return@collect
                    val plans = result.data
                    val activeDietPlan = plans.find { it.status == PlanStatus.ACTIVE }
                    val activeDietDetail = activeDietPlan?.let { loadDietPlanDetail(it.id) }
                    val nextMeal = deriveNextMeal(activeDietDetail, nutritionPair.first)
                    val current = _uiState.value
                    if (current is HomeUiState.Success) {
                        _uiState.value = current.copy(nextMeal = nextMeal)
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
                    val currentState = _uiState.value
                    if (currentState is HomeUiState.Success) {
                        _uiState.value = currentState.copy(weightEntries = newWeightEntries)
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

    // ── Private helpers ──

    private suspend fun loadProfile(): UserProfile? =
        getUserProfileUseCase()
            .first { it !is Result.Loading }
            .let { r -> if (r is Result.Success) r.data else null }


    private suspend fun loadPlanDetail(planId: String): TrainingPlan? =
        when (val r = getTrainingPlanDetailUseCase(planId)) {
            is Result.Success -> r.data
            else -> null
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
        return getWorkoutHistoryUseCase(from = today, to = today)
            .first { it !is Result.Loading }
            .let { r -> if (r is Result.Success) r.data else emptyList() }
    }

    private fun deriveTodayTraining(
        activePlan: TrainingPlan?,
        weeklySummary: WeeklyProgressSummary?,
        todayWorkoutLogs: List<WorkoutLog>,
    ): TodayTrainingState? {
        if (activePlan == null || activePlan.days.isEmpty()) return null

        val today = java.time.DayOfWeek.from(LocalDate.now())
        val todayTrainingDay = activePlan.days.find { it.dayOfWeek == today } ?: return null

        if (todayTrainingDay.dayType == TrainingDayType.REST) return null

        val isCompleted = todayWorkoutLogs.any { it.trainingPlanId == activePlan.id }

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
        todayNutritionLog: NutritionLog?,
    ): NextMealState {
        if (activeDietPlan == null || activeDietPlan.days.isEmpty()) return NextMealState.NoPlan

        val dayOfWeek = LocalDate.now().dayOfWeek.value // 1=Monday … 7=Sunday
        val todayDietDay = activeDietPlan.days.getOrNull(
            (dayOfWeek - 1) % activeDietPlan.days.size,
        ) ?: return NextMealState.NoPlan

        val loggedMealTypes = todayNutritionLog?.meals
            ?.map { it.mealType }
            ?.toSet()
            ?: emptySet()

        val nextMeal = todayDietDay.meals.firstOrNull { it.mealType !in loggedMealTypes }
            ?: return NextMealState.AllDone

        return NextMealState.Upcoming(
            mealName = nextMeal.name,
            estimatedTime = estimatedTimeForMealType(nextMeal.mealType),
            calories = nextMeal.calories,
            proteinG = nextMeal.proteinGrams.toDouble(),
            carbsG = nextMeal.carbsGrams.toDouble(),
            fatG = nextMeal.fatGrams.toDouble(),
        )
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

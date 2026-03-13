package com.jlsh.aifit.feature.home.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.feature.gamification.domain.model.Streak
import com.jlsh.aifit.feature.gamification.domain.usecase.GetUserStreaksUseCase
import com.jlsh.aifit.feature.home.ui.state.HomeUiEvent
import com.jlsh.aifit.feature.home.ui.state.HomeUiState
import com.jlsh.aifit.feature.home.ui.state.TodayNutritionState
import com.jlsh.aifit.feature.home.ui.state.TodayTrainingState
import com.jlsh.aifit.feature.nutrition.domain.model.NutritionLog
import com.jlsh.aifit.feature.nutrition.domain.model.NutritionTarget
import com.jlsh.aifit.feature.nutrition.domain.usecase.GetCurrentNutritionTargetUseCase
import com.jlsh.aifit.feature.nutrition.domain.usecase.GetNutritionLogUseCase
import com.jlsh.aifit.feature.progress.domain.model.BodyWeightLog
import com.jlsh.aifit.feature.progress.domain.model.WeeklyProgressSummary
import com.jlsh.aifit.feature.progress.domain.usecase.GetBodyWeightHistoryUseCase
import com.jlsh.aifit.feature.progress.domain.usecase.GetWeeklyProgressSummaryUseCase
import com.jlsh.aifit.feature.training.domain.model.PlanStatus
import com.jlsh.aifit.feature.training.domain.model.TrainingPlan
import com.jlsh.aifit.feature.training.domain.usecase.GetTrainingPlansUseCase
import com.jlsh.aifit.feature.user.domain.model.UserProfile
import com.jlsh.aifit.feature.user.domain.usecase.GetUserProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
    private val getNutritionLogUseCase: GetNutritionLogUseCase,
    private val getCurrentNutritionTargetUseCase: GetCurrentNutritionTargetUseCase,
    private val getWeeklyProgressSummaryUseCase: GetWeeklyProgressSummaryUseCase,
    private val getUserStreaksUseCase: GetUserStreaksUseCase,
    private val getBodyWeightHistoryUseCase: GetBodyWeightHistoryUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _events = Channel<HomeUiEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    init {
        loadAll()
    }

    fun loadAll() {
        viewModelScope.launch {
            _uiState.value = HomeUiState.Loading

            // ── Parallel loads ──
            val profileDeferred = async { loadProfile() }
            val plansDeferred = async { loadPlans() }
            val nutritionDeferred = async { loadNutrition() }
            val weeklyDeferred = async { loadWeeklySummary() }
            val streaksDeferred = async { loadStreaks() }
            val weightDeferred = async { loadWeightHistory() }

            val profile = profileDeferred.await()
            val plans = plansDeferred.await()
            val nutritionPair = nutritionDeferred.await()
            val weeklySummary = weeklyDeferred.await()
            val streaks = streaksDeferred.await()
            val weightEntries = weightDeferred.await()

            if (profile == null) {
                _uiState.value = HomeUiState.Error("No se pudo cargar el perfil")
                return@launch
            }

            val todayTraining = deriveTodayTraining(plans, weeklySummary)
            val todayNutrition = deriveNutrition(nutritionPair.first, nutritionPair.second)

            _uiState.value = HomeUiState.Success(
                userName = profile.name,
                avatarUrl = profile.profilePictureUrl,
                todayTraining = todayTraining,
                todayNutrition = todayNutrition,
                streaks = streaks,
                weeklySummary = weeklySummary,
                weightEntries = weightEntries,
            )
        }
    }

    fun onStartSession(planId: String) {
        emitEvent(HomeUiEvent.NavigateToWorkoutLog(planId))
    }

    fun onLogMeal() {
        emitEvent(HomeUiEvent.NavigateToTrackMeal)
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

    private suspend fun loadPlans(): List<TrainingPlan> =
        getTrainingPlansUseCase()
            .first { it !is Result.Loading }
            .let { r -> if (r is Result.Success) r.data else emptyList() }

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
            today.toString()
        )
            .first { it !is Result.Loading }
            .let { r -> if (r is Result.Success) r.data.takeLast(7) else emptyList() }
    }

    private fun deriveTodayTraining(
        plans: List<TrainingPlan>,
        weeklySummary: WeeklyProgressSummary?,
    ): TodayTrainingState? {
        val activePlan = plans.find { it.status == PlanStatus.ACTIVE } ?: return null
        if (activePlan.totalDays == 0) return null

        val dayOfWeek = LocalDate.now().dayOfWeek.value
        val dayNumber = (dayOfWeek - 1) % activePlan.totalDays + 1

        val adherence = if (weeklySummary != null && weeklySummary.workoutsTarget > 0) {
            (weeklySummary.workoutsThisWeek.toFloat() / weeklySummary.workoutsTarget * 100f)
                .coerceIn(0f, 100f)
        } else 0f

        return TodayTrainingState(
            planId = activePlan.id,
            planName = activePlan.name,
            dayName = "Día $dayNumber",
            exerciseCount = 0,
            adherencePercentage = adherence,
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
    }
}

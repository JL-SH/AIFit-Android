package com.jlsh.aifit.feature.home.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.util.Log
import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.core.common.toMessage
import com.jlsh.aifit.feature.diet.domain.model.DietPlan
import com.jlsh.aifit.feature.diet.domain.model.Meal
import com.jlsh.aifit.feature.diet.domain.model.MealType
import com.jlsh.aifit.feature.diet.domain.util.mealsForToday
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
import com.jlsh.aifit.feature.nutrition.domain.util.toTrackMealRequestDto
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
 * ViewModel del dashboard de inicio: perfil, entreno y nutrición de hoy, rachas y peso.
 *
 * **UiState expuesto** ([uiState] — [HomeUiState]):
 * - [HomeUiState.Loading]: carga inicial del dashboard; skeleton hasta snapshot completo.
 * - [HomeUiState.Error]: fallo crítico (p. ej. perfil no cargado).
 * - [HomeUiState.Success]: dashboard con tarjetas de entreno, nutrición, próxima comida, peso y gamificación.
 *
 * **Eventos emitidos** ([events] — [HomeUiEvent]):
 * - [HomeUiEvent.NavigateToWorkoutSession]: iniciar sesión de entreno (planId, dayId).
 * - [HomeUiEvent.NavigateToTrainingDetail]: detalle del plan de entrenamiento.
 * - [HomeUiEvent.ShowTrackMealSheet]: abrir selector de modo de registro de comida.
 * - [HomeUiEvent.NavigateToProgressDashboard]: panel de progreso semanal.
 * - [HomeUiEvent.NavigateToBodyWeight]: historial de peso corporal.
 * - [HomeUiEvent.NavigateToGamification]: pantalla de logros/rachas (tab).
 * - [HomeUiEvent.NavigateToProfile]: perfil del usuario.
 * - [HomeUiEvent.NavigateToGeneratePlan]: generar nuevo plan de entrenamiento.
 * - [HomeUiEvent.ShowLogWeightSheet]: abrir hoja modal para registrar peso.
 * - [HomeUiEvent.ShowSnackbar]: mensaje transitorio (éxito o error al guardar peso).
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
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)

    /** Estado del dashboard de inicio; observar con `collectAsStateWithLifecycle`. */
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _events = Channel<HomeUiEvent>(Channel.BUFFERED)

    /** Eventos de navegación y UI de un solo uso; consumir en [HomeScreen]. */
    val events = _events.receiveAsFlow()

    private val _planPickerMeals = MutableStateFlow<List<Meal>>(emptyList())

    /** Comidas del plan activo para el día actual; se cargan al abrir el picker. */
    val planPickerMeals: StateFlow<List<Meal>> = _planPickerMeals.asStateFlow()

    private var loadJob: Job? = null
    private var refreshJob: Job? = null
    private var resumeDebounceJob: Job? = null
    private var cachedActivePlanDetail: TrainingPlan? = null
    private var cachedActivePlanId: String? = null       // Fix 2: survives a null cachedActivePlanDetail
    private var cachedActivePlanSummary: ActivePlanSummary? = null
    private var cachedWeeklySummary: WeeklyProgressSummary? = null
    private var cachedActiveDietDetail: DietPlan? = null
    private var cachedActiveDietId: String? = null

    init {
        loadAll()
    }

    /** Recarga el home: cache-first instantáneo y reconciliación silenciosa en red. */
    fun loadAll() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            if (_uiState.value is HomeUiState.Success) {
                scheduleSilentRefresh()
                return@launch
            }

            var profile = firstSuccessProfile()
            if (profile == null) {
                _uiState.value = HomeUiState.Loading
                profile = withTimeoutOrNull(PROFILE_NETWORK_TIMEOUT_MS) {
                    loadProfileLastSuccess()
                }
            }

            if (profile == null) {
                _uiState.value = HomeUiState.Error("No se pudo cargar el perfil")
                return@launch
            }

            _uiState.value = HomeUiState.Loading

            val snapshot: HomeUiState.Success
            coroutineScope {
                val nutritionDeferred = async { loadNutritionCacheFirst() }
                val weightDeferred = async { firstSuccessWeightHistory() }
                val trainingPlansDeferred = async { firstSuccessTrainingPlans() }
                val dietPlansDeferred = async { firstSuccessDietPlans() }
                val workoutDeferred = async { firstSuccessWorkoutHistory() }
                val weeklyDeferred = async { loadWeeklySummary() }
                val streaksDeferred = async { loadStreaks() }
                val achievementsDeferred = async { loadUserAchievements() }
                val definitionsDeferred = async { loadAchievementDefinitions() }

                val trainingPlans = trainingPlansDeferred.await()
                val dietPlans = dietPlansDeferred.await()
                val initialActivePlan = trainingPlans.find { it.status == PlanStatus.ACTIVE }
                val initialActiveDiet = dietPlans.find { it.status == PlanStatus.ACTIVE }

                val planDetailDeferred = initialActivePlan?.let { plan ->
                    async {
                        getTrainingPlanDetailUseCase.fromCache(plan.id) ?: loadPlanDetail(plan.id)
                    }
                }
                val dietDetailDeferred = initialActiveDiet?.let { diet ->
                    async { loadDietPlanDetail(diet.id) }
                }

                snapshot = buildHomeSnapshot(
                    profile = profile,
                    nutritionPair = nutritionDeferred.await(),
                    weightEntries = weightDeferred.await(),
                    trainingPlans = trainingPlans,
                    dietPlans = dietPlans,
                    todayWorkoutLogs = workoutDeferred.await(),
                    weeklySummary = weeklyDeferred.await(),
                    streaks = streaksDeferred.await(),
                    userAchievements = achievementsDeferred.await(),
                    allDefinitions = definitionsDeferred.await(),
                    planDetail = planDetailDeferred?.await(),
                    dietDetail = dietDetailDeferred?.await(),
                )
            }

            _uiState.value = snapshot

            Log.d(
                "AIFIT_DEBUG",
                "[VM][loadAll] initial Success — activePlan=$cachedActivePlanId",
            )

            scheduleSilentRefresh()
        }
    }

    /**
     * Inicia la sesión de entreno del día si hay [TodayTrainingState] disponible.
     *
     * @param planId Identificador del plan activo.
     */
    fun onStartSession(planId: String) {
        val state = _uiState.value as? HomeUiState.Success ?: return
        val dayId = state.todayTraining?.dayId ?: return
        emitEvent(HomeUiEvent.NavigateToWorkoutSession(planId, dayId))
    }

    /**
     * Navega al detalle del plan de entrenamiento.
     *
     * @param planId Identificador del plan.
     */
    fun onViewTrainingDetail(planId: String) {
        emitEvent(HomeUiEvent.NavigateToTrainingDetail(planId))
    }

    /** Abre el bottom sheet para elegir el modo de registro de comida. */
    fun onLogMeal() {
        emitEvent(HomeUiEvent.ShowTrackMealSheet)
    }

    /** Carga las comidas de hoy del plan activo y abre el picker. */
    fun onShowPlanMealPicker() {
        viewModelScope.launch {
            _planPickerMeals.value = resolveTodayPlanMeals()
            emitEvent(HomeUiEvent.ShowPlanMealPicker)
        }
    }

    /**
     * Registra una comida del plan de dieta activo y actualiza el resumen nutricional del home.
     *
     * @param meal Comida seleccionada en el picker.
     */
    fun onTrackMealFromPlan(meal: Meal) {
        viewModelScope.launch {
            when (val result = trackMealUseCase(meal.toTrackMealRequestDto())) {
                is Result.Success -> {
                    val nutritionPair = loadNutritionCacheFirst()
                    val todayNutrition = deriveNutrition(nutritionPair.first, nutritionPair.second)
                    _uiState.update { current ->
                        if (current is HomeUiState.Success) {
                            current.copy(todayNutrition = todayNutrition)
                        } else current
                    }
                    emitEvent(HomeUiEvent.ShowSnackbar("Comida del plan registrada"))
                }
                is Result.Error -> {
                    emitEvent(HomeUiEvent.ShowSnackbar(result.exception.toMessage()))
                }
                is Result.Loading -> Unit
            }
        }
    }

    /** Abre la hoja modal para registrar el peso corporal. */
    fun onLogWeight() {
        emitEvent(HomeUiEvent.ShowLogWeightSheet)
    }

    /**
     * Persiste un nuevo registro de peso y actualiza [HomeUiState.Success.weightEntries].
     *
     * @param weight Peso en kilogramos.
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
                    _uiState.update { current ->
                        if (current is HomeUiState.Success) current.copy(weightEntries = newWeightEntries) else current
                    }
                    emitEvent(HomeUiEvent.WeightLoggedSuccessfully)
                }
                is Result.Error -> {
                    emitEvent(HomeUiEvent.ShowSnackbar(result.exception.toMessage()))
                }
                is Result.Loading -> Unit
            }
        }
    }

    /** Navega al panel de progreso semanal. */
    fun onProgressDashboard() {
        emitEvent(HomeUiEvent.NavigateToProgressDashboard)
    }

    /** Navega a la pantalla de historial de peso corporal. */
    fun onBodyWeight() {
        emitEvent(HomeUiEvent.NavigateToBodyWeight)
    }

    /**
     * Navega a gamificación (logros o rachas).
     *
     * @param tab Pestaña destino (p. ej. `"ACHIEVEMENTS"`).
     */
    fun onGamification(tab: String) {
        emitEvent(HomeUiEvent.NavigateToGamification(tab))
    }

    /** Navega al perfil del usuario. */
    fun onProfile() {
        emitEvent(HomeUiEvent.NavigateToProfile)
    }

    /** Navega al flujo de generación de un nuevo plan de entrenamiento. */
    fun onCreatePlan() {
        emitEvent(HomeUiEvent.NavigateToGeneratePlan)
    }

    /** Refresco silencioso al volver a la pantalla (ciclo RESUMED). */
    fun onResumed() {
        Log.d("AIFIT_HOME", "onResumed called")
        resumeDebounceJob?.cancel()
        resumeDebounceJob = viewModelScope.launch {
            delay(RESUME_DEBOUNCE_MS)
            scheduleSilentRefresh()
        }
    }

    // ── Private helpers ──

    private fun scheduleSilentRefresh() {
        refreshJob?.cancel()
        refreshJob = viewModelScope.launch {
            if (_uiState.value !is HomeUiState.Success) return@launch
            performSilentRefresh()
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
        val isTrainingHydrating = initialActivePlan != null && planDetail == null
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

    /**
     * Reconciles all dashboard sections from network/cache and applies a single [HomeUiState] update.
     */
    private suspend fun performSilentRefresh() {
        if (_uiState.value !is HomeUiState.Success) return

        val profile: UserProfile?
        val freshTrainingPlans: List<TrainingPlan>
        val freshDietPlans: List<DietPlan>
        val nutritionPair: Pair<NutritionLog?, NutritionTarget?>
        val freshWorkoutLogs: List<WorkoutLog>
        val weeklySummary: WeeklyProgressSummary?
        val streaks: List<Streak>
        val userAchievements: List<UserAchievement>
        val allDefinitions: List<AchievementDefinition>

        coroutineScope {
            val profileDeferred = async { loadProfileLastSuccess() }
            val trainingDeferred = async { awaitFreshPlans() }
            val dietDeferred = async { awaitFreshDietPlans() }
            val nutritionDeferred = async { loadNutritionCacheFirst() }
            val workoutDeferred = async { loadTodayWorkoutHistoryFresh() }
            val weeklyDeferred = async { loadWeeklySummary() }
            val streaksDeferred = async { loadStreaks() }
            val achievementsDeferred = async { loadUserAchievements() }
            val definitionsDeferred = async { loadAchievementDefinitions() }

            profile = profileDeferred.await()
            freshTrainingPlans = trainingDeferred.await()
            freshDietPlans = dietDeferred.await()
            nutritionPair = nutritionDeferred.await()
            freshWorkoutLogs = workoutDeferred.await()
            weeklySummary = weeklyDeferred.await()
            streaks = streaksDeferred.await()
            userAchievements = achievementsDeferred.await()
            allDefinitions = definitionsDeferred.await()
        }

        val resolvedActivePlan = freshTrainingPlans.find { it.status == PlanStatus.ACTIVE }
        val resolvedActiveDiet = freshDietPlans.find { it.status == PlanStatus.ACTIVE }

        if (resolvedActivePlan == null && cachedActivePlanId != null) {
            cachedActivePlanDetail = null
            cachedActivePlanId = null
            cachedActivePlanSummary = null
        } else if (resolvedActivePlan != null) {
            cachedActivePlanId = resolvedActivePlan.id
            val swrDetail = getTrainingPlanDetailUseCase.fromCache(resolvedActivePlan.id)
            if (swrDetail != null) {
                cachedActivePlanDetail = swrDetail
                cachedActivePlanSummary = ActivePlanSummary(swrDetail.id, swrDetail.name)
            } else {
                cachedActivePlanSummary = ActivePlanSummary(resolvedActivePlan.id, resolvedActivePlan.name)
            }
        }

        val planDetail: TrainingPlan?
        val dietDetail: DietPlan?

        coroutineScope {
            val planDetailDeferred = resolvedActivePlan?.let { plan ->
                async {
                    getTrainingPlanDetailUseCase.fromCache(plan.id) ?: loadPlanDetail(plan.id)
                }
            }
            val dietDetailDeferred = resolvedActiveDiet?.let { diet -> async { loadDietPlanDetail(diet.id) } }
            planDetail = planDetailDeferred?.await()
            dietDetail = dietDetailDeferred?.await()
        }

        if (resolvedActivePlan != null) {
            cachedActivePlanId = resolvedActivePlan.id
            cachedActivePlanDetail = planDetail ?: cachedActivePlanDetail
            cachedActivePlanSummary = when {
                planDetail != null -> ActivePlanSummary(planDetail.id, planDetail.name)
                else -> ActivePlanSummary(resolvedActivePlan.id, resolvedActivePlan.name)
            }
        }

        if (resolvedActiveDiet != null) {
            cachedActiveDietId = resolvedActiveDiet.id
            cachedActiveDietDetail = dietDetail
        } else if (cachedActiveDietId != null) {
            cachedActiveDietId = null
            cachedActiveDietDetail = null
        }

        cachedWeeklySummary = weeklySummary
        val motivation = deriveMotivation(userAchievements, allDefinitions, streaks)
        val todayTraining = deriveTodayTraining(
            cachedActivePlanDetail,
            weeklySummary,
            freshWorkoutLogs,
        )
        val nextMeal = if (resolvedActiveDiet == null && cachedActiveDietId == null) {
            NextMealState.NoPlan
        } else {
            deriveNextMeal(cachedActiveDietDetail)
        }
        val isTrainingHydrating = resolvedActivePlan != null && cachedActivePlanDetail == null
        val todayNutrition = deriveNutrition(nutritionPair.first, nutritionPair.second)

        Log.d(
            "AIFIT_DEBUG",
            "[VM][silentRefresh] todayTraining=${todayTraining?.planId} nextMeal=${nextMeal::class.simpleName}",
        )

        _uiState.update { cur ->
            if (cur is HomeUiState.Success) {
                cur.copy(
                    userName = profile?.name ?: cur.userName,
                    avatarUrl = profile?.profilePictureUrl ?: cur.avatarUrl,
                    activePlan = cachedActivePlanSummary,
                    todayTraining = todayTraining,
                    nextMeal = nextMeal,
                    todayNutrition = todayNutrition,
                    weeklySummary = weeklySummary,
                    streaks = streaks,
                    lastAchievement = motivation.lastAchievement,
                    nextAchievement = motivation.nextAchievement,
                    trainingStreakDays = motivation.trainingStreakDays,
                    isTrainingHydrating = isTrainingHydrating,
                )
            } else cur
        }
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
        val today = LocalDate.now()
        val logDeferred = async {
            when (val r = getNutritionLogUseCase(today).first { it !is Result.Loading }) {
                is Result.Success -> r.data
                else -> null
            }
        }
        val targetDeferred = async {
            when (val r = getCurrentNutritionTargetUseCase().first { it !is Result.Loading }) {
                is Result.Success -> r.data
                else -> null
            }
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

    /** Cache-first: first [Result.Success] (Room before network); does not wait for network. */
    private suspend fun firstSuccessWorkoutHistory(): List<WorkoutLog> {
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

        val isCompleted = todayWorkoutLogs.any {
            it.trainingPlanId == activePlan.id &&
                it.trainingDayId == todayTrainingDay.id &&
                it.isLocked
        }
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

    private suspend fun resolveTodayPlanMeals(): List<Meal> {
        val plan = cachedActiveDietDetail
            ?: cachedActiveDietId?.let { planId ->
                loadDietPlanDetail(planId)?.also { cachedActiveDietDetail = it }
            }
        return plan?.mealsForToday() ?: emptyList()
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

        private const val PROFILE_NETWORK_TIMEOUT_MS = 15_000L
        private const val RESUME_DEBOUNCE_MS = 300L

        /**
         * Saludo según la hora local del dispositivo.
         *
         * @return `"Buenos días"`, `"Buenas tardes"` o `"Buenas noches"`.
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

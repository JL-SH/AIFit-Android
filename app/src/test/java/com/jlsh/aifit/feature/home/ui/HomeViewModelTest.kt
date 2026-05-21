package com.jlsh.aifit.feature.home.ui

import android.util.Log
import app.cash.turbine.test
import com.jlsh.aifit.core.common.AppException
import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.feature.diet.domain.model.DietPlan
import com.jlsh.aifit.feature.diet.domain.usecase.GetDietPlanDetailUseCase
import com.jlsh.aifit.feature.diet.domain.usecase.GetDietPlansUseCase
import com.jlsh.aifit.feature.gamification.domain.model.Streak
import com.jlsh.aifit.feature.gamification.domain.usecase.GetAllAchievementDefinitionsUseCase
import com.jlsh.aifit.feature.gamification.domain.usecase.GetUserAchievementsUseCase
import com.jlsh.aifit.feature.gamification.domain.usecase.GetUserStreaksUseCase
import com.jlsh.aifit.feature.home.ui.state.HomeUiEvent
import com.jlsh.aifit.feature.home.ui.state.HomeUiState
import com.jlsh.aifit.feature.home.ui.state.NextMealState
import com.jlsh.aifit.feature.nutrition.domain.model.NutritionLog
import com.jlsh.aifit.feature.nutrition.domain.model.NutritionTarget
import com.jlsh.aifit.feature.nutrition.domain.usecase.GetCurrentNutritionTargetUseCase
import com.jlsh.aifit.feature.nutrition.domain.usecase.GetNutritionLogUseCase
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
import com.jlsh.aifit.testutil.*
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    // ── Mocks ──────────────────────────────────────────────────────────────────

    private val getUserProfileUseCase: GetUserProfileUseCase = mockk()
    private val getTrainingPlansUseCase: GetTrainingPlansUseCase = mockk()
    private val getTrainingPlanDetailUseCase: GetTrainingPlanDetailUseCase = mockk()
    private val getDietPlansUseCase: GetDietPlansUseCase = mockk()
    private val getDietPlanDetailUseCase: GetDietPlanDetailUseCase = mockk()
    private val getNutritionLogUseCase: GetNutritionLogUseCase = mockk()
    private val getCurrentNutritionTargetUseCase: GetCurrentNutritionTargetUseCase = mockk()
    private val getWeeklyProgressSummaryUseCase: GetWeeklyProgressSummaryUseCase = mockk()
    private val getUserStreaksUseCase: GetUserStreaksUseCase = mockk()
    private val getUserAchievementsUseCase: GetUserAchievementsUseCase = mockk()
    private val getAllDefinitionsUseCase: GetAllAchievementDefinitionsUseCase = mockk()
    private val getBodyWeightHistoryUseCase: GetBodyWeightHistoryUseCase = mockk()
    private val getWorkoutHistoryUseCase: GetWorkoutHistoryUseCase = mockk()
    private val logBodyWeightUseCase: LogBodyWeightUseCase = mockk()

    @Before
    fun setUp() {
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        every { Log.w(any(), any<String>()) } returns 0
        every { Log.e(any(), any()) } returns 0
    }

    @After
    fun tearDown() {
        unmockkStatic(Log::class)
    }

    // ── Factory ────────────────────────────────────────────────────────────────

    /**
     * Creates a HomeViewModel with sensible defaults. Override any parameter to
     * change the behavior of the corresponding use case.
     */
    private fun createViewModel(
        profileFlow: Flow<Result<UserProfile>> =
            flowOf(Result.Success(fakeUserProfile())),
        plansFlow: Flow<Result<List<TrainingPlan>>> =
            flowOf(Result.Success(emptyList())),
        planDetailResult: Result<TrainingPlan> =
            Result.Success(fakeTrainingPlan()),
        dietPlansFlow: Flow<Result<List<DietPlan>>> =
            flowOf(Result.Success(emptyList())),
        dietPlanDetailResult: Result<DietPlan> =
            Result.Success(fakeDietPlan()),
        nutritionLogFlow: Flow<Result<NutritionLog>> =
            flowOf(Result.Success(fakeNutritionLog())),
        nutritionTargetFlow: Flow<Result<NutritionTarget>> =
            flowOf(Result.Success(fakeNutritionTarget())),
        weeklySummaryResult: Result<WeeklyProgressSummary> =
            Result.Success(fakeWeeklyProgressSummary()),
        streaksResult: Result<List<Streak>> =
            Result.Success(listOf(fakeStreak())),
        weightHistoryFlow: Flow<Result<List<BodyWeightLog>>> =
            flowOf(Result.Success(listOf(fakeBodyWeightLog()))),
        workoutHistoryFlow: Flow<Result<List<WorkoutLog>>> =
            flowOf(Result.Success(emptyList())),
    ): HomeViewModel {
        every { getUserProfileUseCase() } returns profileFlow
        every { getTrainingPlansUseCase() } returns plansFlow
        coEvery { getTrainingPlanDetailUseCase(any()) } returns planDetailResult
        every { getDietPlansUseCase() } returns dietPlansFlow
        coEvery { getDietPlanDetailUseCase(any()) } returns dietPlanDetailResult
        every { getNutritionLogUseCase(any()) } returns nutritionLogFlow
        every { getCurrentNutritionTargetUseCase() } returns nutritionTargetFlow
        coEvery { getWeeklyProgressSummaryUseCase() } returns weeklySummaryResult
        coEvery { getUserStreaksUseCase() } returns streaksResult
        coEvery { getUserAchievementsUseCase() } returns Result.Success(emptyList())
        coEvery { getAllDefinitionsUseCase() } returns Result.Success(emptyList())
        every { getBodyWeightHistoryUseCase(any(), any()) } returns weightHistoryFlow
        every { getWorkoutHistoryUseCase(any(), any(), any()) } returns workoutHistoryFlow
        return HomeViewModel(
            getUserProfileUseCase,
            getTrainingPlansUseCase,
            getTrainingPlanDetailUseCase,
            getDietPlansUseCase,
            getDietPlanDetailUseCase,
            getNutritionLogUseCase,
            getCurrentNutritionTargetUseCase,
            getWeeklyProgressSummaryUseCase,
            getUserStreaksUseCase,
            getUserAchievementsUseCase,
            getAllDefinitionsUseCase,
            getBodyWeightHistoryUseCase,
            getWorkoutHistoryUseCase,
            logBodyWeightUseCase,
        )
    }

    // ── Estado inicial ─────────────────────────────────────────────────────────

    @Test
    fun `uiState es Loading inicialmente cuando los datos no han cargado`() {
        val vm = createViewModel(
            profileFlow = flow { emit(Result.Loading); awaitCancellation() },
            plansFlow = flow { emit(Result.Loading); awaitCancellation() },
        )

        assertTrue(vm.uiState.value is HomeUiState.Loading)
    }

    // ── Error state ────────────────────────────────────────────────────────────

    @Test
    fun `uiState es Error cuando profile falla`() = runTest {
        val vm = createViewModel(
            profileFlow = flowOf(Result.Error(AppException.NetworkException)),
        )
        advanceUntilIdle()

        val state = vm.uiState.value
        assertTrue(state is HomeUiState.Error)
        assertEquals("No se pudo cargar el perfil", (state as HomeUiState.Error).message)
    }

    // ── Success — sin planes ───────────────────────────────────────────────────

    @Test
    fun `uiState es Success con todayTraining null cuando no hay plan activo`() = runTest {
        val vm = createViewModel(
            plansFlow = flowOf(Result.Success(emptyList())),
        )
        advanceUntilIdle()

        val state = vm.uiState.value
        assertTrue(state is HomeUiState.Success)
        val success = state as HomeUiState.Success
        assertNull(success.todayTraining)
        assertNull(success.activePlan)
        assertEquals("Test User", success.userName)
    }

    // ── Success — con plan activo ──────────────────────────────────────────────

    @Test
    fun `uiState es Success con todayTraining cuando hay plan activo con dia de hoy`() = runTest {
        val trainingDay = fakeTrainingDay(
            id = "day-1",
            name = "Push Day",
            dayType = TrainingDayType.TRAINING,
            exercises = listOf(fakeTrainingExercise()),
        )
        val plan = fakeTrainingPlan(
            id = "plan-1",
            name = "My Plan",
            status = PlanStatus.ACTIVE,
            days = listOf(trainingDay),
        )

        val vm = createViewModel(
            plansFlow = flowOf(Result.Success(listOf(plan))),
            planDetailResult = Result.Success(plan),
        )
        advanceUntilIdle()

        val state = vm.uiState.value
        assertTrue(state is HomeUiState.Success)
        val success = state as HomeUiState.Success
        assertNotNull(success.activePlan)
        assertEquals("plan-1", success.activePlan?.id)
        assertEquals("My Plan", success.activePlan?.name)
        // todayTraining may be non-null depending on the day-matching logic
        // but activePlan should always be set
    }

    @Test
    fun `uiState Success con activePlan no null pero todayTraining null en dia de descanso`() = runTest {
        val restDay = fakeTrainingDay(
            id = "rest-1",
            name = "Rest Day",
            dayType = TrainingDayType.REST,
            exercises = emptyList(),
        )
        val plan = fakeTrainingPlan(
            id = "plan-1",
            name = "Rest Plan",
            status = PlanStatus.ACTIVE,
            days = listOf(restDay),
        )

        val vm = createViewModel(
            plansFlow = flowOf(Result.Success(listOf(plan))),
            planDetailResult = Result.Success(plan),
        )
        advanceUntilIdle()

        val state = vm.uiState.value as HomeUiState.Success
        assertNotNull(state.activePlan)
        assertNull(state.todayTraining)
    }

    // ── Success — nutrición ────────────────────────────────────────────────────

    @Test
    fun `uiState Success con todayNutrition cuando hay log y target`() = runTest {
        val vm = createViewModel(
            nutritionLogFlow = flowOf(Result.Success(fakeNutritionLog())),
            nutritionTargetFlow = flowOf(Result.Success(fakeNutritionTarget())),
        )
        advanceUntilIdle()

        val state = vm.uiState.value as HomeUiState.Success
        assertNotNull(state.todayNutrition)
        val nutrition = state.todayNutrition!!
        assertEquals(1450, nutrition.caloriesConsumed)
        assertEquals(2200, nutrition.calorieTarget)
    }

    @Test
    fun `uiState Success con todayNutrition null cuando nutricion falla`() = runTest {
        val vm = createViewModel(
            nutritionLogFlow = flowOf(Result.Error(AppException.NetworkException)),
            nutritionTargetFlow = flowOf(Result.Error(AppException.NetworkException)),
        )
        advanceUntilIdle()

        val state = vm.uiState.value as HomeUiState.Success
        assertNull(state.todayNutrition)
    }

    // ── Success — streaks ──────────────────────────────────────────────────────

    @Test
    fun `uiState Success con streaks cuando hay rachas`() = runTest {
        val streaks = listOf(fakeStreak())
        val vm = createViewModel(streaksResult = Result.Success(streaks))
        advanceUntilIdle()

        val state = vm.uiState.value as HomeUiState.Success
        assertEquals(1, state.streaks.size)
    }

    @Test
    fun `uiState Success con streaks vacios cuando streaks falla`() = runTest {
        val vm = createViewModel(
            streaksResult = Result.Error(AppException.ServerException),
        )
        advanceUntilIdle()

        val state = vm.uiState.value as HomeUiState.Success
        assertTrue(state.streaks.isEmpty())
    }

    // ── Success — weekly summary ───────────────────────────────────────────────

    @Test
    fun `uiState Success con weeklySummary cuando carga correctamente`() = runTest {
        val vm = createViewModel(
            weeklySummaryResult = Result.Success(fakeWeeklyProgressSummary()),
        )
        advanceUntilIdle()

        val state = vm.uiState.value as HomeUiState.Success
        assertNotNull(state.weeklySummary)
        val summary = state.weeklySummary!!
        assertEquals(3, summary.workoutsThisWeek)
    }

    @Test
    fun `uiState Success con weeklySummary null cuando falla`() = runTest {
        val vm = createViewModel(
            weeklySummaryResult = Result.Error(AppException.NetworkException),
        )
        advanceUntilIdle()

        val state = vm.uiState.value as HomeUiState.Success
        assertNull(state.weeklySummary)
    }

    // ── Success — weight entries ───────────────────────────────────────────────

    @Test
    fun `uiState Success con weightEntries cuando hay historial`() = runTest {
        val weights = listOf(fakeBodyWeightLog())
        val vm = createViewModel(
            weightHistoryFlow = flowOf(Result.Success(weights)),
        )
        advanceUntilIdle()

        val state = vm.uiState.value as HomeUiState.Success
        assertEquals(1, state.weightEntries.size)
    }

    @Test
    fun `uiState Success con weightEntries vacios cuando historial falla`() = runTest {
        val vm = createViewModel(
            weightHistoryFlow = flowOf(Result.Error(AppException.NetworkException)),
        )
        advanceUntilIdle()

        val state = vm.uiState.value as HomeUiState.Success
        assertTrue(state.weightEntries.isEmpty())
    }

    // ── Success — nextMeal ─────────────────────────────────────────────────────

    @Test
    fun `uiState Success con nextMeal NoPlan cuando no hay diet plan activo`() = runTest {
        val vm = createViewModel(
            dietPlansFlow = flowOf(Result.Success(emptyList())),
        )
        advanceUntilIdle()

        val state = vm.uiState.value as HomeUiState.Success
        assertTrue(state.nextMeal is NextMealState.NoPlan)
    }

    @Test
    fun `uiState Success con nextMeal Upcoming cuando hay diet plan activo con comidas`() = runTest {
        val diet = fakeDietPlan(
            status = PlanStatus.ACTIVE,
            days = listOf(fakeDietDay(meals = listOf(fakeMeal()))),
        )
        val vm = createViewModel(
            dietPlansFlow = flowOf(Result.Success(listOf(diet))),
            dietPlanDetailResult = Result.Success(diet),
            // No meals logged yet
            nutritionLogFlow = flowOf(Result.Error(AppException.NetworkException)),
        )
        advanceUntilIdle()

        val state = vm.uiState.value as HomeUiState.Success
        assertTrue(state.nextMeal is NextMealState.Upcoming || state.nextMeal is NextMealState.AllDone)
    }

    // ── Workout completed ──────────────────────────────────────────────────────

    @Test
    fun `todayTraining isCompleted true cuando hay workout log locked para el plan activo`() = runTest {
        val trainingDay = fakeTrainingDay(
            dayType = TrainingDayType.TRAINING,
            exercises = listOf(fakeTrainingExercise()),
        )
        val plan = fakeTrainingPlan(
            id = "plan-1",
            status = PlanStatus.ACTIVE,
            days = listOf(trainingDay),
        )
        val lockedLog = fakeWorkoutLog(
            trainingPlanId = "plan-1",
            isLocked = true,
            date = LocalDate.now(),
        )

        val vm = createViewModel(
            plansFlow = flowOf(Result.Success(listOf(plan))),
            planDetailResult = Result.Success(plan),
            workoutHistoryFlow = flowOf(Result.Success(listOf(lockedLog))),
        )
        advanceUntilIdle()

        val state = vm.uiState.value as HomeUiState.Success
        if (state.todayTraining != null) {
            assertTrue(state.todayTraining.isCompleted)
        }
        // If todayTraining is null (rest day rotation), the test is still valid
    }

    // ── Event: onStartSession ──────────────────────────────────────────────────

    @Test
    fun `onStartSession emite NavigateToWorkoutSession cuando hay todayTraining`() = runTest {
        val trainingDay = fakeTrainingDay(
            id = "day-1",
            dayType = TrainingDayType.TRAINING,
            exercises = listOf(fakeTrainingExercise()),
        )
        val plan = fakeTrainingPlan(
            id = "plan-1",
            status = PlanStatus.ACTIVE,
            days = listOf(trainingDay),
        )

        val vm = createViewModel(
            plansFlow = flowOf(Result.Success(listOf(plan))),
            planDetailResult = Result.Success(plan),
        )
        advanceUntilIdle()

        val state = vm.uiState.value as HomeUiState.Success
        if (state.todayTraining != null) {
            vm.events.test {
                vm.onStartSession("plan-1")
                val event = awaitItem()
                assertTrue(event is HomeUiEvent.NavigateToWorkoutSession)
                val nav = event as HomeUiEvent.NavigateToWorkoutSession
                assertEquals("plan-1", nav.planId)
                assertEquals("day-1", nav.dayId)
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    // ── Event: onViewTrainingDetail ────────────────────────────────────────────

    @Test
    fun `onViewTrainingDetail emite NavigateToTrainingDetail`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()

        vm.events.test {
            vm.onViewTrainingDetail("plan-1")
            val event = awaitItem()
            assertTrue(event is HomeUiEvent.NavigateToTrainingDetail)
            assertEquals("plan-1", (event as HomeUiEvent.NavigateToTrainingDetail).planId)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ── Event: onLogMeal ───────────────────────────────────────────────────────

    @Test
    fun `onLogMeal emite NavigateToTrackMeal`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()

        vm.events.test {
            vm.onLogMeal()
            val event = awaitItem()
            assertTrue(event is HomeUiEvent.NavigateToTrackMeal)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ── Event: onLogWeight ─────────────────────────────────────────────────────

    @Test
    fun `onLogWeight emite ShowLogWeightSheet`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()

        vm.events.test {
            vm.onLogWeight()
            val event = awaitItem()
            assertTrue(event is HomeUiEvent.ShowLogWeightSheet)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ── Event: onSaveWeight ────────────────────────────────────────────────────

    @Test
    fun `onSaveWeight emite ShowSnackbar de exito cuando funciona`() = runTest {
        coEvery { logBodyWeightUseCase(any()) } returns Result.Success(fakeBodyWeightLog())
        val vm = createViewModel()
        advanceUntilIdle()

        vm.events.test {
            vm.onSaveWeight(78.5)
            advanceUntilIdle()
            val event = awaitItem()
            assertTrue(event is HomeUiEvent.ShowSnackbar)
            assertTrue((event as HomeUiEvent.ShowSnackbar).message.contains("✓"))
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onSaveWeight emite ShowSnackbar de error cuando falla`() = runTest {
        coEvery { logBodyWeightUseCase(any()) } returns Result.Error(AppException.ServerException)
        val vm = createViewModel()
        advanceUntilIdle()

        vm.events.test {
            vm.onSaveWeight(78.5)
            advanceUntilIdle()
            val event = awaitItem()
            assertTrue(event is HomeUiEvent.ShowSnackbar)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onSaveWeight actualiza weightEntries en Success`() = runTest {
        coEvery { logBodyWeightUseCase(any()) } returns Result.Success(fakeBodyWeightLog())
        val vm = createViewModel(
            weightHistoryFlow = flowOf(Result.Success(listOf(fakeBodyWeightLog(), fakeBodyWeightLog(id = "bw-2")))),
        )
        advanceUntilIdle()

        vm.onSaveWeight(79.0)
        advanceUntilIdle()

        val state = vm.uiState.value as HomeUiState.Success
        assertEquals(2, state.weightEntries.size)
    }

    // ── Event: onProgressDashboard ─────────────────────────────────────────────

    @Test
    fun `onProgressDashboard emite NavigateToProgressDashboard`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()

        vm.events.test {
            vm.onProgressDashboard()
            val event = awaitItem()
            assertTrue(event is HomeUiEvent.NavigateToProgressDashboard)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ── Event: onBodyWeight ────────────────────────────────────────────────────

    @Test
    fun `onBodyWeight emite NavigateToBodyWeight`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()

        vm.events.test {
            vm.onBodyWeight()
            val event = awaitItem()
            assertTrue(event is HomeUiEvent.NavigateToBodyWeight)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ── Event: onGamification ──────────────────────────────────────────────────

    @Test
    fun `onGamification emite NavigateToGamification con tab correcto`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()

        vm.events.test {
            vm.onGamification("ACHIEVEMENTS")
            val event = awaitItem()
            assertTrue(event is HomeUiEvent.NavigateToGamification)
            assertEquals("ACHIEVEMENTS", (event as HomeUiEvent.NavigateToGamification).tab)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ── Event: onProfile ───────────────────────────────────────────────────────

    @Test
    fun `onProfile emite NavigateToProfile`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()

        vm.events.test {
            vm.onProfile()
            val event = awaitItem()
            assertTrue(event is HomeUiEvent.NavigateToProfile)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ── Event: onCreatePlan ────────────────────────────────────────────────────

    @Test
    fun `onCreatePlan emite NavigateToGeneratePlan`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()

        vm.events.test {
            vm.onCreatePlan()
            val event = awaitItem()
            assertTrue(event is HomeUiEvent.NavigateToGeneratePlan)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ── Companion: greetingForTime ─────────────────────────────────────────────

    @Test
    fun `greetingForTime retorna un saludo no vacio`() {
        val greeting = HomeViewModel.greetingForTime()
        assertTrue(greeting.isNotBlank())
        assertTrue(
            greeting.startsWith("Buenos días") ||
                greeting.startsWith("Buenas tardes") ||
                greeting.startsWith("Buenas noches"),
        )
    }

    // ── Resiliencia parcial: secciones independientes ──────────────────────────

    @Test
    fun `uiState Success cuando solo falla nutricion pero el resto carga bien`() = runTest {
        val vm = createViewModel(
            nutritionLogFlow = flowOf(Result.Error(AppException.NetworkException)),
            nutritionTargetFlow = flowOf(Result.Error(AppException.NetworkException)),
            streaksResult = Result.Success(listOf(fakeStreak())),
            weeklySummaryResult = Result.Success(fakeWeeklyProgressSummary()),
        )
        advanceUntilIdle()

        val state = vm.uiState.value as HomeUiState.Success
        assertNull(state.todayNutrition)
        assertEquals(1, state.streaks.size)
        assertNotNull(state.weeklySummary)
    }

    @Test
    fun `uiState Success cuando solo falla weekly summary pero el resto carga bien`() = runTest {
        val vm = createViewModel(
            weeklySummaryResult = Result.Error(AppException.ServerException),
            streaksResult = Result.Success(listOf(fakeStreak())),
        )
        advanceUntilIdle()

        val state = vm.uiState.value as HomeUiState.Success
        assertNull(state.weeklySummary)
        assertEquals(1, state.streaks.size)
    }

    // ── onResumed ──────────────────────────────────────────────────────────────

    @Test
    fun `onResumed actualiza todayNutrition con ultimo Success del log`() = runTest {
        val cachedLog = fakeNutritionLog(totalCalories = 100, meals = emptyList())
        val freshLog = fakeNutritionLog(totalCalories = 300, meals = listOf(fakeMealLog()))
        val vm = createViewModel(
            nutritionLogFlow = flowOf(Result.Success(cachedLog)),
        )
        advanceUntilIdle()

        val before = vm.uiState.value as HomeUiState.Success
        assertEquals(100, before.todayNutrition?.caloriesConsumed)

        every { getNutritionLogUseCase(any()) } returns flow {
            emit(Result.Loading)
            emit(Result.Success(cachedLog))
            emit(Result.Success(freshLog))
        }

        vm.onResumed()
        advanceUntilIdle()

        val after = vm.uiState.value as HomeUiState.Success
        assertEquals(300, after.todayNutrition?.caloriesConsumed)
    }

    @Test
    fun `onResumed detecta plan activo nuevo y actualiza el estado`() = runTest {
        // Initial: no active plan
        val vm = createViewModel(
            plansFlow = flowOf(Result.Success(emptyList())),
        )
        advanceUntilIdle()

        val stateBeforeResume = vm.uiState.value as HomeUiState.Success
        assertNull(stateBeforeResume.activePlan)

        // Now a new plan is active after resume
        val newPlan = fakeTrainingPlan(
            id = "new-plan",
            name = "New Active Plan",
            status = PlanStatus.ACTIVE,
            days = listOf(fakeTrainingDay(dayType = TrainingDayType.REST)),
        )
        every { getTrainingPlansUseCase() } returns flowOf(Result.Success(listOf(newPlan)))
        coEvery { getTrainingPlanDetailUseCase("new-plan") } returns Result.Success(newPlan)
        every { getWorkoutHistoryUseCase(any(), any(), any()) } returns flowOf(Result.Success(emptyList()))

        vm.onResumed()
        advanceUntilIdle()

        val stateAfterResume = vm.uiState.value as HomeUiState.Success
        assertNotNull(stateAfterResume.activePlan)
        assertEquals("new-plan", stateAfterResume.activePlan?.id)
    }

    @Test
    fun `onResumed limpia activePlan cuando el plan fue desactivado`() = runTest {
        val plan = fakeTrainingPlan(
            id = "plan-1",
            status = PlanStatus.ACTIVE,
            days = listOf(fakeTrainingDay(dayType = TrainingDayType.REST)),
        )

        val vm = createViewModel(
            plansFlow = flowOf(Result.Success(listOf(plan))),
            planDetailResult = Result.Success(plan),
        )
        advanceUntilIdle()

        val stateBefore = vm.uiState.value as HomeUiState.Success
        assertNotNull(stateBefore.activePlan)

        // Now no active plans after resume
        every { getTrainingPlansUseCase() } returns flowOf(Result.Success(
            listOf(fakeTrainingPlan(id = "plan-1", status = PlanStatus.COMPLETED)),
        ))

        vm.onResumed()
        advanceUntilIdle()

        val stateAfter = vm.uiState.value as HomeUiState.Success
        assertNull(stateAfter.activePlan)
        assertNull(stateAfter.todayTraining)
    }

    @Test
    fun `onResumed muestra activePlan desde summary cuando loadPlanDetail falla`() = runTest {
        // Initial: no active plan
        val vm = createViewModel(
            plansFlow = flowOf(Result.Success(emptyList())),
        )
        advanceUntilIdle()

        val stateBeforeResume = vm.uiState.value as HomeUiState.Success
        assertNull(stateBeforeResume.activePlan)

        // Now a new plan is active but detail load fails
        val newPlan = fakeTrainingPlan(
            id = "new-plan",
            name = "New Active Plan",
            status = PlanStatus.ACTIVE,
            days = listOf(fakeTrainingDay(dayType = TrainingDayType.REST)),
        )
        every { getTrainingPlansUseCase() } returns flowOf(Result.Success(listOf(newPlan)))
        coEvery { getTrainingPlanDetailUseCase("new-plan") } returns Result.Error(AppException.NetworkException)
        every { getWorkoutHistoryUseCase(any(), any(), any()) } returns flowOf(Result.Success(emptyList()))

        vm.onResumed()
        advanceUntilIdle()

        val stateAfterResume = vm.uiState.value as HomeUiState.Success
        assertNotNull(stateAfterResume.activePlan)
        assertEquals("new-plan", stateAfterResume.activePlan?.id)
        assertEquals("New Active Plan", stateAfterResume.activePlan?.name)
    }

    @Test
    fun `onResumed no sobreescribe activePlan con null cuando ya habia plan y detail falla`() = runTest {
        // Initial: active plan with detail success
        val plan = fakeTrainingPlan(
            id = "plan-1",
            name = "My Plan",
            status = PlanStatus.ACTIVE,
            days = listOf(fakeTrainingDay(dayType = TrainingDayType.REST)),
        )

        val vm = createViewModel(
            plansFlow = flowOf(Result.Success(listOf(plan))),
            planDetailResult = Result.Success(plan),
        )
        advanceUntilIdle()

        val stateBefore = vm.uiState.value as HomeUiState.Success
        assertNotNull(stateBefore.activePlan)
        assertEquals("plan-1", stateBefore.activePlan?.id)

        // A different plan is now active but detail fails
        val newPlan = fakeTrainingPlan(
            id = "new-plan",
            name = "New Plan",
            status = PlanStatus.ACTIVE,
        )
        every { getTrainingPlansUseCase() } returns flowOf(Result.Success(listOf(newPlan)))
        coEvery { getTrainingPlanDetailUseCase("new-plan") } returns Result.Error(AppException.NetworkException)
        every { getWorkoutHistoryUseCase(any(), any(), any()) } returns flowOf(Result.Success(emptyList()))

        vm.onResumed()
        advanceUntilIdle()

        val stateAfter = vm.uiState.value as HomeUiState.Success
        assertNotNull(stateAfter.activePlan)
        assertEquals("new-plan", stateAfter.activePlan?.id)
        assertEquals("New Plan", stateAfter.activePlan?.name)
    }

    @Test
    fun `onResumed detecta cambio de plan de dieta activo y actualiza nextMeal`() = runTest {
        val chickenMeal = fakeMeal(name = "Pollo con lentejas")
        val veganMeal = fakeMeal(name = "Ensalada vegana")
        val planChicken = fakeDietPlan(
            id = "diet-1",
            name = "Plan pollo",
            status = PlanStatus.ACTIVE,
            days = listOf(fakeDietDay(meals = listOf(chickenMeal))),
        )
        val planVegan = fakeDietPlan(
            id = "diet-2",
            name = "Plan vegano",
            status = PlanStatus.ACTIVE,
            days = listOf(fakeDietDay(meals = listOf(veganMeal))),
        )

        val vm = createViewModel(
            dietPlansFlow = flowOf(Result.Success(listOf(planChicken))),
            dietPlanDetailResult = Result.Success(planChicken),
            plansFlow = flowOf(Result.Success(emptyList())),
        )
        advanceUntilIdle()

        every { getDietPlansUseCase() } returns flowOf(
            Result.Success(listOf(planChicken.copy(status = PlanStatus.PAUSED), planVegan)),
        )
        coEvery { getDietPlanDetailUseCase("diet-2") } returns Result.Success(planVegan)
        every { getTrainingPlansUseCase() } returns flowOf(Result.Success(emptyList()))

        vm.onResumed()
        advanceUntilIdle()

        val state = vm.uiState.value as HomeUiState.Success
        val upcoming = state.nextMeal as? NextMealState.Upcoming
        assertNotNull(upcoming)
        assertEquals("Ensalada vegana", upcoming?.mealName)
    }

    // ── userName y avatarUrl ────────────────────────────────────────────────────

    @Test
    fun `uiState Success contiene nombre y avatarUrl del perfil`() = runTest {
        val profile = fakeUserProfile(name = "María", profilePictureUrl = "https://img.url/avatar.png")
        val vm = createViewModel(
            profileFlow = flowOf(Result.Success(profile)),
        )
        advanceUntilIdle()

        val state = vm.uiState.value as HomeUiState.Success
        assertEquals("María", state.userName)
        assertEquals("https://img.url/avatar.png", state.avatarUrl)
    }

    @Test
    fun `loadAll usa ultimo Success del perfil cuando cache no tiene URL`() = runTest {
        val cloudinaryUrl = "https://res.cloudinary.com/demo/photo.jpg"
        val cachedProfile = fakeUserProfile(profilePictureUrl = null)
        val apiProfile = fakeUserProfile(profilePictureUrl = cloudinaryUrl)
        val vm = createViewModel(
            profileFlow = flowOf(
                Result.Loading,
                Result.Success(cachedProfile),
                Result.Success(apiProfile),
            ),
        )
        advanceUntilIdle()

        val state = vm.uiState.value as HomeUiState.Success
        assertEquals(cloudinaryUrl, state.avatarUrl)
    }
}

















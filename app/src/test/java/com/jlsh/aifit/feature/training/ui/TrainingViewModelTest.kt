package com.jlsh.aifit.feature.training.ui

import android.util.Log
import app.cash.turbine.test
import com.jlsh.aifit.core.common.AppException
import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.feature.training.domain.model.PlanStatus
import com.jlsh.aifit.feature.training.domain.model.TrainingDayType
import com.jlsh.aifit.feature.training.domain.usecase.DeleteTrainingPlanUseCase
import com.jlsh.aifit.feature.training.domain.usecase.GenerateTrainingPlanUseCase
import com.jlsh.aifit.feature.training.domain.usecase.GetTrainingPlanDetailUseCase
import com.jlsh.aifit.feature.training.domain.usecase.GetTrainingPlansUseCase
import com.jlsh.aifit.feature.training.domain.usecase.SetActivePlanUseCase
import com.jlsh.aifit.feature.training.ui.state.GeneratePlanUiState
import com.jlsh.aifit.feature.training.ui.state.TrainingDetailUiState
import com.jlsh.aifit.feature.training.ui.state.TrainingHubUiState
import com.jlsh.aifit.feature.training.ui.state.TrainingUiEvent
import com.jlsh.aifit.feature.training.ui.state.TrainingUiState
import com.jlsh.aifit.feature.user.domain.usecase.GetUserProfileUseCase
import com.jlsh.aifit.testutil.*
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TrainingViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val getTrainingPlansUseCase: GetTrainingPlansUseCase = mockk()
    private val getTrainingPlanDetailUseCase: GetTrainingPlanDetailUseCase = mockk()
    private val generateTrainingPlanUseCase: GenerateTrainingPlanUseCase = mockk()
    private val deleteTrainingPlanUseCase: DeleteTrainingPlanUseCase = mockk()
    private val setActivePlanUseCase: SetActivePlanUseCase = mockk()
    private val getUserProfileUseCase: GetUserProfileUseCase = mockk()

    @Before
    fun setUp() {
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        every { Log.e(any(), any()) } returns 0
    }

    @After
    fun tearDown() {
        unmockkStatic(Log::class)
    }

    private fun createViewModel(
        plansFlow: Flow<Result<List<com.jlsh.aifit.feature.training.domain.model.TrainingPlan>>> =
            flowOf(Result.Success(emptyList())),
        profileFlow: Flow<Result<com.jlsh.aifit.feature.user.domain.model.UserProfile>> =
            flowOf(Result.Success(fakeUserProfile())),
    ): TrainingViewModel {
        every { getTrainingPlansUseCase() } returns plansFlow
        every { getUserProfileUseCase() } returns profileFlow
        return TrainingViewModel(
            getTrainingPlansUseCase,
            getTrainingPlanDetailUseCase,
            generateTrainingPlanUseCase,
            deleteTrainingPlanUseCase,
            setActivePlanUseCase,
            getUserProfileUseCase,
        )
    }

    // ─── Estado inicial ────────────────────────────────────────────────────────

    @Test
    fun `uiState es Success con lista vacía cuando no hay planes`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()

        val state = vm.uiState.value
        assertTrue(state is TrainingUiState.Success)
        assertTrue((state as TrainingUiState.Success).plans.isEmpty())
        assertNull(state.activePlan)
    }

    @Test
    fun `hubUiState es NoActivePlan cuando no hay planes`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()

        val state = vm.hubUiState.value
        assertTrue(state is TrainingHubUiState.NoActivePlan)
    }

    // ─── Con planes ────────────────────────────────────────────────────────────

    @Test
    fun `uiState es Success con plan activo cuando hay plan ACTIVE`() = runTest {
        val plans = listOf(
            fakeTrainingPlan(id = "p-1", status = PlanStatus.ACTIVE),
            fakeTrainingPlan(id = "p-2", status = PlanStatus.COMPLETED),
        )
        val vm = createViewModel(plansFlow = flowOf(Result.Success(plans)))
        advanceUntilIdle()

        val state = vm.uiState.value as TrainingUiState.Success
        assertNotNull(state.activePlan)
        assertEquals("p-1", state.activePlan!!.id)
        assertEquals(2, state.plans.size)
    }

    @Test
    fun `hubUiState es ActivePlan cuando hay plan activo`() = runTest {
        val plans = listOf(fakeTrainingPlan(id = "p-1", status = PlanStatus.ACTIVE))
        val vm = createViewModel(plansFlow = flowOf(Result.Success(plans)))
        advanceUntilIdle()

        val state = vm.hubUiState.value
        assertTrue(state is TrainingHubUiState.ActivePlan)
        assertEquals("p-1", (state as TrainingHubUiState.ActivePlan).plan.id)
    }

    // ─── Error state ───────────────────────────────────────────────────────────

    @Test
    fun `uiState es Error cuando getPlans falla`() = runTest {
        val vm = createViewModel(
            plansFlow = flowOf(Result.Error(AppException.NetworkException))
        )
        advanceUntilIdle()

        assertTrue(vm.uiState.value is TrainingUiState.Error)
    }

    @Test
    fun `hubUiState es Error cuando getPlans falla`() = runTest {
        val vm = createViewModel(
            plansFlow = flowOf(Result.Error(AppException.NetworkException))
        )
        advanceUntilIdle()

        assertTrue(vm.hubUiState.value is TrainingHubUiState.Error)
    }

    // ─── onTabSelected ─────────────────────────────────────────────────────────

    @Test
    fun `onTabSelected actualiza selectedTabIndex`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()

        vm.onTabSelected(1)

        assertEquals(1, vm.selectedTabIndex.value)
    }

    // ─── onPlanClicked ─────────────────────────────────────────────────────────

    @Test
    fun `onPlanClicked emite NavigateToDetail`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()

        vm.events.test {
            vm.onPlanClicked("p-1")
            val event = awaitItem()
            assertTrue(event is TrainingUiEvent.NavigateToDetail)
            assertEquals("p-1", (event as TrainingUiEvent.NavigateToDetail).planId)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ─── loadPlanDetail ────────────────────────────────────────────────────────

    @Test
    fun `loadPlanDetail emite Ready con days cuando tiene éxito`() = runTest {
        val plan = fakeTrainingPlan(
            id = "p-1",
            days = listOf(fakeTrainingDay(dayType = TrainingDayType.TRAINING)),
        )
        coEvery { getTrainingPlanDetailUseCase("p-1") } returns Result.Success(plan)
        val vm = createViewModel()
        advanceUntilIdle()

        vm.loadPlanDetail("p-1")
        advanceUntilIdle()

        val state = vm.detailUiState.value
        assertTrue(state is TrainingDetailUiState.Ready)
        assertEquals("Test Plan", (state as TrainingDetailUiState.Ready).planName)
        assertEquals(1, state.days.size)
    }

    @Test
    fun `loadPlanDetail emite Error cuando falla`() = runTest {
        coEvery { getTrainingPlanDetailUseCase(any()) } returns
            Result.Error(AppException.NotFoundException("Plan"))
        val vm = createViewModel()
        advanceUntilIdle()

        vm.loadPlanDetail("bad-id")
        advanceUntilIdle()

        assertTrue(vm.detailUiState.value is TrainingDetailUiState.Error)
    }

    // ─── onDeletePlan ──────────────────────────────────────────────────────────

    @Test
    fun `onDeletePlan emite PlanDeleted y ShowSnackbar en éxito`() = runTest {
        coEvery { deleteTrainingPlanUseCase("p-1") } returns Result.Success(Unit)
        val vm = createViewModel()
        advanceUntilIdle()

        vm.events.test {
            vm.onDeletePlan("p-1")
            advanceUntilIdle()

            val events = mutableListOf<TrainingUiEvent>()
            // Collect available events (ShowSnackbar + PlanDeleted)
            repeat(2) {
                events.add(awaitItem())
            }
            assertTrue(events.any { it is TrainingUiEvent.ShowSnackbar })
            assertTrue(events.any { it is TrainingUiEvent.PlanDeleted })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onDeletePlan emite ShowSnackbar en error`() = runTest {
        coEvery { deleteTrainingPlanUseCase(any()) } returns
            Result.Error(AppException.ServerException)
        val vm = createViewModel()
        advanceUntilIdle()

        vm.events.test {
            vm.onDeletePlan("p-1")
            advanceUntilIdle()

            val event = awaitItem()
            assertTrue(event is TrainingUiEvent.ShowSnackbar)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ─── onGeneratePlan ────────────────────────────────────────────────────────

    @Test
    fun `onGeneratePlan cambia generateUiState a Loading y luego Success`() = runTest {
        val newPlan = fakeTrainingPlan(id = "gen-1")
        coEvery { generateTrainingPlanUseCase(any()) } returns Result.Success(newPlan)
        val vm = createViewModel()
        advanceUntilIdle()

        vm.onGeneratePlan(fakeGenerateTrainingPlanRequestDto())
        advanceUntilIdle()

        val state = vm.generateUiState.value
        assertTrue(state is GeneratePlanUiState.Success)
        assertEquals("gen-1", (state as GeneratePlanUiState.Success).plan.id)
    }

    @Test
    fun `onGeneratePlan emite NavigateToDetail en éxito`() = runTest {
        val newPlan = fakeTrainingPlan(id = "gen-1")
        coEvery { generateTrainingPlanUseCase(any()) } returns Result.Success(newPlan)
        val vm = createViewModel()
        advanceUntilIdle()

        vm.events.test {
            vm.onGeneratePlan(fakeGenerateTrainingPlanRequestDto())
            advanceUntilIdle()

            val event = awaitItem()
            assertTrue(event is TrainingUiEvent.NavigateToDetail)
            assertEquals("gen-1", (event as TrainingUiEvent.NavigateToDetail).planId)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onGeneratePlan emite ShowSnackbar en error`() = runTest {
        coEvery { generateTrainingPlanUseCase(any()) } returns
            Result.Error(AppException.ServerException)
        val vm = createViewModel()
        advanceUntilIdle()

        vm.events.test {
            vm.onGeneratePlan(fakeGenerateTrainingPlanRequestDto())
            advanceUntilIdle()

            val event = awaitItem()
            assertTrue(event is TrainingUiEvent.ShowSnackbar)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onGeneratePlan cambia generateUiState a Error cuando falla`() = runTest {
        coEvery { generateTrainingPlanUseCase(any()) } returns
            Result.Error(AppException.ServerException)
        val vm = createViewModel()
        advanceUntilIdle()

        vm.onGeneratePlan(fakeGenerateTrainingPlanRequestDto())
        advanceUntilIdle()

        assertTrue(vm.generateUiState.value is GeneratePlanUiState.Error)
    }

    // ─── onNavigateToGenerate ──────────────────────────────────────────────────

    @Test
    fun `onNavigateToGenerate emite NavigateToGenerate`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()

        vm.events.test {
            vm.onNavigateToGenerate(adaptive = true, basePlanId = "p-1")
            val event = awaitItem()
            assertTrue(event is TrainingUiEvent.NavigateToGenerate)
            val nav = event as TrainingUiEvent.NavigateToGenerate
            assertTrue(nav.adaptive)
            assertEquals("p-1", nav.basePlanId)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ─── onStartSession ────────────────────────────────────────────────────────

    @Test
    fun `onStartSession emite NavigateToWorkoutLog`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()

        vm.events.test {
            vm.onStartSession("p-1")
            val event = awaitItem()
            assertTrue(event is TrainingUiEvent.NavigateToWorkoutLog)
            assertEquals("p-1", (event as TrainingUiEvent.NavigateToWorkoutLog).planId)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ─── filterPlans ───────────────────────────────────────────────────────────

    @Test
    fun `filterPlans actualiza selectedFilter en hubUiState`() = runTest {
        val plans = listOf(fakeTrainingPlan(status = PlanStatus.ACTIVE))
        val vm = createViewModel(plansFlow = flowOf(Result.Success(plans)))
        advanceUntilIdle()

        vm.filterPlans(PlanStatus.COMPLETED)

        val state = vm.hubUiState.value
        assertTrue(state is TrainingHubUiState.ActivePlan)
        assertEquals(PlanStatus.COMPLETED, (state as TrainingHubUiState.ActivePlan).selectedFilter)
    }
}


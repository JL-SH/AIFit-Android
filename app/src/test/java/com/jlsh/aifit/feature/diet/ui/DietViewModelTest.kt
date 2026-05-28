package com.jlsh.aifit.feature.diet.ui

import app.cash.turbine.test
import com.jlsh.aifit.core.common.AppException
import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.feature.diet.domain.usecase.DeleteDietPlanUseCase
import com.jlsh.aifit.feature.diet.domain.usecase.GenerateDietPlanUseCase
import com.jlsh.aifit.feature.diet.domain.usecase.GetDietPlanDetailUseCase
import com.jlsh.aifit.feature.diet.domain.usecase.SetActiveDietPlanUseCase
import com.jlsh.aifit.feature.diet.ui.state.DietUiEvent
import com.jlsh.aifit.feature.diet.ui.state.DietUiState
import com.jlsh.aifit.feature.diet.ui.state.GenerateDietUiState
import com.jlsh.aifit.feature.training.domain.model.PlanStatus
import com.jlsh.aifit.feature.user.domain.usecase.GetUserProfileUseCase
import com.jlsh.aifit.testutil.*
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DietViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val getDietPlanDetailUseCase: GetDietPlanDetailUseCase = mockk()
    private val generateDietPlanUseCase: GenerateDietPlanUseCase = mockk()
    private val deleteDietPlanUseCase: DeleteDietPlanUseCase = mockk()
    private val setActiveDietPlanUseCase: SetActiveDietPlanUseCase = mockk()
    private val getUserProfileUseCase: GetUserProfileUseCase = mockk()

    private lateinit var viewModel: DietViewModel

    @Before
    fun setUp() {
        viewModel = DietViewModel(
            getDietPlanDetailUseCase,
            generateDietPlanUseCase,
            deleteDietPlanUseCase,
            setActiveDietPlanUseCase,
            getUserProfileUseCase,
        )
    }

    // ─── Initial state ──────────────────────────── ────────────────────────────

    @Test
    fun `detailUiState inicial es Loading`() {
        assertTrue(viewModel.detailUiState.value is DietUiState.Loading)
    }

    @Test
    fun `generateUiState inicial es Idle`() {
        assertTrue(viewModel.generateUiState.value is GenerateDietUiState.Idle)
    }

    // ─── loadPlanDetail ────────────────────────────────────────────────────────

    @Test
    fun `loadPlanDetail emite Success cuando tiene éxito`() = runTest {
        val plan = fakeDietPlan(id = "dp-1", days = listOf(fakeDietDay()))
        coEvery { getDietPlanDetailUseCase("dp-1") } returns Result.Success(plan)

        viewModel.loadPlanDetail("dp-1")
        advanceUntilIdle()

        val state = viewModel.detailUiState.value
        assertTrue(state is DietUiState.Success)
        assertEquals("dp-1", (state as DietUiState.Success).plan.id)
        assertEquals(1, state.plan.days.size)
    }

    @Test
    fun `loadPlanDetail emite Error cuando falla`() = runTest {
        coEvery { getDietPlanDetailUseCase(any()) } returns
            Result.Error(AppException.NotFoundException("DietPlan"))

        viewModel.loadPlanDetail("bad-id")
        advanceUntilIdle()

        assertTrue(viewModel.detailUiState.value is DietUiState.Error)
    }

    // ─── onDeletePlan ──────────────────────────────────────────────────────────

    @Test
    fun `onDeletePlan emite ShowSnackbar y NavigateBack en éxito`() = runTest {
        coEvery { deleteDietPlanUseCase("dp-1") } returns Result.Success(Unit)

        viewModel.events.test {
            viewModel.onDeletePlan("dp-1")
            advanceUntilIdle()

            val events = mutableListOf<DietUiEvent>()
            repeat(2) { events.add(awaitItem()) }

            assertTrue(events.any { it is DietUiEvent.ShowSnackbar })
            assertTrue(events.any { it is DietUiEvent.NavigateBack })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onDeletePlan no llama use case cuando plan es ACTIVE`() = runTest {
        val activePlan = fakeDietPlan(id = "dp-active", status = PlanStatus.ACTIVE)
        coEvery { getDietPlanDetailUseCase("dp-active") } returns Result.Success(activePlan)
        viewModel.loadPlanDetail("dp-active")
        advanceUntilIdle()

        viewModel.events.test {
            viewModel.onDeletePlan("dp-active")
            advanceUntilIdle()

            val event = awaitItem()
            assertTrue(event is DietUiEvent.ShowSnackbar)
            coVerify(exactly = 0) { deleteDietPlanUseCase(any()) }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onDeletePlan emite ShowSnackbar en error`() = runTest {
        coEvery { deleteDietPlanUseCase(any()) } returns
            Result.Error(AppException.ServerException)

        viewModel.events.test {
            viewModel.onDeletePlan("dp-1")
            advanceUntilIdle()

            val event = awaitItem()
            assertTrue(event is DietUiEvent.ShowSnackbar)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ─── onGeneratePlan ────────────────────────────────────────────────────────

    @Test
    fun `onGeneratePlan cambia generateUiState a Generating y luego Success`() = runTest {
        val newPlan = fakeDietPlan(id = "gen-diet-1")
        coEvery { generateDietPlanUseCase(any()) } returns Result.Success(newPlan)

        viewModel.onGeneratePlan(fakeGenerateDietPlanRequestDto())
        advanceUntilIdle()

        val state = viewModel.generateUiState.value
        assertTrue(state is GenerateDietUiState.Success)
        assertEquals("gen-diet-1", (state as GenerateDietUiState.Success).plan.id)
    }

    @Test
    fun `onGeneratePlan emite NavigateToDetail en éxito`() = runTest {
        val newPlan = fakeDietPlan(id = "gen-diet-1")
        coEvery { generateDietPlanUseCase(any()) } returns Result.Success(newPlan)

        viewModel.events.test {
            viewModel.onGeneratePlan(fakeGenerateDietPlanRequestDto())
            advanceUntilIdle()

            val event = awaitItem()
            assertTrue(event is DietUiEvent.NavigateToDetail)
            assertEquals("gen-diet-1", (event as DietUiEvent.NavigateToDetail).planId)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onGeneratePlan emite ShowSnackbar en error`() = runTest {
        coEvery { generateDietPlanUseCase(any()) } returns
            Result.Error(AppException.ServerException)

        viewModel.events.test {
            viewModel.onGeneratePlan(fakeGenerateDietPlanRequestDto())
            advanceUntilIdle()

            val event = awaitItem()
            assertTrue(event is DietUiEvent.ShowSnackbar)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onGeneratePlan cambia generateUiState a Error cuando falla`() = runTest {
        coEvery { generateDietPlanUseCase(any()) } returns
            Result.Error(AppException.ServerException)

        viewModel.onGeneratePlan(fakeGenerateDietPlanRequestDto())
        advanceUntilIdle()

        assertTrue(viewModel.generateUiState.value is GenerateDietUiState.Error)
    }

    // ─── onGenerateAdaptivePlan ────────────────────────────────────────────────

    @Test
    fun `onGenerateAdaptivePlan emite NavigateToDetail en éxito`() = runTest {
        val newPlan = fakeDietPlan(id = "adaptive-diet")
        coEvery { generateDietPlanUseCase.invokeAdaptive(any()) } returns Result.Success(newPlan)

        val request = com.jlsh.aifit.feature.diet.data.dto.GenerateAdaptiveDietPlanRequestDto(
            durationWeeks = 4, mealsPerDay = 3, dietPreference = "NONE",
        )

        viewModel.events.test {
            viewModel.onGenerateAdaptivePlan(request)
            advanceUntilIdle()

            val event = awaitItem()
            assertTrue(event is DietUiEvent.NavigateToDetail)
            assertEquals("adaptive-diet", (event as DietUiEvent.NavigateToDetail).planId)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ─── onRejectDietPlan ──────────────────────────────────────────────────────

    @Test
    fun `onRejectDietPlan emite NavigateToDietGenerate en éxito`() = runTest {
        coEvery { deleteDietPlanUseCase("dp-1") } returns Result.Success(Unit)

        viewModel.events.test {
            viewModel.onRejectDietPlan("dp-1")
            advanceUntilIdle()

            val event = awaitItem()
            assertTrue(event is DietUiEvent.NavigateToDietGenerate)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onRejectDietPlan emite ShowSnackbar en error`() = runTest {
        coEvery { deleteDietPlanUseCase(any()) } returns
            Result.Error(AppException.ServerException)

        viewModel.events.test {
            viewModel.onRejectDietPlan("dp-1")
            advanceUntilIdle()

            val event = awaitItem()
            assertTrue(event is DietUiEvent.ShowSnackbar)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ─── onApproveDietPlan ─────────────────────────────────────────────────────

    @Test
    fun `onApproveDietPlan emite NavigateBack en éxito`() = runTest {
        coEvery { setActiveDietPlanUseCase("dp-1") } returns Result.Success(fakeDietPlan(id = "dp-1"))

        viewModel.events.test {
            viewModel.onApproveDietPlan("dp-1")
            advanceUntilIdle()

            val event = awaitItem()
            assertTrue(event is DietUiEvent.PlanApproved)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onApproveDietPlan emite Error y ShowSnackbar cuando falla`() = runTest {
        coEvery { setActiveDietPlanUseCase(any()) } returns
            Result.Error(AppException.ServerException)

        viewModel.onApproveDietPlan("dp-1")
        advanceUntilIdle()

        assertTrue(viewModel.detailUiState.value is DietUiState.Error)
    }
}


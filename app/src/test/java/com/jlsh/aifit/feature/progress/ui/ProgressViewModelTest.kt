package com.jlsh.aifit.feature.progress.ui

import app.cash.turbine.test
import com.jlsh.aifit.core.common.AppException
import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.feature.progress.domain.usecase.GetBodyWeightHistoryUseCase
import com.jlsh.aifit.feature.progress.domain.usecase.GetProgressDashboardUseCase
import com.jlsh.aifit.feature.progress.domain.usecase.GetWeeklyProgressSummaryUseCase
import com.jlsh.aifit.feature.progress.domain.usecase.LogBodyWeightUseCase
import com.jlsh.aifit.feature.progress.ui.state.BodyWeightUiState
import com.jlsh.aifit.feature.progress.ui.state.DashboardUiState
import com.jlsh.aifit.feature.progress.ui.state.ProgressUiEvent
import com.jlsh.aifit.feature.progress.ui.state.WeeklySummaryUiState
import com.jlsh.aifit.testutil.*
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class ProgressViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val getProgressDashboardUseCase: GetProgressDashboardUseCase = mockk()
    private val getWeeklyProgressSummaryUseCase: GetWeeklyProgressSummaryUseCase = mockk()
    private val logBodyWeightUseCase: LogBodyWeightUseCase = mockk()
    private val getBodyWeightHistoryUseCase: GetBodyWeightHistoryUseCase = mockk()

    private fun createViewModel(
        dashboardResult: Result<com.jlsh.aifit.feature.progress.domain.model.ProgressDashboard> =
            Result.Success(fakeProgressDashboard()),
    ): ProgressViewModel {
        coEvery { getProgressDashboardUseCase(any(), any()) } returns dashboardResult
        return ProgressViewModel(
            getProgressDashboardUseCase,
            getWeeklyProgressSummaryUseCase,
            logBodyWeightUseCase,
            getBodyWeightHistoryUseCase,
        )
    }

    // ─── Dashboard States ──────────────────────────────────────────────────────

    @Test
    fun `init carga dashboard y dashboardState es Success`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()

        val state = vm.dashboardState.value
        assertTrue(state is DashboardUiState.Success)
        assertEquals(12, (state as DashboardUiState.Success).dashboard.workoutAdherence.plannedSessions)
    }

    @Test
    fun `cuando getDashboard falla, dashboardState es Error`() = runTest {
        val vm = createViewModel(
            dashboardResult = Result.Error(AppException.NetworkException),
        )
        advanceUntilIdle()

        val state = vm.dashboardState.value
        assertTrue(state is DashboardUiState.Error)
    }

    @Test
    fun `onPeriodSelected recarga dashboard con nuevo periodo`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()

        vm.onPeriodSelected("7 days")
        advanceUntilIdle()

        assertEquals("7 days", vm.selectedPeriod.value)
        assertTrue(vm.dashboardState.value is DashboardUiState.Success)
    }

    // ─── Body Weight States ────────────────────────────────────────────────────

    @Test
    fun `loadBodyWeightHistory cuando useCase emite Success actualiza weightHistory`() = runTest {
        val logs = listOf(fakeBodyWeightLog())
        every { getBodyWeightHistoryUseCase(any(), any()) } returns
                flowOf(Result.Success(logs))
        val vm = createViewModel()
        advanceUntilIdle()

        vm.loadBodyWeightHistory()
        advanceUntilIdle()

        val state = vm.bodyWeightState.value
        assertFalse(state.isLoading)
        assertEquals(1, state.weightHistory.size)
    }

    @Test
    fun `loadBodyWeightHistory cuando useCase emite Error envía ShowSnackbar`() = runTest {
        every { getBodyWeightHistoryUseCase(any(), any()) } returns
                flowOf(Result.Error(AppException.NetworkException))
        val vm = createViewModel()
        advanceUntilIdle()

        vm.events.test {
            vm.loadBodyWeightHistory()
            advanceUntilIdle()

            val event = awaitItem()
            assertTrue(event is ProgressUiEvent.ShowSnackbar)
        }
    }

    @Test
    fun `onWeightChanged actualiza formWeight en bodyWeightState`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()

        vm.onWeightChanged("80.5")

        assertEquals("80.5", vm.bodyWeightState.value.formWeight)
    }

    @Test
    fun `onWeightDateChanged actualiza formDate en bodyWeightState`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()

        val date = LocalDate.of(2026, 4, 1)
        vm.onWeightDateChanged(date)

        assertEquals(date, vm.bodyWeightState.value.formDate)
    }

    @Test
    fun `onWeightNotesChanged actualiza formNotes en bodyWeightState`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()

        vm.onWeightNotesChanged("After breakfast")

        assertEquals("After breakfast", vm.bodyWeightState.value.formNotes)
    }

    @Test
    fun `onLogWeight con peso válido llama a logBodyWeightUseCase y envía WeightLoggedSuccessfully`() = runTest {
        coEvery { logBodyWeightUseCase(any()) } returns Result.Success(fakeBodyWeightLog())
        every { getBodyWeightHistoryUseCase(any(), any()) } returns
                flowOf(Result.Success(listOf(fakeBodyWeightLog())))
        val vm = createViewModel()
        advanceUntilIdle()

        vm.onWeightChanged("78.5")

        vm.events.test {
            vm.onLogWeight()
            advanceUntilIdle()

            val event = awaitItem()
            assertEquals(ProgressUiEvent.WeightLoggedSuccessfully, event)
        }

        // Form should be reset
        assertEquals("", vm.bodyWeightState.value.formWeight)
    }

    @Test
    fun `onLogWeight con peso inválido no hace nada`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()

        vm.onWeightChanged("abc")
        vm.onLogWeight()
        advanceUntilIdle()

        assertFalse(vm.bodyWeightState.value.isSaving)
    }

    @Test
    fun `onLogWeight cuando useCase falla envía ShowSnackbar con error`() = runTest {
        coEvery { logBodyWeightUseCase(any()) } returns
                Result.Error(AppException.ServerException)
        val vm = createViewModel()
        advanceUntilIdle()

        vm.onWeightChanged("78.5")

        vm.events.test {
            vm.onLogWeight()
            advanceUntilIdle()

            val event = awaitItem()
            assertTrue(event is ProgressUiEvent.ShowSnackbar)
        }
    }

    // ─── Weekly Summary States ─────────────────────────────────────────────────

    @Test
    fun `loadWeeklySummary cuando useCase retorna Success, state es Success`() = runTest {
        coEvery { getWeeklyProgressSummaryUseCase() } returns
                Result.Success(fakeWeeklyProgressSummary())
        val vm = createViewModel()
        advanceUntilIdle()

        vm.loadWeeklySummary()
        advanceUntilIdle()

        val state = vm.weeklySummaryState.value
        assertTrue(state is WeeklySummaryUiState.Success)
        assertEquals(3, (state as WeeklySummaryUiState.Success).summary.workoutsThisWeek)
    }

    @Test
    fun `loadWeeklySummary cuando useCase falla, state es Error`() = runTest {
        coEvery { getWeeklyProgressSummaryUseCase() } returns
                Result.Error(AppException.ServerException)
        val vm = createViewModel()
        advanceUntilIdle()

        vm.loadWeeklySummary()
        advanceUntilIdle()

        val state = vm.weeklySummaryState.value
        assertTrue(state is WeeklySummaryUiState.Error)
    }

    // ─── Navigation Events ─────────────────────────────────────────────────────

    @Test
    fun `onNavigateToBodyWeight envía evento NavigateToBodyWeight`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()

        vm.events.test {
            vm.onNavigateToBodyWeight()
            assertEquals(ProgressUiEvent.NavigateToBodyWeight, awaitItem())
        }
    }

    @Test
    fun `onNavigateToWeeklySummary envía evento NavigateToWeeklySummary`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()

        vm.events.test {
            vm.onNavigateToWeeklySummary()
            assertEquals(ProgressUiEvent.NavigateToWeeklySummary, awaitItem())
        }
    }

    @Test
    fun `onNavigateToMetabolic envía evento NavigateToMetabolic`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()

        vm.events.test {
            vm.onNavigateToMetabolic()
            assertEquals(ProgressUiEvent.NavigateToMetabolic, awaitItem())
        }
    }
}


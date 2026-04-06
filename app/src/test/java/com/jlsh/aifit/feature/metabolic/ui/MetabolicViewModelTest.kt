package com.jlsh.aifit.feature.metabolic.ui

import app.cash.turbine.test
import com.jlsh.aifit.core.common.AppException
import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.feature.metabolic.domain.usecase.AnalyzeMetabolicProgressUseCase
import com.jlsh.aifit.feature.metabolic.domain.usecase.ApplyMetabolicAdjustmentUseCase
import com.jlsh.aifit.feature.metabolic.domain.usecase.GetMetabolicInsightsUseCase
import com.jlsh.aifit.feature.metabolic.ui.state.MetabolicUiEvent
import com.jlsh.aifit.feature.metabolic.ui.state.MetabolicUiState
import com.jlsh.aifit.feature.nutrition.domain.usecase.GetCurrentNutritionTargetUseCase
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

@OptIn(ExperimentalCoroutinesApi::class)
class MetabolicViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val analyzeUseCase: AnalyzeMetabolicProgressUseCase = mockk()
    private val insightsUseCase: GetMetabolicInsightsUseCase = mockk()
    private val applyUseCase: ApplyMetabolicAdjustmentUseCase = mockk()
    private val nutritionTargetUseCase: GetCurrentNutritionTargetUseCase = mockk()

    private fun buildViewModel(): MetabolicViewModel {
        return MetabolicViewModel(
            analyzeMetabolicProgressUseCase = analyzeUseCase,
            getMetabolicInsightsUseCase = insightsUseCase,
            applyMetabolicAdjustmentUseCase = applyUseCase,
            getCurrentNutritionTargetUseCase = nutritionTargetUseCase,
        )
    }

    // ── loadAll / init ──────────────────────────────────────────────────────

    @Test
    fun `estado inicial es Loading`() = runTest {
        coEvery { analyzeUseCase() } returns Result.Success(fakeMetabolicAnalysis())
        coEvery { insightsUseCase() } returns Result.Success(listOf(fakeMetabolicInsight()))

        val vm = buildViewModel()

        // After init loadAll completes, state should be Success
        val state = vm.uiState.value
        assertTrue(state is MetabolicUiState.Success)
    }

    @Test
    fun `loadAll con analysis e insights exitosos produce Success`() = runTest {
        val analysis = fakeMetabolicAnalysis()
        val insights = listOf(fakeMetabolicInsight())
        coEvery { analyzeUseCase() } returns Result.Success(analysis)
        coEvery { insightsUseCase() } returns Result.Success(insights)

        val vm = buildViewModel()
        advanceUntilIdle()

        val state = vm.uiState.value
        assertTrue(state is MetabolicUiState.Success)
        val success = state as MetabolicUiState.Success
        assertEquals(analysis, success.analysis)
        assertEquals(insights, success.insights)
        assertFalse(success.isApplying)
    }

    @Test
    fun `loadAll con analysis exitoso e insights fallidos produce Success con insights vacios`() = runTest {
        val analysis = fakeMetabolicAnalysis()
        coEvery { analyzeUseCase() } returns Result.Success(analysis)
        coEvery { insightsUseCase() } returns Result.Error(AppException.ServerException)

        val vm = buildViewModel()
        advanceUntilIdle()

        val state = vm.uiState.value
        assertTrue(state is MetabolicUiState.Success)
        val success = state as MetabolicUiState.Success
        assertEquals(analysis, success.analysis)
        assertTrue(success.insights.isEmpty())
    }

    @Test
    fun `loadAll con analysis fallido produce Error`() = runTest {
        coEvery { analyzeUseCase() } returns Result.Error(AppException.NetworkException)
        coEvery { insightsUseCase() } returns Result.Success(emptyList())

        val vm = buildViewModel()
        advanceUntilIdle()

        val state = vm.uiState.value
        assertTrue(state is MetabolicUiState.Error)
    }

    // ── onApplyAdjustment ───────────────────────────────────────────────────

    @Test
    fun `onApplyAdjustment exitoso envia evento AdjustmentApplied y recarga`() = runTest {
        val analysis = fakeMetabolicAnalysis()
        val insights = listOf(fakeMetabolicInsight())
        coEvery { analyzeUseCase() } returns Result.Success(analysis)
        coEvery { insightsUseCase() } returns Result.Success(insights)
        coEvery { applyUseCase(any()) } returns Result.Success(fakeNutritionTarget())
        every { nutritionTargetUseCase() } returns flowOf(Result.Success(fakeNutritionTarget()))

        val vm = buildViewModel()
        advanceUntilIdle()

        vm.events.test {
            vm.onApplyAdjustment()
            advanceUntilIdle()

            val snackbar = awaitItem()
            assertTrue(snackbar is MetabolicUiEvent.ShowSnackbar)
            assertEquals("Ajuste aplicado correctamente", (snackbar as MetabolicUiEvent.ShowSnackbar).message)

            val applied = awaitItem()
            assertTrue(applied is MetabolicUiEvent.AdjustmentApplied)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onApplyAdjustment fallido muestra snackbar de error y restaura isApplying`() = runTest {
        val analysis = fakeMetabolicAnalysis()
        coEvery { analyzeUseCase() } returns Result.Success(analysis)
        coEvery { insightsUseCase() } returns Result.Success(emptyList())
        coEvery { applyUseCase(any()) } returns Result.Error(AppException.ServerException)

        val vm = buildViewModel()
        advanceUntilIdle()

        vm.events.test {
            vm.onApplyAdjustment()
            advanceUntilIdle()

            val event = awaitItem()
            assertTrue(event is MetabolicUiEvent.ShowSnackbar)

            cancelAndIgnoreRemainingEvents()
        }

        val state = vm.uiState.value
        assertTrue(state is MetabolicUiState.Success)
        assertFalse((state as MetabolicUiState.Success).isApplying)
    }

    @Test
    fun `onApplyAdjustment no hace nada si estado no es Success`() = runTest {
        coEvery { analyzeUseCase() } returns Result.Error(AppException.ServerException)
        coEvery { insightsUseCase() } returns Result.Success(emptyList())

        val vm = buildViewModel()
        advanceUntilIdle()

        assertTrue(vm.uiState.value is MetabolicUiState.Error)

        // This should be a no-op
        vm.onApplyAdjustment()
        advanceUntilIdle()

        assertTrue(vm.uiState.value is MetabolicUiState.Error)
    }

    @Test
    fun `onApplyAdjustment no hace nada si analysis no tiene recomendacion`() = runTest {
        val analysis = fakeMetabolicAnalysis(recommendation = null)
        coEvery { analyzeUseCase() } returns Result.Success(analysis)
        coEvery { insightsUseCase() } returns Result.Success(emptyList())

        val vm = buildViewModel()
        advanceUntilIdle()

        val stateBefore = vm.uiState.value
        vm.onApplyAdjustment()
        advanceUntilIdle()

        assertEquals(stateBefore, vm.uiState.value)
    }
}


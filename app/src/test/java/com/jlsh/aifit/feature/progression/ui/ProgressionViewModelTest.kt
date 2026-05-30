package com.jlsh.aifit.feature.progression.ui

import com.jlsh.aifit.core.common.AppException
import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.feature.progression.domain.ProgressionRequirements.MIN_SESSIONS_REQUIRED
import com.jlsh.aifit.feature.progression.domain.model.ProgressionType
import com.jlsh.aifit.feature.progression.domain.usecase.GetExerciseProgressionRecommendationUseCase
import com.jlsh.aifit.feature.progression.domain.usecase.GetFullPlanProgressionRecommendationsUseCase
import com.jlsh.aifit.feature.workout.domain.usecase.GetExerciseLoggedSessionCountUseCase
import com.jlsh.aifit.testutil.*
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProgressionViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val getExerciseRecommendationUseCase: GetExerciseProgressionRecommendationUseCase = mockk()
    private val getPlanRecommendationsUseCase: GetFullPlanProgressionRecommendationsUseCase = mockk()
    private val getExerciseLoggedSessionCountUseCase: GetExerciseLoggedSessionCountUseCase = mockk()

    private fun createViewModel() = ProgressionViewModel(
        getExerciseRecommendationUseCase,
        getPlanRecommendationsUseCase,
        getExerciseLoggedSessionCountUseCase,
    )

    // ─── Initial state ──────────────────────────── ────────────────────────────

    @Test
    fun `estado inicial de recommendationState es Idle`() {
        val vm = createViewModel()
        assertTrue(vm.recommendationState.value is RecommendationState.Idle)
    }

    @Test
    fun `estado inicial de planSummaryState es Idle`() {
        val vm = createViewModel()
        assertTrue(vm.planSummaryState.value is PlanSummaryState.Idle)
    }

    // ─── openExerciseProgression ─────────────────────────────────────────────────

    @Test
    fun `openExerciseProgression con pocas sesiones del ejercicio emite InsufficientData`() = runTest {
        coEvery { getExerciseLoggedSessionCountUseCase("exercise-1") } returns Result.Success(1)
        val vm = createViewModel()

        vm.openExerciseProgression("exercise-1")
        advanceUntilIdle()

        val state = vm.recommendationState.value
        assertTrue(state is RecommendationState.InsufficientData)
        assertEquals(1, (state as RecommendationState.InsufficientData).currentSessions)
        assertEquals(1, vm.exerciseSessionCounts.value["exercise-1"])
    }

    @Test
    fun `openExerciseProgression con suficientes sesiones del ejercicio emite PromptConfirm`() = runTest {
        coEvery { getExerciseLoggedSessionCountUseCase("exercise-1") } returns Result.Success(MIN_SESSIONS_REQUIRED)
        val vm = createViewModel()

        vm.openExerciseProgression("exercise-1")
        advanceUntilIdle()

        assertTrue(vm.recommendationState.value is RecommendationState.PromptConfirm)
    }

    @Test
    fun `confirmExerciseProgression sin sesiones suficientes no llama al useCase de recomendacion`() = runTest {
        coEvery { getExerciseLoggedSessionCountUseCase("exercise-1") } returns Result.Success(2)
        val vm = createViewModel()

        vm.openExerciseProgression("exercise-1")
        advanceUntilIdle()
        vm.confirmExerciseProgression()
        advanceUntilIdle()

        assertTrue(vm.recommendationState.value is RecommendationState.InsufficientData)
    }

    @Test
    fun `confirmExerciseProgression tras PromptConfirm carga la recomendacion`() = runTest {
        coEvery { getExerciseLoggedSessionCountUseCase("exercise-1") } returns Result.Success(MIN_SESSIONS_REQUIRED)
        val recommendation = fakeProgressionRecommendation()
        coEvery { getExerciseRecommendationUseCase("exercise-1") } returns Result.Success(recommendation)
        val vm = createViewModel()

        vm.openExerciseProgression("exercise-1")
        advanceUntilIdle()
        vm.confirmExerciseProgression()
        advanceUntilIdle()

        assertTrue(vm.recommendationState.value is RecommendationState.Success)
    }

    // ─── loadExerciseRecommendation ────────────────────────────────────────────

    @Test
    fun `loadExerciseRecommendation cuando useCase retorna Success, state es Success`() = runTest {
        val recommendation = fakeProgressionRecommendation()
        coEvery { getExerciseRecommendationUseCase(any()) } returns Result.Success(recommendation)
        val vm = createViewModel()

        vm.loadExerciseRecommendation("exercise-1")
        advanceUntilIdle()

        val state = vm.recommendationState.value
        assertTrue(state is RecommendationState.Success)
        assertEquals(recommendation, (state as RecommendationState.Success).data)
    }

    @Test
    fun `loadExerciseRecommendation cuando el tipo es INSUFFICIENT_DATA emite InsufficientData`() = runTest {
        val recommendation = fakeProgressionRecommendation(type = ProgressionType.INSUFFICIENT_DATA, basedOnSessions = 1)
        coEvery { getExerciseRecommendationUseCase(any()) } returns Result.Success(recommendation)
        val vm = createViewModel()

        vm.loadExerciseRecommendation("exercise-1")
        advanceUntilIdle()

        assertTrue(vm.recommendationState.value is RecommendationState.InsufficientData)
    }

    @Test
    fun `loadExerciseRecommendation cuando useCase falla, state es Error`() = runTest {
        coEvery { getExerciseRecommendationUseCase(any()) } returns
                Result.Error(AppException.NetworkException)
        val vm = createViewModel()

        vm.loadExerciseRecommendation("exercise-1")
        advanceUntilIdle()

        val state = vm.recommendationState.value
        assertTrue(state is RecommendationState.Error)
    }

    // ─── loadPlanRecommendations ───────────────────────────────────────────────

    @Test
    fun `loadPlanRecommendations cuando useCase retorna Success, state es Success`() = runTest {
        val summary = fakePlanProgressionSummary()
        coEvery { getPlanRecommendationsUseCase(any()) } returns Result.Success(summary)
        val vm = createViewModel()

        vm.loadPlanRecommendations("plan-1")
        advanceUntilIdle()

        val state = vm.planSummaryState.value
        assertTrue(state is PlanSummaryState.Success)
        assertEquals(summary, (state as PlanSummaryState.Success).data)
    }

    @Test
    fun `loadPlanRecommendations cuando useCase falla, state es Error`() = runTest {
        coEvery { getPlanRecommendationsUseCase(any()) } returns
                Result.Error(AppException.ServerException)
        val vm = createViewModel()

        vm.loadPlanRecommendations("plan-1")
        advanceUntilIdle()

        assertTrue(vm.planSummaryState.value is PlanSummaryState.Error)
    }

    // ─── Resets ───────────────────────────────────────────────────────────────

    @Test
    fun `resetRecommendationState vuelve a Idle`() = runTest {
        val recommendation = fakeProgressionRecommendation()
        coEvery { getExerciseRecommendationUseCase(any()) } returns Result.Success(recommendation)
        val vm = createViewModel()

        vm.loadExerciseRecommendation("exercise-1")
        advanceUntilIdle()
        assertTrue(vm.recommendationState.value is RecommendationState.Success)

        vm.resetRecommendationState()

        assertTrue(vm.recommendationState.value is RecommendationState.Idle)
    }

    @Test
    fun `resetPlanSummaryState vuelve a Idle`() = runTest {
        val summary = fakePlanProgressionSummary()
        coEvery { getPlanRecommendationsUseCase(any()) } returns Result.Success(summary)
        val vm = createViewModel()

        vm.loadPlanRecommendations("plan-1")
        advanceUntilIdle()
        assertTrue(vm.planSummaryState.value is PlanSummaryState.Success)

        vm.resetPlanSummaryState()

        assertTrue(vm.planSummaryState.value is PlanSummaryState.Idle)
    }
}

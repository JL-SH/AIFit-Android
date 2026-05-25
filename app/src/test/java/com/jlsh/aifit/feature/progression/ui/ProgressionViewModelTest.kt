package com.jlsh.aifit.feature.progression.ui

import com.jlsh.aifit.core.common.AppException
import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.feature.progression.domain.usecase.GetExerciseProgressionRecommendationUseCase
import com.jlsh.aifit.feature.progression.domain.usecase.GetFullPlanProgressionRecommendationsUseCase
import com.jlsh.aifit.feature.workout.domain.usecase.GetWorkoutHistoryUseCase
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
class ProgressionViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val getExerciseRecommendationUseCase: GetExerciseProgressionRecommendationUseCase = mockk()
    private val getPlanRecommendationsUseCase: GetFullPlanProgressionRecommendationsUseCase = mockk()
    private val getWorkoutHistoryUseCase: GetWorkoutHistoryUseCase = mockk {
        every { this@mockk.invoke(any(), any(), any()) } returns flowOf(Result.Success(emptyList()))
    }

    private fun createViewModel() = ProgressionViewModel(
        getExerciseRecommendationUseCase,
        getPlanRecommendationsUseCase,
        getWorkoutHistoryUseCase,
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


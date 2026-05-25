package com.jlsh.aifit.feature.education.ui

import com.jlsh.aifit.core.common.AppException
import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.feature.education.domain.usecase.GetExerciseExplanationUseCase
import com.jlsh.aifit.feature.education.domain.usecase.GetGlossaryTermUseCase
import com.jlsh.aifit.feature.education.domain.usecase.GetMealExplanationUseCase
import com.jlsh.aifit.feature.education.domain.usecase.GetWhyThisExerciseUseCase
import com.jlsh.aifit.feature.education.domain.usecase.GetWhyThisMealUseCase
import com.jlsh.aifit.feature.education.ui.state.ExplanationState
import com.jlsh.aifit.feature.education.ui.state.GlossaryState
import com.jlsh.aifit.feature.education.ui.state.WhyThisState
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
class EducationViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val getExerciseExplanationUseCase: GetExerciseExplanationUseCase = mockk()
    private val getMealExplanationUseCase: GetMealExplanationUseCase = mockk()
    private val getWhyThisExerciseUseCase: GetWhyThisExerciseUseCase = mockk()
    private val getWhyThisMealUseCase: GetWhyThisMealUseCase = mockk()
    private val getGlossaryTermUseCase: GetGlossaryTermUseCase = mockk()

    private fun createViewModel() = EducationViewModel(
        getExerciseExplanationUseCase,
        getMealExplanationUseCase,
        getWhyThisExerciseUseCase,
        getWhyThisMealUseCase,
        getGlossaryTermUseCase,
    )

    // ─── Initial state ──────────────────────────── ────────────────────────────

    @Test
    fun `estado inicial de explanationState es Idle`() {
        val vm = createViewModel()
        assertTrue(vm.explanationState.value is ExplanationState.Idle)
    }

    @Test
    fun `estado inicial de whyThisState es Idle`() {
        val vm = createViewModel()
        assertTrue(vm.whyThisState.value is WhyThisState.Idle)
    }

    @Test
    fun `estado inicial de glossaryState es Idle`() {
        val vm = createViewModel()
        assertTrue(vm.glossaryState.value is GlossaryState.Idle)
    }

    // ─── loadExerciseExplanation ───────────────────────────────────────────────

    @Test
    fun `loadExerciseExplanation cuando useCase retorna Success, state es Success`() = runTest {
        val explanation = fakeContextualExplanation()
        coEvery { getExerciseExplanationUseCase(any()) } returns Result.Success(explanation)
        val vm = createViewModel()

        vm.loadExerciseExplanation("exercise-1")
        advanceUntilIdle()

        val state = vm.explanationState.value
        assertTrue(state is ExplanationState.Success)
        assertEquals(explanation, (state as ExplanationState.Success).data)
    }

    @Test
    fun `loadExerciseExplanation cuando useCase falla, state es Error`() = runTest {
        coEvery { getExerciseExplanationUseCase(any()) } returns
                Result.Error(AppException.NetworkException)
        val vm = createViewModel()

        vm.loadExerciseExplanation("exercise-1")
        advanceUntilIdle()

        assertTrue(vm.explanationState.value is ExplanationState.Error)
    }

    // ─── loadMealExplanation ───────────────────────────────────────────────────

    @Test
    fun `loadMealExplanation cuando useCase retorna Success, state es Success`() = runTest {
        val explanation = fakeContextualExplanation()
        coEvery { getMealExplanationUseCase(any()) } returns Result.Success(explanation)
        val vm = createViewModel()

        vm.loadMealExplanation("meal-1")
        advanceUntilIdle()

        assertTrue(vm.explanationState.value is ExplanationState.Success)
    }

    @Test
    fun `loadMealExplanation cuando useCase falla, state es Error`() = runTest {
        coEvery { getMealExplanationUseCase(any()) } returns
                Result.Error(AppException.ServerException)
        val vm = createViewModel()

        vm.loadMealExplanation("meal-1")
        advanceUntilIdle()

        assertTrue(vm.explanationState.value is ExplanationState.Error)
    }

    // ─── loadWhyThisExercise ───────────────────────────────────────────────────

    @Test
    fun `loadWhyThisExercise cuando useCase retorna Success, state es Success`() = runTest {
        val explanation = fakeWhyThisExplanation()
        coEvery { getWhyThisExerciseUseCase(any()) } returns Result.Success(explanation)
        val vm = createViewModel()

        vm.loadWhyThisExercise("exercise-1")
        advanceUntilIdle()

        val state = vm.whyThisState.value
        assertTrue(state is WhyThisState.Success)
        assertEquals(explanation, (state as WhyThisState.Success).data)
    }

    @Test
    fun `loadWhyThisExercise cuando useCase falla, state es Error`() = runTest {
        coEvery { getWhyThisExerciseUseCase(any()) } returns
                Result.Error(AppException.NetworkException)
        val vm = createViewModel()

        vm.loadWhyThisExercise("exercise-1")
        advanceUntilIdle()

        assertTrue(vm.whyThisState.value is WhyThisState.Error)
    }

    // ─── loadWhyThisMeal ──────────────────────────────────────────────────────

    @Test
    fun `loadWhyThisMeal cuando useCase retorna Success, state es Success`() = runTest {
        val explanation = fakeWhyThisExplanation()
        coEvery { getWhyThisMealUseCase(any()) } returns Result.Success(explanation)
        val vm = createViewModel()

        vm.loadWhyThisMeal("meal-1")
        advanceUntilIdle()

        assertTrue(vm.whyThisState.value is WhyThisState.Success)
    }

    @Test
    fun `loadWhyThisMeal cuando useCase falla, state es Error`() = runTest {
        coEvery { getWhyThisMealUseCase(any()) } returns
                Result.Error(AppException.ServerException)
        val vm = createViewModel()

        vm.loadWhyThisMeal("meal-1")
        advanceUntilIdle()

        assertTrue(vm.whyThisState.value is WhyThisState.Error)
    }

    // ─── loadGlossaryTerm ─────────────────────────────────────────────────────

    @Test
    fun `loadGlossaryTerm cuando useCase retorna Success, state es Success`() = runTest {
        val definition = fakeGlossaryDefinition()
        coEvery { getGlossaryTermUseCase(any()) } returns Result.Success(definition)
        val vm = createViewModel()

        vm.loadGlossaryTerm("Hypertrophy")
        advanceUntilIdle()

        val state = vm.glossaryState.value
        assertTrue(state is GlossaryState.Success)
        assertEquals(definition, (state as GlossaryState.Success).data)
    }

    @Test
    fun `loadGlossaryTerm cuando useCase falla, state es Error`() = runTest {
        coEvery { getGlossaryTermUseCase(any()) } returns
                Result.Error(AppException.NetworkException)
        val vm = createViewModel()

        vm.loadGlossaryTerm("Hypertrophy")
        advanceUntilIdle()

        assertTrue(vm.glossaryState.value is GlossaryState.Error)
    }

    // ─── Resets ───────────────────────────────────────────────────────────────

    @Test
    fun `resetExplanationState vuelve a Idle`() = runTest {
        val explanation = fakeContextualExplanation()
        coEvery { getExerciseExplanationUseCase(any()) } returns Result.Success(explanation)
        val vm = createViewModel()

        vm.loadExerciseExplanation("exercise-1")
        advanceUntilIdle()
        assertTrue(vm.explanationState.value is ExplanationState.Success)

        vm.resetExplanationState()

        assertTrue(vm.explanationState.value is ExplanationState.Idle)
    }

    @Test
    fun `resetWhyThisState vuelve a Idle`() = runTest {
        val explanation = fakeWhyThisExplanation()
        coEvery { getWhyThisExerciseUseCase(any()) } returns Result.Success(explanation)
        val vm = createViewModel()

        vm.loadWhyThisExercise("exercise-1")
        advanceUntilIdle()
        assertTrue(vm.whyThisState.value is WhyThisState.Success)

        vm.resetWhyThisState()

        assertTrue(vm.whyThisState.value is WhyThisState.Idle)
    }

    @Test
    fun `resetGlossaryState vuelve a Idle`() = runTest {
        val definition = fakeGlossaryDefinition()
        coEvery { getGlossaryTermUseCase(any()) } returns Result.Success(definition)
        val vm = createViewModel()

        vm.loadGlossaryTerm("Hypertrophy")
        advanceUntilIdle()
        assertTrue(vm.glossaryState.value is GlossaryState.Success)

        vm.resetGlossaryState()

        assertTrue(vm.glossaryState.value is GlossaryState.Idle)
    }
}


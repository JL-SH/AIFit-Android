package com.jlsh.aifit.feature.vision.ui

import app.cash.turbine.test
import com.jlsh.aifit.core.common.AppException
import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.feature.nutrition.domain.usecase.TrackMealUseCase
import com.jlsh.aifit.feature.vision.domain.usecase.AnalyzeFoodPhotoUseCase
import com.jlsh.aifit.feature.vision.ui.state.VisionUiEvent
import com.jlsh.aifit.feature.vision.ui.state.VisionUiState
import com.jlsh.aifit.testutil.*
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class VisionViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val analyzeUseCase: AnalyzeFoodPhotoUseCase = mockk()
    private val trackMealUseCase: TrackMealUseCase = mockk()

    private fun buildViewModel(): VisionViewModel = VisionViewModel(
        analyzeFoodPhotoUseCase = analyzeUseCase,
        trackMealUseCase = trackMealUseCase,
    )

    @Test
    fun `estado inicial es Idle`() = runTest {
        val vm = buildViewModel()

        assertTrue(vm.uiState.value is VisionUiState.Idle)
    }

    @Test
    fun `onSelectFromGallery exitoso transiciona a Result`() = runTest {
        val analysisResult = fakeFoodPhotoAnalysisResult()
        coEvery { analyzeUseCase(any(), any()) } returns Result.Success(analysisResult)

        val vm = buildViewModel()
        vm.onSelectFromGallery(byteArrayOf(1, 2, 3))
        advanceUntilIdle()

        val state = vm.uiState.value
        assertTrue(state is VisionUiState.Result)
        assertEquals(analysisResult, (state as VisionUiState.Result).result)
    }

    @Test
    fun `onSelectFromGallery con error transiciona a Error`() = runTest {
        coEvery { analyzeUseCase(any(), any()) } returns Result.Error(AppException.ServerException)

        val vm = buildViewModel()
        vm.onSelectFromGallery(byteArrayOf(1, 2, 3))
        advanceUntilIdle()

        val state = vm.uiState.value
        assertTrue(state is VisionUiState.Error)
    }

    @Test
    fun `onTryAgain restaura estado a Idle`() = runTest {
        val analysisResult = fakeFoodPhotoAnalysisResult()
        coEvery { analyzeUseCase(any(), any()) } returns Result.Success(analysisResult)

        val vm = buildViewModel()
        vm.onSelectFromGallery(byteArrayOf(1, 2, 3))
        advanceUntilIdle()
        assertTrue(vm.uiState.value is VisionUiState.Result)

        vm.onTryAgain()

        assertTrue(vm.uiState.value is VisionUiState.Idle)
    }

    @Test
    fun `onLogMeal guarda comida y emite MealLogged y NavigateBack`() = runTest {
        val analysisResult = fakeFoodPhotoAnalysisResult(identifiedFoodName = "Ensalada")
        coEvery { analyzeUseCase(any(), any()) } returns Result.Success(analysisResult)
        coEvery { trackMealUseCase(any()) } returns Result.Success(fakeMealLog())

        val vm = buildViewModel()
        vm.onSelectFromGallery(byteArrayOf(1, 2, 3))
        advanceUntilIdle()

        vm.events.test {
            vm.onLogMeal()
            advanceUntilIdle()

            assertEquals(VisionUiEvent.MealLogged, awaitItem())
            assertEquals(VisionUiEvent.NavigateBack, awaitItem())

            cancelAndIgnoreRemainingEvents()
        }

        val requestSlot = slot<com.jlsh.aifit.feature.nutrition.data.dto.TrackMealRequestDto>()
        coVerify { trackMealUseCase(capture(requestSlot)) }
        assertEquals("Ensalada", requestSlot.captured.name)
        assertTrue(requestSlot.captured.items.isNotEmpty())
    }

    @Test
    fun `onLogMeal con error muestra snackbar y mantiene resultado`() = runTest {
        val analysisResult = fakeFoodPhotoAnalysisResult()
        coEvery { analyzeUseCase(any(), any()) } returns Result.Success(analysisResult)
        coEvery { trackMealUseCase(any()) } returns Result.Error(AppException.ServerException)

        val vm = buildViewModel()
        vm.onSelectFromGallery(byteArrayOf(1, 2, 3))
        advanceUntilIdle()

        vm.events.test {
            vm.onLogMeal()
            advanceUntilIdle()

            val event = awaitItem()
            assertTrue(event is VisionUiEvent.ShowSnackbar)

            cancelAndIgnoreRemainingEvents()
        }

        val state = vm.uiState.value as VisionUiState.Result
        assertFalse(state.isSaving)
        assertEquals(analysisResult, state.result)
    }

    @Test
    fun `onLogMeal no hace nada si estado no es Result`() = runTest {
        val vm = buildViewModel()
        assertTrue(vm.uiState.value is VisionUiState.Idle)

        vm.onLogMeal()
        advanceUntilIdle()

        assertTrue(vm.uiState.value is VisionUiState.Idle)
        coVerify(exactly = 0) { trackMealUseCase(any()) }
    }
}

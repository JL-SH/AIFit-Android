package com.jlsh.aifit.feature.vision.ui

import app.cash.turbine.test
import com.jlsh.aifit.core.common.AppException
import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.feature.vision.domain.usecase.AnalyzeFoodPhotoUseCase
import com.jlsh.aifit.feature.vision.ui.state.VisionUiEvent
import com.jlsh.aifit.feature.vision.ui.state.VisionUiState
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
class VisionViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val analyzeUseCase: AnalyzeFoodPhotoUseCase = mockk()

    private fun buildViewModel(): VisionViewModel = VisionViewModel(analyzeUseCase)

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
    fun `onLogMeal envia NavigateToTrackMeal cuando hay Result`() = runTest {
        val analysisResult = fakeFoodPhotoAnalysisResult()
        coEvery { analyzeUseCase(any(), any()) } returns Result.Success(analysisResult)

        val vm = buildViewModel()
        vm.onSelectFromGallery(byteArrayOf(1, 2, 3))
        advanceUntilIdle()

        vm.events.test {
            vm.onLogMeal()
            advanceUntilIdle()

            val event = awaitItem()
            assertTrue(event is VisionUiEvent.NavigateToTrackMeal)
            assertTrue((event as VisionUiEvent.NavigateToTrackMeal).prefilled.isNotBlank())

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onLogMeal no hace nada si estado no es Result`() = runTest {
        val vm = buildViewModel()
        assertTrue(vm.uiState.value is VisionUiState.Idle)

        // Should be a no-op
        vm.onLogMeal()
        advanceUntilIdle()

        assertTrue(vm.uiState.value is VisionUiState.Idle)
    }
}


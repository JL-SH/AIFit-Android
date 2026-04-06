package com.jlsh.aifit.feature.progression.data.repository

import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.core.network.ApiResponse
import com.jlsh.aifit.feature.progression.data.api.ProgressionApiService
import com.jlsh.aifit.testutil.*
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class ProgressionRepositoryImplTest {

    private val apiService: ProgressionApiService = mockk()
    private val repository = ProgressionRepositoryImpl(apiService)

    // ─── getExerciseRecommendation ─────────────────────────────────────────────

    @Test
    fun `getExerciseRecommendation cuando API responde OK retorna Success`() = runTest {
        coEvery { apiService.getExerciseRecommendation(any()) } returns ApiResponse(
            success = true, data = fakeProgressionRecommendationResponseDto(),
        )

        val result = repository.getExerciseRecommendation("exercise-1")

        assertTrue(result is Result.Success)
        assertEquals("exercise-1", (result as Result.Success).data.trainingExerciseId)
    }

    @Test
    fun `getExerciseRecommendation cuando API falla retorna Error`() = runTest {
        coEvery { apiService.getExerciseRecommendation(any()) } throws Exception("Network error")

        val result = repository.getExerciseRecommendation("exercise-1")

        assertTrue(result is Result.Error)
    }

    // ─── getPlanRecommendations ────────────────────────────────────────────────

    @Test
    fun `getPlanRecommendations cuando API responde OK retorna Success`() = runTest {
        coEvery { apiService.getPlanRecommendations(any()) } returns ApiResponse(
            success = true, data = fakePlanProgressionSummaryResponseDto(),
        )

        val result = repository.getPlanRecommendations("plan-1")

        assertTrue(result is Result.Success)
        assertEquals("plan-1", (result as Result.Success).data.planId)
    }

    @Test
    fun `getPlanRecommendations cuando API falla retorna Error`() = runTest {
        coEvery { apiService.getPlanRecommendations(any()) } throws Exception("Server error")

        val result = repository.getPlanRecommendations("plan-1")

        assertTrue(result is Result.Error)
    }
}


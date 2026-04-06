package com.jlsh.aifit.feature.metabolic.data.repository

import com.jlsh.aifit.core.common.AppException
import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.core.network.ApiResponse
import com.jlsh.aifit.feature.metabolic.data.api.MetabolicApiService
import com.jlsh.aifit.feature.metabolic.domain.model.MetabolicStatus
import com.jlsh.aifit.testutil.*
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class MetabolicRepositoryImplTest {

    private val apiService: MetabolicApiService = mockk()
    private lateinit var repository: MetabolicRepositoryImpl

    @Before
    fun setUp() {
        repository = MetabolicRepositoryImpl(apiService)
    }

    // ── analyzeMetabolicProgress ────────────────────────────────────────────

    @Test
    fun `analyzeMetabolicProgress retorna Success cuando API responde ok`() = runTest {
        val dto = fakeMetabolicAnalysisResponseDto()
        coEvery { apiService.getAnalysis() } returns ApiResponse(success = true, data = dto)

        val result = repository.analyzeMetabolicProgress()

        assertTrue(result is Result.Success)
        val analysis = (result as Result.Success).data
        assertEquals(MetabolicStatus.ON_TRACK, analysis.status)
    }

    @Test
    fun `analyzeMetabolicProgress retorna Error cuando API falla`() = runTest {
        coEvery { apiService.getAnalysis() } throws RuntimeException("Server error")

        val result = repository.analyzeMetabolicProgress()

        assertTrue(result is Result.Error)
    }

    // ── getInsights ─────────────────────────────────────────────────────────

    @Test
    fun `getInsights retorna Success con lista de insights`() = runTest {
        val dtos = listOf(fakeMetabolicInsightResponseDto(), fakeMetabolicInsightResponseDto(id = "insight-2"))
        coEvery { apiService.getInsights() } returns ApiResponse(success = true, data = dtos)

        val result = repository.getInsights()

        assertTrue(result is Result.Success)
        assertEquals(2, (result as Result.Success).data.size)
    }

    @Test
    fun `getInsights retorna Error cuando API falla`() = runTest {
        coEvery { apiService.getInsights() } throws RuntimeException("Network error")

        val result = repository.getInsights()

        assertTrue(result is Result.Error)
    }

    // ── applyAdjustment ─────────────────────────────────────────────────────

    @Test
    fun `applyAdjustment retorna Success con NutritionTarget`() = runTest {
        val targetDto = fakeNutritionTargetResponseDto()
        val request = fakeApplyMetabolicAdjustmentRequestDto()
        coEvery { apiService.applyAdjustment(request) } returns ApiResponse(success = true, data = targetDto)

        val result = repository.applyAdjustment(request)

        assertTrue(result is Result.Success)
    }

    @Test
    fun `applyAdjustment retorna Error cuando API falla`() = runTest {
        val request = fakeApplyMetabolicAdjustmentRequestDto()
        coEvery { apiService.applyAdjustment(request) } throws RuntimeException("Server error")

        val result = repository.applyAdjustment(request)

        assertTrue(result is Result.Error)
    }
}


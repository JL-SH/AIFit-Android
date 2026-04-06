package com.jlsh.aifit.feature.nutrition.data.repository

import app.cash.turbine.test
import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.core.network.ApiResponse
import com.jlsh.aifit.feature.nutrition.data.api.NutritionTargetApiService
import com.jlsh.aifit.testutil.*
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class NutritionTargetRepositoryImplTest {

    private val apiService: NutritionTargetApiService = mockk()
    private lateinit var sut: NutritionTargetRepositoryImpl

    @Before
    fun setUp() {
        sut = NutritionTargetRepositoryImpl(apiService)
    }

    // ─── getCurrentTarget ──────────────────────────────────────────────────────

    @Test
    fun `getCurrentTarget emite Loading y luego Success con target`() = runTest {
        coEvery { apiService.getCurrentTarget() } returns ApiResponse(
            success = true, data = fakeNutritionTargetResponseDto(),
        )

        sut.getCurrentTarget().test {
            val loading = awaitItem()
            assertTrue(loading is Result.Loading)

            val success = awaitItem()
            assertTrue(success is Result.Success)
            assertEquals("target-1", (success as Result.Success).data.id)
            assertEquals(2200, success.data.calorieTarget)

            awaitComplete()
        }
    }

    @Test
    fun `getCurrentTarget emite Error cuando API falla`() = runTest {
        coEvery { apiService.getCurrentTarget() } throws java.io.IOException("fail")

        sut.getCurrentTarget().test {
            val loading = awaitItem()
            assertTrue(loading is Result.Loading)

            val error = awaitItem()
            assertTrue(error is Result.Error)

            awaitComplete()
        }
    }

    // ─── updateTarget ──────────────────────────────────────────────────────────

    @Test
    fun `updateTarget retorna Success con target actualizado`() = runTest {
        val request = fakeUpdateNutritionTargetRequestDto()
        coEvery { apiService.updateTarget(request) } returns ApiResponse(
            success = true, data = fakeNutritionTargetResponseDto(calorieTarget = 2500),
        )

        val result = sut.updateTarget(request)

        assertTrue(result is Result.Success)
        assertEquals(2500, (result as Result.Success).data.calorieTarget)
    }

    @Test
    fun `updateTarget retorna Error cuando API falla`() = runTest {
        val request = fakeUpdateNutritionTargetRequestDto()
        coEvery { apiService.updateTarget(request) } throws java.io.IOException("fail")

        val result = sut.updateTarget(request)

        assertTrue(result is Result.Error)
    }
}


package com.jlsh.aifit.feature.vision.data.repository

import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.core.network.ApiResponse
import com.jlsh.aifit.feature.vision.data.api.VisionApiService
import com.jlsh.aifit.testutil.*
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class VisionRepositoryImplTest {

    private val apiService: VisionApiService = mockk()
    private lateinit var repository: VisionRepositoryImpl

    @Before
    fun setUp() {
        repository = VisionRepositoryImpl(apiService)
    }

    @Test
    fun `analyzePhoto retorna Success cuando API responde ok`() = runTest {
        val dto = fakeFoodPhotoAnalysisResponseDto()
        coEvery { apiService.analyzePhoto(any()) } returns ApiResponse(success = true, data = dto)

        val result = repository.analyzePhoto(byteArrayOf(1, 2, 3), "image/jpeg")

        assertTrue(result is Result.Success)
        assertEquals("Grilled Chicken Salad", (result as Result.Success).data.identifiedFoodName)
    }

    @Test
    fun `analyzePhoto retorna Error cuando API falla`() = runTest {
        coEvery { apiService.analyzePhoto(any()) } throws RuntimeException("Server error")

        val result = repository.analyzePhoto(byteArrayOf(1, 2, 3), "image/jpeg")

        assertTrue(result is Result.Error)
    }
}


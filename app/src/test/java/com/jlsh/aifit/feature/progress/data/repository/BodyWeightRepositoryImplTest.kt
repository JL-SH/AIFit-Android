package com.jlsh.aifit.feature.progress.data.repository

import com.jlsh.aifit.core.common.AppException
import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.core.network.ApiResponse
import com.jlsh.aifit.feature.progress.data.api.BodyWeightApiService
import com.jlsh.aifit.feature.progress.data.local.BodyWeightDao
import com.jlsh.aifit.testutil.*
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class BodyWeightRepositoryImplTest {

    private val apiService: BodyWeightApiService = mockk()
    private val dao: BodyWeightDao = mockk(relaxed = true)
    private val repository = BodyWeightRepositoryImpl(apiService, dao)

    // ─── getHistory ────────────────────────────────────────────────────────────

    @Test
    fun `getHistory emite Loading, luego cache, luego API fresh data`() = runTest {
        val cachedEntity = fakeBodyWeightEntity(id = "cached-1")
        coEvery { dao.getByDateRange(any(), any()) } returns listOf(cachedEntity)
        coEvery { apiService.getHistory(any(), any()) } returns ApiResponse(
            success = true,
            data = listOf(fakeBodyWeightLogResponseDto(id = "api-1")),
        )

        val emissions = repository.getHistory("2026-03-01", "2026-03-31").toList()

        assertTrue(emissions[0] is Result.Loading)
        assertTrue(emissions[1] is Result.Success) // cache
        assertTrue(emissions[2] is Result.Success) // API
        assertEquals("api-1", (emissions[2] as Result.Success).data.first().id)
    }

    @Test
    fun `getHistory cuando API falla y hay cache no emite error`() = runTest {
        val cachedEntity = fakeBodyWeightEntity(id = "cached-1")
        coEvery { dao.getByDateRange(any(), any()) } returns listOf(cachedEntity)
        coEvery { apiService.getHistory(any(), any()) } throws Exception("Network error")

        val emissions = repository.getHistory("2026-03-01", "2026-03-31").toList()

        assertTrue(emissions[0] is Result.Loading)
        assertTrue(emissions[1] is Result.Success) // cache
        // No error emission — silently swallowed because cache exists
        assertEquals(2, emissions.size)
    }

    @Test
    fun `getHistory cuando API falla y no hay cache emite error`() = runTest {
        coEvery { dao.getByDateRange(any(), any()) } returns emptyList()
        coEvery { apiService.getHistory(any(), any()) } throws Exception("Network error")

        val emissions = repository.getHistory("2026-03-01", "2026-03-31").toList()

        assertTrue(emissions[0] is Result.Loading)
        assertTrue(emissions[1] is Result.Error)
    }

    @Test
    fun `getHistory cuando API tiene éxito persiste datos en DAO`() = runTest {
        coEvery { dao.getByDateRange(any(), any()) } returns emptyList()
        coEvery { apiService.getHistory(any(), any()) } returns ApiResponse(
            success = true,
            data = listOf(fakeBodyWeightLogResponseDto()),
        )

        repository.getHistory("2026-03-01", "2026-03-31").toList()

        coVerify { dao.upsertAll(any()) }
    }

    // ─── logWeight ─────────────────────────────────────────────────────────────

    @Test
    fun `logWeight cuando API responde correctamente retorna Success y persiste en DAO`() = runTest {
        coEvery { apiService.logWeight(any()) } returns ApiResponse(
            success = true,
            data = fakeBodyWeightLogResponseDto(),
        )

        val result = repository.logWeight(fakeLogBodyWeightRequestDto())

        assertTrue(result is Result.Success)
        assertEquals("bw-1", (result as Result.Success).data.id)
        coVerify { dao.upsert(any()) }
    }

    @Test
    fun `logWeight cuando API falla retorna Error`() = runTest {
        coEvery { apiService.logWeight(any()) } throws Exception("Server error")

        val result = repository.logWeight(fakeLogBodyWeightRequestDto())

        assertTrue(result is Result.Error)
    }
}


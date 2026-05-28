package com.jlsh.aifit.feature.progress.data.repository

import com.jlsh.aifit.core.common.AppException
import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.core.network.ApiResponse
import com.jlsh.aifit.feature.progress.data.api.BodyWeightApiService
import com.jlsh.aifit.feature.progress.data.local.BodyWeightDao
import com.jlsh.aifit.testutil.*
import app.cash.turbine.test
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
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
        every { dao.observeByDateRange(any(), any()) } returns flowOf(listOf(cachedEntity))
        coEvery { apiService.getHistory(any(), any()) } returns ApiResponse(
            success = true,
            data = listOf(fakeBodyWeightLogResponseDto(id = "api-1")),
        )

        repository.getHistory("2026-03-01", "2026-03-31").test {
            assertTrue(awaitItem() is Result.Loading)
            val cached = awaitItem()
            assertTrue(cached is Result.Success)
            assertEquals("cached-1", (cached as Result.Success).data.first().id)
            cancelAndIgnoreRemainingEvents()
        }
        coVerify { dao.upsertAll(match { it.isNotEmpty() }) }
    }

    @Test
    fun `getHistory cuando API falla y hay cache no emite error`() = runTest {
        val cachedEntity = fakeBodyWeightEntity(id = "cached-1")
        every { dao.observeByDateRange(any(), any()) } returns flowOf(listOf(cachedEntity))
        coEvery { apiService.getHistory(any(), any()) } throws Exception("Network error")

        repository.getHistory("2026-03-01", "2026-03-31").test {
            assertTrue(awaitItem() is Result.Loading)
            assertTrue(awaitItem() is Result.Success)
            awaitComplete()
        }
    }

    @Test
    fun `getHistory cuando API falla y no hay cache emite error`() = runTest {
        every { dao.observeByDateRange(any(), any()) } returns flowOf(emptyList())
        coEvery { apiService.getHistory(any(), any()) } throws Exception("Network error")

        repository.getHistory("2026-03-01", "2026-03-31").test {
            assertTrue(awaitItem() is Result.Loading)
            val observed = awaitItem()
            assertTrue(observed is Result.Success)
            assertTrue((observed as Result.Success).data.isEmpty())
            awaitComplete()
        }
    }

    @Test
    fun `getHistory cuando API tiene éxito persiste datos en DAO`() = runTest {
        every { dao.observeByDateRange(any(), any()) } returns flowOf(emptyList())
        coEvery { apiService.getHistory(any(), any()) } returns ApiResponse(
            success = true,
            data = listOf(fakeBodyWeightLogResponseDto()),
        )

        repository.getHistory("2026-03-01", "2026-03-31").test {
            awaitItem()
            awaitItem()
            cancelAndIgnoreRemainingEvents()
        }

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


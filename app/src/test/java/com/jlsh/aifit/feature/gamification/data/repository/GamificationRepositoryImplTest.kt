package com.jlsh.aifit.feature.gamification.data.repository

import com.jlsh.aifit.core.common.AppException
import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.core.network.ApiResponse
import com.jlsh.aifit.feature.gamification.data.api.GamificationApiService
import com.jlsh.aifit.testutil.*
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class GamificationRepositoryImplTest {

    private val apiService: GamificationApiService = mockk()
    private lateinit var sut: GamificationRepositoryImpl

    @Before
    fun setUp() {
        sut = GamificationRepositoryImpl(apiService)
    }

    // ── getStreaks ──────────────────────────────────────────────────────────────

    @Test
    fun `getStreaks retorna Success cuando API responde correctamente`() = runTest {
        val dtos = listOf(fakeStreakResponseDto())
        coEvery { apiService.getStreaks() } returns ApiResponse(success = true, data = dtos)

        val result = sut.getStreaks()

        assertIs<Result.Success<*>>(result)
        assertEquals(1, (result as Result.Success).data.size)
    }

    @Test
    fun `getStreaks retorna Error cuando API falla`() = runTest {
        coEvery { apiService.getStreaks() } throws RuntimeException("Network error")

        val result = sut.getStreaks()

        assertIs<Result.Error>(result)
    }

    // ── getAchievements ────────────────────────────────────────────────────────

    @Test
    fun `getAchievements retorna Success cuando API responde correctamente`() = runTest {
        val dtos = listOf(fakeUserAchievementResponseDto())
        coEvery { apiService.getAchievements() } returns ApiResponse(success = true, data = dtos)

        val result = sut.getAchievements()

        assertIs<Result.Success<*>>(result)
        assertEquals(1, (result as Result.Success).data.size)
    }

    @Test
    fun `getAchievements retorna Error cuando API falla`() = runTest {
        coEvery { apiService.getAchievements() } throws RuntimeException("timeout")

        val result = sut.getAchievements()

        assertIs<Result.Error>(result)
    }

    // ── getAllDefinitions ───────────────────────────────────────────────────────

    @Test
    fun `getAllDefinitions retorna Success cuando API responde correctamente`() = runTest {
        val dtos = listOf(fakeAchievementDefinitionResponseDto())
        coEvery { apiService.getAllAchievementDefinitions() } returns ApiResponse(success = true, data = dtos)

        val result = sut.getAllDefinitions()

        assertIs<Result.Success<*>>(result)
        assertEquals(1, (result as Result.Success).data.size)
    }

    @Test
    fun `getAllDefinitions retorna Success local cuando API falla`() = runTest {
        coEvery { apiService.getAllAchievementDefinitions() } throws RuntimeException("error")

        val result = sut.getAllDefinitions()

        assertIs<Result.Success<*>>(result)
        assertTrue((result as Result.Success).data.isNotEmpty())
    }

    // ── getPersonalRecords ─────────────────────────────────────────────────────

    @Test
    fun `getPersonalRecords retorna Success cuando API responde correctamente`() = runTest {
        val dtos = listOf(fakePersonalRecordResponseDto())
        coEvery { apiService.getPersonalRecords() } returns ApiResponse(success = true, data = dtos)

        val result = sut.getPersonalRecords()

        assertIs<Result.Success<*>>(result)
        assertEquals(1, (result as Result.Success).data.size)
    }

    @Test
    fun `getPersonalRecords retorna Error cuando API falla`() = runTest {
        coEvery { apiService.getPersonalRecords() } throws RuntimeException("error")

        val result = sut.getPersonalRecords()

        assertIs<Result.Error>(result)
    }

    // ── getExport ──────────────────────────────────────────────────────────────

    @Test
    fun `getExport retorna Success cuando API responde correctamente`() = runTest {
        val dto = fakeProgressExportResponseDto()
        coEvery { apiService.getExport("LAST_MONTH") } returns ApiResponse(success = true, data = dto)

        val result = sut.getExport("LAST_MONTH")

        assertIs<Result.Success<*>>(result)
        assertEquals("user-1", (result as Result.Success).data.userId)
    }

    @Test
    fun `getExport retorna Error cuando API falla`() = runTest {
        coEvery { apiService.getExport(any()) } throws RuntimeException("error")

        val result = sut.getExport("LAST_MONTH")

        assertIs<Result.Error>(result)
    }

    @Test
    fun `getExport retorna Error cuando API devuelve success false`() = runTest {
        coEvery { apiService.getExport(any()) } returns ApiResponse(
            success = false,
            data = null,
            message = "Server error",
        )

        val result = sut.getExport("ALL_TIME")

        assertIs<Result.Error>(result)
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    private inline fun <reified T> assertIs(value: Any?) {
        assertTrue(
            "Expected ${T::class.simpleName} but was ${value?.let { it::class.simpleName }}",
            value is T,
        )
    }
}


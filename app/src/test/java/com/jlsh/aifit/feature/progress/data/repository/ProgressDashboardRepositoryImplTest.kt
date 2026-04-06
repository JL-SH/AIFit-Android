package com.jlsh.aifit.feature.progress.data.repository

import com.jlsh.aifit.core.common.AppException
import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.core.network.ApiResponse
import com.jlsh.aifit.feature.progress.data.api.ProgressDashboardApiService
import com.jlsh.aifit.testutil.*
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class ProgressDashboardRepositoryImplTest {

    private val apiService: ProgressDashboardApiService = mockk()
    private val repository = ProgressDashboardRepositoryImpl(apiService)

    // ─── getDashboard ──────────────────────────────────────────────────────────

    @Test
    fun `getDashboard cuando API responde correctamente retorna Success con dashboard`() = runTest {
        coEvery { apiService.getDashboard(any(), any()) } returns ApiResponse(
            success = true,
            data = fakeProgressDashboardResponseDto(),
        )

        val result = repository.getDashboard("2026-03-01", "2026-03-31")

        assertTrue(result is Result.Success)
        val dashboard = (result as Result.Success).data
        assertEquals(12, dashboard.workoutAdherence.plannedSessions)
    }

    @Test
    fun `getDashboard cuando API falla retorna Error`() = runTest {
        coEvery { apiService.getDashboard(any(), any()) } throws Exception("Network error")

        val result = repository.getDashboard("2026-03-01", "2026-03-31")

        assertTrue(result is Result.Error)
    }

    // ─── getWeeklySummary ──────────────────────────────────────────────────────

    @Test
    fun `getWeeklySummary cuando API responde correctamente retorna Success`() = runTest {
        coEvery { apiService.getWeeklySummary() } returns ApiResponse(
            success = true,
            data = fakeWeeklyProgressSummaryResponseDto(),
        )

        val result = repository.getWeeklySummary()

        assertTrue(result is Result.Success)
        val summary = (result as Result.Success).data
        assertEquals(3, summary.workoutsThisWeek)
    }

    @Test
    fun `getWeeklySummary cuando API falla retorna Error`() = runTest {
        coEvery { apiService.getWeeklySummary() } throws Exception("Server error")

        val result = repository.getWeeklySummary()

        assertTrue(result is Result.Error)
    }
}


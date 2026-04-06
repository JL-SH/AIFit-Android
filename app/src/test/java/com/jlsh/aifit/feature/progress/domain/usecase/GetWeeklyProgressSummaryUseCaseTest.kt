package com.jlsh.aifit.feature.progress.domain.usecase

import com.jlsh.aifit.core.common.AppException
import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.feature.progress.domain.repository.ProgressDashboardRepository
import com.jlsh.aifit.testutil.fakeWeeklyProgressSummary
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class GetWeeklyProgressSummaryUseCaseTest {

    private val repository: ProgressDashboardRepository = mockk()
    private val useCase = GetWeeklyProgressSummaryUseCase(repository)

    @Test
    fun `cuando repo retorna Success, useCase retorna Success con summary`() = runTest {
        val summary = fakeWeeklyProgressSummary()
        coEvery { repository.getWeeklySummary() } returns Result.Success(summary)

        val result = useCase()

        assertTrue(result is Result.Success)
        assertEquals(summary, (result as Result.Success).data)
    }

    @Test
    fun `cuando repo retorna Error, useCase retorna Error`() = runTest {
        coEvery { repository.getWeeklySummary() } returns
                Result.Error(AppException.ServerException)

        val result = useCase()

        assertTrue(result is Result.Error)
    }
}


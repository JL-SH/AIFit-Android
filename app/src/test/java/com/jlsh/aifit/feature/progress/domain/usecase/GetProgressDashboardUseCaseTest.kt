package com.jlsh.aifit.feature.progress.domain.usecase

import com.jlsh.aifit.core.common.AppException
import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.feature.progress.domain.repository.ProgressDashboardRepository
import com.jlsh.aifit.testutil.fakeProgressDashboard
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class GetProgressDashboardUseCaseTest {

    private val repository: ProgressDashboardRepository = mockk()
    private val useCase = GetProgressDashboardUseCase(repository)

    @Test
    fun `cuando repo retorna Success, useCase retorna Success con dashboard`() = runTest {
        val dashboard = fakeProgressDashboard()
        coEvery { repository.getDashboard(any(), any()) } returns Result.Success(dashboard)

        val result = useCase("2026-03-01", "2026-03-31")

        assertTrue(result is Result.Success)
        assertEquals(dashboard, (result as Result.Success).data)
    }

    @Test
    fun `cuando repo retorna Error, useCase retorna Error`() = runTest {
        coEvery { repository.getDashboard(any(), any()) } returns
                Result.Error(AppException.NetworkException)

        val result = useCase("2026-03-01", "2026-03-31")

        assertTrue(result is Result.Error)
    }
}


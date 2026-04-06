package com.jlsh.aifit.feature.nutrition.domain.usecase

import com.jlsh.aifit.core.common.AppException
import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.feature.nutrition.domain.repository.NutritionLogRepository
import com.jlsh.aifit.testutil.fakeNutritionLog
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class GetNutritionHistoryUseCaseTest {

    private val repository: NutritionLogRepository = mockk()
    private val sut = GetNutritionHistoryUseCase(repository)

    @Test
    fun `invoke retorna Success con lista de logs`() = runTest {
        val logs = listOf(fakeNutritionLog())
        coEvery { repository.getNutritionHistory(any(), any()) } returns Result.Success(logs)

        val result = sut("2026-04-01", "2026-04-06")

        assertTrue(result is Result.Success)
        assertEquals(1, (result as Result.Success).data.size)
    }

    @Test
    fun `invoke retorna Error cuando repository falla`() = runTest {
        coEvery { repository.getNutritionHistory(any(), any()) } returns
            Result.Error(AppException.NetworkException)

        val result = sut("2026-04-01", "2026-04-06")

        assertTrue(result is Result.Error)
    }
}


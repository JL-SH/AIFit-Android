package com.jlsh.aifit.feature.metabolic.domain.usecase

import com.jlsh.aifit.core.common.AppException
import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.feature.metabolic.domain.repository.MetabolicRepository
import com.jlsh.aifit.testutil.*
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class GetMetabolicInsightsUseCaseTest {

    private val repository: MetabolicRepository = mockk()
    private val useCase = GetMetabolicInsightsUseCase(repository)

    @Test
    fun `invoke retorna Success con lista de insights`() = runTest {
        val insights = listOf(fakeMetabolicInsight(), fakeMetabolicInsight(id = "insight-2"))
        coEvery { repository.getInsights() } returns Result.Success(insights)

        val result = useCase()

        assertTrue(result is Result.Success)
        assertEquals(2, (result as Result.Success).data.size)
    }

    @Test
    fun `invoke retorna Error cuando repository falla`() = runTest {
        coEvery { repository.getInsights() } returns Result.Error(AppException.ServerException)

        val result = useCase()

        assertTrue(result is Result.Error)
    }
}


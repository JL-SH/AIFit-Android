package com.jlsh.aifit.feature.progression.domain.usecase

import com.jlsh.aifit.core.common.AppException
import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.feature.progression.domain.repository.ProgressionRepository
import com.jlsh.aifit.testutil.fakePlanProgressionSummary
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class GetFullPlanProgressionRecommendationsUseCaseTest {

    private val repository: ProgressionRepository = mockk()
    private val useCase = GetFullPlanProgressionRecommendationsUseCase(repository)

    @Test
    fun `cuando repo retorna Success, useCase retorna Success`() = runTest {
        val summary = fakePlanProgressionSummary()
        coEvery { repository.getPlanRecommendations(any()) } returns Result.Success(summary)

        val result = useCase("plan-1")

        assertTrue(result is Result.Success)
        assertEquals(summary, (result as Result.Success).data)
    }

    @Test
    fun `cuando repo retorna Error, useCase retorna Error`() = runTest {
        coEvery { repository.getPlanRecommendations(any()) } returns
                Result.Error(AppException.ServerException)

        val result = useCase("plan-1")

        assertTrue(result is Result.Error)
    }
}


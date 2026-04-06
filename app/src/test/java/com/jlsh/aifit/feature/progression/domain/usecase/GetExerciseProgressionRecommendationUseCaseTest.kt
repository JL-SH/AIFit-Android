package com.jlsh.aifit.feature.progression.domain.usecase

import com.jlsh.aifit.core.common.AppException
import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.feature.progression.domain.repository.ProgressionRepository
import com.jlsh.aifit.testutil.fakeProgressionRecommendation
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class GetExerciseProgressionRecommendationUseCaseTest {

    private val repository: ProgressionRepository = mockk()
    private val useCase = GetExerciseProgressionRecommendationUseCase(repository)

    @Test
    fun `cuando repo retorna Success, useCase retorna Success`() = runTest {
        val recommendation = fakeProgressionRecommendation()
        coEvery { repository.getExerciseRecommendation(any()) } returns Result.Success(recommendation)

        val result = useCase("exercise-1")

        assertTrue(result is Result.Success)
        assertEquals(recommendation, (result as Result.Success).data)
    }

    @Test
    fun `cuando repo retorna Error, useCase retorna Error`() = runTest {
        coEvery { repository.getExerciseRecommendation(any()) } returns
                Result.Error(AppException.NetworkException)

        val result = useCase("exercise-1")

        assertTrue(result is Result.Error)
    }
}


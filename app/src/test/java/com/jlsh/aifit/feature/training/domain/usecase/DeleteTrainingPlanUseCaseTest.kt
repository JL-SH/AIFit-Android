package com.jlsh.aifit.feature.training.domain.usecase

import com.jlsh.aifit.core.common.AppException
import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.feature.training.domain.repository.TrainingRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class DeleteTrainingPlanUseCaseTest {

    private val repository: TrainingRepository = mockk()
    private val sut = DeleteTrainingPlanUseCase(repository)

    @Test
    fun `invoke retorna Success cuando delete tiene éxito`() = runTest {
        coEvery { repository.deleteTrainingPlan("plan-1") } returns Result.Success(Unit)

        val result = sut("plan-1")

        assertTrue(result is Result.Success)
    }

    @Test
    fun `invoke retorna Error cuando delete falla`() = runTest {
        coEvery { repository.deleteTrainingPlan(any()) } returns
            Result.Error(AppException.ServerException)

        val result = sut("plan-1")

        assertTrue(result is Result.Error)
    }
}


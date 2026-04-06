package com.jlsh.aifit.feature.training.domain.usecase

import app.cash.turbine.test
import com.jlsh.aifit.core.common.AppException
import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.feature.training.domain.repository.TrainingRepository
import com.jlsh.aifit.testutil.fakeTrainingPlan
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class GetTrainingPlansUseCaseTest {

    private val repository: TrainingRepository = mockk()
    private val sut = GetTrainingPlansUseCase(repository)

    @Test
    fun `invoke delega en repository y retorna lista de planes`() = runTest {
        val plans = listOf(fakeTrainingPlan())
        every { repository.getTrainingPlans() } returns flowOf(Result.Success(plans))

        sut().test {
            val result = awaitItem()
            assertTrue(result is Result.Success)
            assertEquals(plans, (result as Result.Success).data)
            awaitComplete()
        }
    }

    @Test
    fun `invoke retorna Error cuando repository falla`() = runTest {
        every { repository.getTrainingPlans() } returns flowOf(
            Result.Error(AppException.NetworkException)
        )

        sut().test {
            val result = awaitItem()
            assertTrue(result is Result.Error)
            awaitComplete()
        }
    }
}


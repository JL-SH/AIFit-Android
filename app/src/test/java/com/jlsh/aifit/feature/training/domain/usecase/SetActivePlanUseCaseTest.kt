package com.jlsh.aifit.feature.training.domain.usecase

import com.jlsh.aifit.core.common.AppException
import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.feature.training.domain.repository.TrainingRepository
import com.jlsh.aifit.testutil.fakeTrainingPlan
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class SetActivePlanUseCaseTest {

    private val repository: TrainingRepository = mockk()
    private val sut = SetActivePlanUseCase(repository)

    @Test
    fun `invoke retorna plan activado cuando tiene éxito`() = runTest {
        val plan = fakeTrainingPlan(id = "p-1")
        coEvery { repository.activatePlan("p-1") } returns Result.Success(plan)

        val result = sut("p-1")

        assertTrue(result is Result.Success)
        assertEquals("p-1", (result as Result.Success).data.id)
    }

    @Test
    fun `invoke retorna Error cuando falla`() = runTest {
        coEvery { repository.activatePlan(any()) } returns
            Result.Error(AppException.NotFoundException("Plan"))

        val result = sut("bad-id")

        assertTrue(result is Result.Error)
    }
}


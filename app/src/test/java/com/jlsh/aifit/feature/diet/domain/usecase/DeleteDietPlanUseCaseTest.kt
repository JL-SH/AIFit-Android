package com.jlsh.aifit.feature.diet.domain.usecase

import com.jlsh.aifit.core.common.AppException
import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.feature.diet.domain.repository.DietRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class DeleteDietPlanUseCaseTest {

    private val repository: DietRepository = mockk()
    private val sut = DeleteDietPlanUseCase(repository)

    @Test
    fun `invoke retorna Success cuando delete tiene éxito`() = runTest {
        coEvery { repository.deleteDietPlan("dp-1") } returns Result.Success(Unit)

        val result = sut("dp-1")

        assertTrue(result is Result.Success)
    }

    @Test
    fun `invoke retorna Error cuando delete falla`() = runTest {
        coEvery { repository.deleteDietPlan(any()) } returns
            Result.Error(AppException.ServerException)

        val result = sut("dp-1")

        assertTrue(result is Result.Error)
    }
}


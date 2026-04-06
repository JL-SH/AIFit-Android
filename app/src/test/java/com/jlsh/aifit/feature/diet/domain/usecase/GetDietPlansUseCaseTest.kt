package com.jlsh.aifit.feature.diet.domain.usecase

import app.cash.turbine.test
import com.jlsh.aifit.core.common.AppException
import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.feature.diet.domain.repository.DietRepository
import com.jlsh.aifit.testutil.fakeDietPlan
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class GetDietPlansUseCaseTest {

    private val repository: DietRepository = mockk()
    private val sut = GetDietPlansUseCase(repository)

    @Test
    fun `invoke delega en repository y retorna lista de planes`() = runTest {
        val plans = listOf(fakeDietPlan())
        every { repository.getDietPlans() } returns flowOf(Result.Success(plans))

        sut().test {
            val result = awaitItem()
            assertTrue(result is Result.Success)
            assertEquals(plans, (result as Result.Success).data)
            awaitComplete()
        }
    }

    @Test
    fun `invoke retorna Error cuando repository falla`() = runTest {
        every { repository.getDietPlans() } returns flowOf(
            Result.Error(AppException.NetworkException)
        )

        sut().test {
            val result = awaitItem()
            assertTrue(result is Result.Error)
            awaitComplete()
        }
    }
}


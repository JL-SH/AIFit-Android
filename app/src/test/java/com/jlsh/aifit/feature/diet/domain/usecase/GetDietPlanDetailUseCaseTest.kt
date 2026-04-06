package com.jlsh.aifit.feature.diet.domain.usecase

import com.jlsh.aifit.core.common.AppException
import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.feature.diet.domain.repository.DietRepository
import com.jlsh.aifit.testutil.fakeDietPlan
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class GetDietPlanDetailUseCaseTest {

    private val repository: DietRepository = mockk()
    private val sut = GetDietPlanDetailUseCase(repository)

    @Test
    fun `invoke retorna plan cuando repository tiene éxito`() = runTest {
        val plan = fakeDietPlan(id = "dp-1")
        coEvery { repository.getDietPlanDetail("dp-1") } returns Result.Success(plan)

        val result = sut("dp-1")

        assertTrue(result is Result.Success)
        assertEquals("dp-1", (result as Result.Success).data.id)
    }

    @Test
    fun `invoke retorna Error cuando repository falla`() = runTest {
        coEvery { repository.getDietPlanDetail(any()) } returns
            Result.Error(AppException.NotFoundException("DietPlan"))

        val result = sut("bad-id")

        assertTrue(result is Result.Error)
    }
}


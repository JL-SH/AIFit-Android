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

class ApplyMetabolicAdjustmentUseCaseTest {

    private val repository: MetabolicRepository = mockk()
    private val useCase = ApplyMetabolicAdjustmentUseCase(repository)

    @Test
    fun `invoke retorna Success con NutritionTarget`() = runTest {
        val request = fakeApplyMetabolicAdjustmentRequestDto()
        val target = fakeNutritionTarget()
        coEvery { repository.applyAdjustment(request) } returns Result.Success(target)

        val result = useCase(request)

        assertTrue(result is Result.Success)
        assertEquals(target, (result as Result.Success).data)
    }

    @Test
    fun `invoke retorna Error cuando repository falla`() = runTest {
        val request = fakeApplyMetabolicAdjustmentRequestDto()
        coEvery { repository.applyAdjustment(request) } returns Result.Error(AppException.ServerException)

        val result = useCase(request)

        assertTrue(result is Result.Error)
    }
}


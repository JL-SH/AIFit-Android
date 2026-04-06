package com.jlsh.aifit.feature.nutrition.domain.usecase

import com.jlsh.aifit.core.common.AppException
import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.feature.nutrition.domain.repository.NutritionTargetRepository
import com.jlsh.aifit.testutil.fakeNutritionTarget
import com.jlsh.aifit.testutil.fakeUpdateNutritionTargetRequestDto
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class UpdateNutritionTargetUseCaseTest {

    private val repository: NutritionTargetRepository = mockk()
    private val sut = UpdateNutritionTargetUseCase(repository)

    @Test
    fun `invoke retorna Success con target actualizado`() = runTest {
        val request = fakeUpdateNutritionTargetRequestDto()
        val target = fakeNutritionTarget(calorieTarget = 2500)
        coEvery { repository.updateTarget(request) } returns Result.Success(target)

        val result = sut(request)

        assertTrue(result is Result.Success)
        assertEquals(2500, (result as Result.Success).data.calorieTarget)
    }

    @Test
    fun `invoke retorna Error cuando repository falla`() = runTest {
        val request = fakeUpdateNutritionTargetRequestDto()
        coEvery { repository.updateTarget(request) } returns
            Result.Error(AppException.ServerException)

        val result = sut(request)

        assertTrue(result is Result.Error)
    }
}


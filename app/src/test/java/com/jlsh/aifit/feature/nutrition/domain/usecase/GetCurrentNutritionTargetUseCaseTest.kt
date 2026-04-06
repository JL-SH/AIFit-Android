package com.jlsh.aifit.feature.nutrition.domain.usecase

import com.jlsh.aifit.core.common.AppException
import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.feature.nutrition.domain.repository.NutritionTargetRepository
import com.jlsh.aifit.testutil.fakeNutritionTarget
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class GetCurrentNutritionTargetUseCaseTest {

    private val repository: NutritionTargetRepository = mockk()
    private val sut = GetCurrentNutritionTargetUseCase(repository)

    @Test
    fun `invoke delega en repository y retorna Flow con Success`() = runTest {
        val target = fakeNutritionTarget()
        every { repository.getCurrentTarget() } returns flowOf(Result.Success(target))

        val result = sut().toList()

        assertEquals(1, result.size)
        assertTrue(result[0] is Result.Success)
        assertEquals(target, (result[0] as Result.Success).data)
    }

    @Test
    fun `invoke retorna Error cuando repository falla`() = runTest {
        every { repository.getCurrentTarget() } returns flowOf(
            Result.Error(AppException.NetworkException),
        )

        val result = sut().toList()

        assertEquals(1, result.size)
        assertTrue(result[0] is Result.Error)
    }
}


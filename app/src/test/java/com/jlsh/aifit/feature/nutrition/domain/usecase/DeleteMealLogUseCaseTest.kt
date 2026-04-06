package com.jlsh.aifit.feature.nutrition.domain.usecase

import com.jlsh.aifit.core.common.AppException
import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.feature.nutrition.domain.repository.NutritionLogRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class DeleteMealLogUseCaseTest {

    private val repository: NutritionLogRepository = mockk()
    private val sut = DeleteMealLogUseCase(repository)

    @Test
    fun `invoke retorna Success cuando la eliminación es exitosa`() = runTest {
        coEvery { repository.deleteMealLog("meal-1") } returns Result.Success(Unit)

        val result = sut("meal-1")

        assertTrue(result is Result.Success)
    }

    @Test
    fun `invoke retorna Error cuando repository falla`() = runTest {
        coEvery { repository.deleteMealLog(any()) } returns
            Result.Error(AppException.NetworkException)

        val result = sut("meal-1")

        assertTrue(result is Result.Error)
    }
}


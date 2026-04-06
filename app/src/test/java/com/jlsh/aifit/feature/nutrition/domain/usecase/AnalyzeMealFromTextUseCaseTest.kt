package com.jlsh.aifit.feature.nutrition.domain.usecase

import com.jlsh.aifit.core.common.AppException
import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.feature.nutrition.domain.repository.NutritionLogRepository
import com.jlsh.aifit.testutil.fakeAnalyzeMealFromTextRequestDto
import com.jlsh.aifit.testutil.fakeMealLog
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class AnalyzeMealFromTextUseCaseTest {

    private val repository: NutritionLogRepository = mockk()
    private val sut = AnalyzeMealFromTextUseCase(repository)

    @Test
    fun `invoke retorna Success con MealLog analizado`() = runTest {
        val meal = fakeMealLog(aiGenerated = true, rawInputText = "chicken and rice")
        val request = fakeAnalyzeMealFromTextRequestDto()
        coEvery { repository.analyzeMealFromText(request) } returns Result.Success(meal)

        val result = sut(request)

        assertTrue(result is Result.Success)
        assertTrue((result as Result.Success).data.aiGenerated)
    }

    @Test
    fun `invoke retorna Error cuando repository falla`() = runTest {
        val request = fakeAnalyzeMealFromTextRequestDto()
        coEvery { repository.analyzeMealFromText(request) } returns
            Result.Error(AppException.ServerException)

        val result = sut(request)

        assertTrue(result is Result.Error)
    }
}


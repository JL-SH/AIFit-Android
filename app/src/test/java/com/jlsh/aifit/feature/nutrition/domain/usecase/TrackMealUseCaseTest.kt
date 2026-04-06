package com.jlsh.aifit.feature.nutrition.domain.usecase

import com.jlsh.aifit.core.common.AppException
import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.feature.nutrition.domain.repository.NutritionLogRepository
import com.jlsh.aifit.testutil.fakeMealLog
import com.jlsh.aifit.testutil.fakeTrackMealRequestDto
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class TrackMealUseCaseTest {

    private val repository: NutritionLogRepository = mockk()
    private val sut = TrackMealUseCase(repository)

    @Test
    fun `invoke retorna Success con MealLog guardado`() = runTest {
        val meal = fakeMealLog()
        val request = fakeTrackMealRequestDto()
        coEvery { repository.trackMeal(request) } returns Result.Success(meal)

        val result = sut(request)

        assertTrue(result is Result.Success)
        assertEquals(meal, (result as Result.Success).data)
    }

    @Test
    fun `invoke retorna Error cuando repository falla`() = runTest {
        val request = fakeTrackMealRequestDto()
        coEvery { repository.trackMeal(request) } returns
            Result.Error(AppException.ServerException)

        val result = sut(request)

        assertTrue(result is Result.Error)
    }
}


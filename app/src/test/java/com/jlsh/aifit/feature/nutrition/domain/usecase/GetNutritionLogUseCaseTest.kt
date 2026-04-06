package com.jlsh.aifit.feature.nutrition.domain.usecase

import com.jlsh.aifit.core.common.AppException
import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.feature.nutrition.domain.repository.NutritionLogRepository
import com.jlsh.aifit.testutil.fakeNutritionLog
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate

class GetNutritionLogUseCaseTest {

    private val repository: NutritionLogRepository = mockk()
    private val sut = GetNutritionLogUseCase(repository)

    @Test
    fun `invoke delega en repository y retorna su Flow con Success`() = runTest {
        val log = fakeNutritionLog()
        every { repository.getNutritionLog(any()) } returns flowOf(Result.Success(log))

        val result = sut(LocalDate.of(2026, 4, 6)).toList()

        assertEquals(1, result.size)
        assertTrue(result[0] is Result.Success)
        assertEquals(log, (result[0] as Result.Success).data)
    }

    @Test
    fun `invoke retorna Error cuando repository falla`() = runTest {
        every { repository.getNutritionLog(any()) } returns flowOf(
            Result.Error(AppException.NetworkException),
        )

        val result = sut(LocalDate.of(2026, 4, 6)).toList()

        assertEquals(1, result.size)
        assertTrue(result[0] is Result.Error)
    }
}


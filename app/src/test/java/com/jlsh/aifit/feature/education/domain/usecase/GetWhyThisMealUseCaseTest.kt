package com.jlsh.aifit.feature.education.domain.usecase

import com.jlsh.aifit.core.common.AppException
import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.feature.education.domain.repository.EducationRepository
import com.jlsh.aifit.testutil.fakeWhyThisExplanation
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class GetWhyThisMealUseCaseTest {

    private val repository: EducationRepository = mockk()
    private val useCase = GetWhyThisMealUseCase(repository)

    @Test
    fun `cuando repo retorna Success, useCase retorna Success`() = runTest {
        val explanation = fakeWhyThisExplanation()
        coEvery { repository.getWhyThisMeal(any()) } returns Result.Success(explanation)

        val result = useCase("meal-1")

        assertTrue(result is Result.Success)
        assertEquals(explanation, (result as Result.Success).data)
    }

    @Test
    fun `cuando repo retorna Error, useCase retorna Error`() = runTest {
        coEvery { repository.getWhyThisMeal(any()) } returns
                Result.Error(AppException.NetworkException)

        val result = useCase("meal-1")

        assertTrue(result is Result.Error)
    }
}


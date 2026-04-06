package com.jlsh.aifit.feature.education.domain.usecase

import com.jlsh.aifit.core.common.AppException
import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.feature.education.domain.repository.EducationRepository
import com.jlsh.aifit.testutil.fakeGlossaryDefinition
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class GetGlossaryTermUseCaseTest {

    private val repository: EducationRepository = mockk()
    private val useCase = GetGlossaryTermUseCase(repository)

    @Test
    fun `cuando repo retorna Success, useCase retorna Success`() = runTest {
        val definition = fakeGlossaryDefinition()
        coEvery { repository.getGlossaryTerm(any()) } returns Result.Success(definition)

        val result = useCase("Hypertrophy")

        assertTrue(result is Result.Success)
        assertEquals(definition, (result as Result.Success).data)
    }

    @Test
    fun `cuando repo retorna Error, useCase retorna Error`() = runTest {
        coEvery { repository.getGlossaryTerm(any()) } returns
                Result.Error(AppException.NetworkException)

        val result = useCase("Hypertrophy")

        assertTrue(result is Result.Error)
    }
}


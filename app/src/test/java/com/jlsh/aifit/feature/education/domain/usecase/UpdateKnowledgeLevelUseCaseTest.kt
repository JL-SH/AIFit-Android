package com.jlsh.aifit.feature.education.domain.usecase

import com.jlsh.aifit.core.common.AppException
import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.feature.education.domain.repository.EducationRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class UpdateKnowledgeLevelUseCaseTest {

    private val repository: EducationRepository = mockk()
    private val useCase = UpdateKnowledgeLevelUseCase(repository)

    @Test
    fun `cuando repo retorna Success, useCase retorna Success`() = runTest {
        coEvery { repository.updateKnowledgeLevel(any()) } returns Result.Success("INTERMEDIATE")

        val result = useCase("INTERMEDIATE")

        assertTrue(result is Result.Success)
        assertEquals("INTERMEDIATE", (result as Result.Success).data)
    }

    @Test
    fun `cuando repo retorna Error, useCase retorna Error`() = runTest {
        coEvery { repository.updateKnowledgeLevel(any()) } returns
                Result.Error(AppException.ServerException)

        val result = useCase("INTERMEDIATE")

        assertTrue(result is Result.Error)
    }
}


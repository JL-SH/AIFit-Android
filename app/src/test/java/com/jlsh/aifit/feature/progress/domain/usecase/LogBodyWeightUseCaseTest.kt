package com.jlsh.aifit.feature.progress.domain.usecase

import com.jlsh.aifit.core.common.AppException
import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.feature.progress.domain.repository.BodyWeightRepository
import com.jlsh.aifit.testutil.fakeBodyWeightLog
import com.jlsh.aifit.testutil.fakeLogBodyWeightRequestDto
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class LogBodyWeightUseCaseTest {

    private val repository: BodyWeightRepository = mockk()
    private val useCase = LogBodyWeightUseCase(repository)

    @Test
    fun `cuando repo retorna Success, useCase retorna Success con log`() = runTest {
        val log = fakeBodyWeightLog()
        coEvery { repository.logWeight(any()) } returns Result.Success(log)

        val result = useCase(fakeLogBodyWeightRequestDto())

        assertTrue(result is Result.Success)
        assertEquals(log, (result as Result.Success).data)
    }

    @Test
    fun `cuando repo retorna Error, useCase retorna Error`() = runTest {
        coEvery { repository.logWeight(any()) } returns
                Result.Error(AppException.ServerException)

        val result = useCase(fakeLogBodyWeightRequestDto())

        assertTrue(result is Result.Error)
    }
}


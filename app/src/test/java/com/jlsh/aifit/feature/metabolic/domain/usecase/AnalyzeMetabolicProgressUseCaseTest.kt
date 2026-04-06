package com.jlsh.aifit.feature.metabolic.domain.usecase

import com.jlsh.aifit.core.common.AppException
import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.feature.metabolic.domain.repository.MetabolicRepository
import com.jlsh.aifit.testutil.*
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class AnalyzeMetabolicProgressUseCaseTest {

    private val repository: MetabolicRepository = mockk()
    private val useCase = AnalyzeMetabolicProgressUseCase(repository)

    @Test
    fun `invoke retorna Success cuando repository responde ok`() = runTest {
        val analysis = fakeMetabolicAnalysis()
        coEvery { repository.analyzeMetabolicProgress() } returns Result.Success(analysis)

        val result = useCase()

        assertTrue(result is Result.Success)
        assertEquals(analysis, (result as Result.Success).data)
    }

    @Test
    fun `invoke retorna Error cuando repository falla`() = runTest {
        coEvery { repository.analyzeMetabolicProgress() } returns Result.Error(AppException.ServerException)

        val result = useCase()

        assertTrue(result is Result.Error)
    }
}


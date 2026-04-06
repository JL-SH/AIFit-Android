package com.jlsh.aifit.feature.progress.domain.usecase

import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.feature.progress.domain.repository.BodyWeightRepository
import com.jlsh.aifit.testutil.fakeBodyWeightLog
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class GetBodyWeightHistoryUseCaseTest {

    private val repository: BodyWeightRepository = mockk()
    private val useCase = GetBodyWeightHistoryUseCase(repository)

    @Test
    fun `cuando repo emite Success, useCase retorna la misma emisión`() = runTest {
        val logs = listOf(fakeBodyWeightLog())
        every { repository.getHistory(any(), any()) } returns
                flowOf(Result.Loading, Result.Success(logs))

        val emissions = useCase("2026-03-01", "2026-03-31").toList()

        assertEquals(2, emissions.size)
        assertTrue(emissions[0] is Result.Loading)
        assertTrue(emissions[1] is Result.Success)
        assertEquals(logs, (emissions[1] as Result.Success).data)
    }
}


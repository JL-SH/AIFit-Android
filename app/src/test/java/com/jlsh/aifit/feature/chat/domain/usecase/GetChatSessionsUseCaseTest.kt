package com.jlsh.aifit.feature.chat.domain.usecase

import app.cash.turbine.test
import com.jlsh.aifit.core.common.AppException
import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.feature.chat.domain.repository.ChatRepository
import com.jlsh.aifit.testutil.*
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class GetChatSessionsUseCaseTest {

    private val repository: ChatRepository = mockk()
    private val useCase = GetChatSessionsUseCase(repository)

    @Test
    fun `invoke retorna Flow con Success cuando repository emite sesiones`() = runTest {
        val sessions = listOf(fakeChatSession(), fakeChatSession(id = "session-2"))
        every { repository.getSessions() } returns flowOf(Result.Success(sessions))

        useCase().test {
            val result = awaitItem()
            assertTrue(result is Result.Success)
            assertEquals(2, (result as Result.Success).data.size)
            awaitComplete()
        }
    }

    @Test
    fun `invoke retorna Flow con Error cuando repository falla`() = runTest {
        every { repository.getSessions() } returns flowOf(Result.Error(AppException.NetworkException))

        useCase().test {
            val result = awaitItem()
            assertTrue(result is Result.Error)
            awaitComplete()
        }
    }
}


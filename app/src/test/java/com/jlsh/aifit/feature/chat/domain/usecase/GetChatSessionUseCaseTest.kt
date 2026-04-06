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

class GetChatSessionUseCaseTest {

    private val repository: ChatRepository = mockk()
    private val useCase = GetChatSessionUseCase(repository)

    @Test
    fun `invoke retorna Flow con Success cuando repository emite sesion`() = runTest {
        val session = fakeChatSession()
        every { repository.getSession("session-1") } returns flowOf(Result.Success(session))

        useCase("session-1").test {
            val result = awaitItem()
            assertTrue(result is Result.Success)
            assertEquals("session-1", (result as Result.Success).data.id)
            awaitComplete()
        }
    }

    @Test
    fun `invoke retorna Flow con Error cuando repository falla`() = runTest {
        every { repository.getSession("session-1") } returns flowOf(Result.Error(AppException.ServerException))

        useCase("session-1").test {
            val result = awaitItem()
            assertTrue(result is Result.Error)
            awaitComplete()
        }
    }
}


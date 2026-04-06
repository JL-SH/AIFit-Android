package com.jlsh.aifit.feature.chat.domain.usecase

import com.jlsh.aifit.core.common.AppException
import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.feature.chat.domain.repository.ChatRepository
import com.jlsh.aifit.testutil.*
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class SendChatMessageUseCaseTest {

    private val repository: ChatRepository = mockk()
    private val useCase = SendChatMessageUseCase(repository)

    @Test
    fun `invoke retorna Success con mensaje de respuesta`() = runTest {
        val message = fakeChatMessage(id = "msg-response", content = "Here is my answer")
        coEvery { repository.sendMessage("session-1", "Hello") } returns Result.Success(message)

        val result = useCase("session-1", "Hello")

        assertTrue(result is Result.Success)
        assertEquals("msg-response", (result as Result.Success).data.id)
    }

    @Test
    fun `invoke retorna Error cuando repository falla`() = runTest {
        coEvery { repository.sendMessage("session-1", "Hello") } returns Result.Error(AppException.NetworkException)

        val result = useCase("session-1", "Hello")

        assertTrue(result is Result.Error)
    }
}


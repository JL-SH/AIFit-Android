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

class StartChatSessionUseCaseTest {

    private val repository: ChatRepository = mockk()
    private val useCase = StartChatSessionUseCase(repository)

    @Test
    fun `invoke retorna Success con sesion creada`() = runTest {
        val session = fakeChatSession()
        coEvery { repository.startSession(any()) } returns Result.Success(session)

        val result = useCase()

        assertTrue(result is Result.Success)
        assertEquals("session-1", (result as Result.Success).data.id)
    }

    @Test
    fun `invoke retorna Error cuando repository falla`() = runTest {
        coEvery { repository.startSession(any()) } returns Result.Error(AppException.ServerException)

        val result = useCase()

        assertTrue(result is Result.Error)
    }
}


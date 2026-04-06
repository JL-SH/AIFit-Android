package com.jlsh.aifit.feature.chat.domain.usecase

import com.jlsh.aifit.core.common.AppException
import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.feature.chat.domain.repository.ChatRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class DeleteChatSessionUseCaseTest {

    private val repository: ChatRepository = mockk()
    private val useCase = DeleteChatSessionUseCase(repository)

    @Test
    fun `invoke retorna Success cuando repository elimina correctamente`() = runTest {
        coEvery { repository.deleteSession("session-1") } returns Result.Success(Unit)

        val result = useCase("session-1")

        assertTrue(result is Result.Success)
    }

    @Test
    fun `invoke retorna Error cuando repository falla`() = runTest {
        coEvery { repository.deleteSession("session-1") } returns Result.Error(AppException.ServerException)

        val result = useCase("session-1")

        assertTrue(result is Result.Error)
    }
}


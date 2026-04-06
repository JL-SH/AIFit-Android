package com.jlsh.aifit.feature.chat.data.repository

import app.cash.turbine.test
import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.core.network.ApiResponse
import com.jlsh.aifit.feature.chat.data.api.ChatApiService
import com.jlsh.aifit.feature.chat.data.local.ChatDao
import com.jlsh.aifit.feature.chat.domain.model.ChatSessionStatus
import com.jlsh.aifit.testutil.*
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class ChatRepositoryImplTest {

    private val apiService: ChatApiService = mockk()
    private val chatDao: ChatDao = mockk(relaxUnitFun = true)
    private lateinit var repository: ChatRepositoryImpl

    @Before
    fun setUp() {
        repository = ChatRepositoryImpl(apiService, chatDao)
    }

    // ── getSessions ─────────────────────────────────────────────────────────

    @Test
    fun `getSessions emite Loading, cache y luego remote`() = runTest {
        val cachedEntities = listOf(fakeChatSessionEntity())
        val remoteDtos = listOf(fakeChatSessionSummaryResponseDto(id = "session-remote"))

        coEvery { chatDao.getAllSessions() } returns cachedEntities
        coEvery { apiService.getSessions() } returns ApiResponse(success = true, data = remoteDtos)

        repository.getSessions().test {
            // 1) Loading
            val loading = awaitItem()
            assertTrue(loading is Result.Loading)

            // 2) Cache
            val cache = awaitItem()
            assertTrue(cache is Result.Success)
            assertEquals("session-1", (cache as Result.Success).data[0].id)

            // 3) Remote
            val remote = awaitItem()
            assertTrue(remote is Result.Success)
            assertEquals("session-remote", (remote as Result.Success).data[0].id)

            awaitComplete()
        }
    }

    @Test
    fun `getSessions sin cache emite Loading y luego remote`() = runTest {
        val remoteDtos = listOf(fakeChatSessionSummaryResponseDto())

        coEvery { chatDao.getAllSessions() } returns emptyList()
        coEvery { apiService.getSessions() } returns ApiResponse(success = true, data = remoteDtos)

        repository.getSessions().test {
            val loading = awaitItem()
            assertTrue(loading is Result.Loading)

            val remote = awaitItem()
            assertTrue(remote is Result.Success)

            awaitComplete()
        }
    }

    @Test
    fun `getSessions sin cache y API falla emite Loading y Error`() = runTest {
        coEvery { chatDao.getAllSessions() } returns emptyList()
        coEvery { apiService.getSessions() } throws RuntimeException("Network error")

        repository.getSessions().test {
            val loading = awaitItem()
            assertTrue(loading is Result.Loading)

            val error = awaitItem()
            assertTrue(error is Result.Error)

            awaitComplete()
        }
    }

    @Test
    fun `getSessions con cache y API falla emite Loading, cache y no emite error`() = runTest {
        val cachedEntities = listOf(fakeChatSessionEntity())

        coEvery { chatDao.getAllSessions() } returns cachedEntities
        coEvery { apiService.getSessions() } throws RuntimeException("Network error")

        repository.getSessions().test {
            val loading = awaitItem()
            assertTrue(loading is Result.Loading)

            val cache = awaitItem()
            assertTrue(cache is Result.Success)

            awaitComplete()
        }
    }

    // ── getSession ──────────────────────────────────────────────────────────

    @Test
    fun `getSession emite Loading, cache y luego remote`() = runTest {
        val cachedSession = fakeChatSessionEntity()
        val cachedMessages = listOf(fakeChatMessageEntity())
        val remoteDto = fakeChatSessionResponseDto()

        coEvery { chatDao.getSessionById("session-1") } returns cachedSession
        coEvery { chatDao.getMessagesBySessionId("session-1") } returns cachedMessages
        coEvery { apiService.getSession("session-1") } returns ApiResponse(success = true, data = remoteDto)

        repository.getSession("session-1").test {
            val loading = awaitItem()
            assertTrue(loading is Result.Loading)

            val cache = awaitItem()
            assertTrue(cache is Result.Success)

            val remote = awaitItem()
            assertTrue(remote is Result.Success)

            awaitComplete()
        }
    }

    @Test
    fun `getSession sin cache emite Loading y luego remote`() = runTest {
        val remoteDto = fakeChatSessionResponseDto()

        coEvery { chatDao.getSessionById("session-1") } returns null
        coEvery { chatDao.getMessagesBySessionId("session-1") } returns emptyList()
        coEvery { apiService.getSession("session-1") } returns ApiResponse(success = true, data = remoteDto)

        repository.getSession("session-1").test {
            val loading = awaitItem()
            assertTrue(loading is Result.Loading)

            val remote = awaitItem()
            assertTrue(remote is Result.Success)

            awaitComplete()
        }
    }

    @Test
    fun `getSession sin cache y API falla emite Loading y Error`() = runTest {
        coEvery { chatDao.getSessionById("session-1") } returns null
        coEvery { chatDao.getMessagesBySessionId("session-1") } returns emptyList()
        coEvery { apiService.getSession("session-1") } throws RuntimeException("Fail")

        repository.getSession("session-1").test {
            val loading = awaitItem()
            assertTrue(loading is Result.Loading)

            val error = awaitItem()
            assertTrue(error is Result.Error)

            awaitComplete()
        }
    }

    // ── startSession ────────────────────────────────────────────────────────

    @Test
    fun `startSession retorna Success y guarda en cache`() = runTest {
        val dto = fakeChatSessionResponseDto()
        coEvery { apiService.startSession(any()) } returns ApiResponse(success = true, data = dto)

        val result = repository.startSession("New Chat")

        assertTrue(result is Result.Success)
        assertEquals("session-1", (result as Result.Success).data.id)
        coVerify { chatDao.upsertSession(any()) }
    }

    @Test
    fun `startSession retorna Error cuando API falla`() = runTest {
        coEvery { apiService.startSession(any()) } throws RuntimeException("Server error")

        val result = repository.startSession(null)

        assertTrue(result is Result.Error)
    }

    // ── sendMessage ─────────────────────────────────────────────────────────

    @Test
    fun `sendMessage retorna Success y guarda en cache`() = runTest {
        val dto = fakeChatMessageResponseDto()
        coEvery { apiService.sendMessage("session-1", any()) } returns ApiResponse(success = true, data = dto)

        val result = repository.sendMessage("session-1", "Hello")

        assertTrue(result is Result.Success)
        coVerify { chatDao.insertMessage(any()) }
    }

    @Test
    fun `sendMessage retorna Error cuando API falla`() = runTest {
        coEvery { apiService.sendMessage("session-1", any()) } throws RuntimeException("Fail")

        val result = repository.sendMessage("session-1", "Hello")

        assertTrue(result is Result.Error)
    }

    // ── archiveSession ──────────────────────────────────────────────────────

    @Test
    fun `archiveSession retorna Success y actualiza cache local`() = runTest {
        val cached = fakeChatSessionEntity()
        coEvery { apiService.archiveSession("session-1") } returns ApiResponse(success = true, data = Unit)
        coEvery { chatDao.getSessionById("session-1") } returns cached

        val result = repository.archiveSession("session-1")

        assertTrue(result is Result.Success)
        coVerify { chatDao.upsertSession(cached.copy(status = "ARCHIVED")) }
    }

    @Test
    fun `archiveSession retorna Error cuando API falla`() = runTest {
        coEvery { apiService.archiveSession("session-1") } throws RuntimeException("Fail")

        val result = repository.archiveSession("session-1")

        assertTrue(result is Result.Error)
    }

    // ── deleteSession ───────────────────────────────────────────────────────

    @Test
    fun `deleteSession retorna Success y limpia cache`() = runTest {
        coEvery { apiService.deleteSession("session-1") } returns ApiResponse(success = true, data = Unit)

        val result = repository.deleteSession("session-1")

        assertTrue(result is Result.Success)
        coVerify { chatDao.deleteMessagesBySessionId("session-1") }
        coVerify { chatDao.deleteSession("session-1") }
    }

    @Test
    fun `deleteSession retorna Error cuando API falla`() = runTest {
        coEvery { apiService.deleteSession("session-1") } throws RuntimeException("Fail")

        val result = repository.deleteSession("session-1")

        assertTrue(result is Result.Error)
    }
}


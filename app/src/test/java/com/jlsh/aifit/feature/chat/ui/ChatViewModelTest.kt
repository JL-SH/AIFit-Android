package com.jlsh.aifit.feature.chat.ui

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.jlsh.aifit.core.common.AppException
import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.feature.chat.domain.model.ChatMessageRole
import com.jlsh.aifit.feature.chat.domain.model.ChatSessionStatus
import com.jlsh.aifit.feature.chat.domain.usecase.ArchiveChatSessionUseCase
import com.jlsh.aifit.feature.chat.domain.usecase.DeleteChatSessionUseCase
import com.jlsh.aifit.feature.chat.domain.usecase.GenerateChatSessionTitleUseCase
import com.jlsh.aifit.feature.chat.domain.usecase.GetChatSessionUseCase
import com.jlsh.aifit.feature.chat.domain.usecase.GetChatSessionsUseCase
import com.jlsh.aifit.feature.chat.domain.usecase.RenameChatSessionUseCase
import com.jlsh.aifit.feature.chat.domain.usecase.SendChatMessageUseCase
import com.jlsh.aifit.feature.chat.domain.usecase.StartChatSessionUseCase
import com.jlsh.aifit.feature.chat.ui.state.ChatListUiState
import com.jlsh.aifit.feature.chat.ui.state.ChatUiEvent
import com.jlsh.aifit.testutil.*
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.coVerify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ChatViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val getChatSessionsUseCase: GetChatSessionsUseCase = mockk()
    private val getChatSessionUseCase: GetChatSessionUseCase = mockk()
    private val startChatSessionUseCase: StartChatSessionUseCase = mockk()
    private val sendChatMessageUseCase: SendChatMessageUseCase = mockk()
    private val archiveChatSessionUseCase: ArchiveChatSessionUseCase = mockk()
    private val deleteChatSessionUseCase: DeleteChatSessionUseCase = mockk()
    private val renameChatSessionUseCase: RenameChatSessionUseCase = mockk()
    private val generateChatSessionTitleUseCase: GenerateChatSessionTitleUseCase = mockk()

    private fun buildViewModel(sessionId: String? = null): ChatViewModel {
        val savedState = SavedStateHandle().apply {
            if (sessionId != null) set("sessionId", sessionId)
        }
        return ChatViewModel(
            savedStateHandle = savedState,
            getChatSessionsUseCase = getChatSessionsUseCase,
            getChatSessionUseCase = getChatSessionUseCase,
            startChatSessionUseCase = startChatSessionUseCase,
            sendChatMessageUseCase = sendChatMessageUseCase,
            archiveChatSessionUseCase = archiveChatSessionUseCase,
            deleteChatSessionUseCase = deleteChatSessionUseCase,
            renameChatSessionUseCase = renameChatSessionUseCase,
            generateChatSessionTitleUseCase = generateChatSessionTitleUseCase,
        )
    }

    // ── List mode (no sessionId) ────────────────────────────────────────────

    @Test
    fun `sin sessionId, init carga lista de sesiones`() = runTest {
        val sessions = listOf(fakeChatSession())
        every { getChatSessionsUseCase() } returns flowOf(Result.Success(sessions))

        val vm = buildViewModel()
        advanceUntilIdle()

        val state = vm.listState.value
        assertTrue(state is ChatListUiState.Success)
        assertEquals(1, (state as ChatListUiState.Success).sessions.size)
    }

    @Test
    fun `loadSessions filtra sesiones ARCHIVED`() = runTest {
        val sessions = listOf(
            fakeChatSession(id = "s1", status = ChatSessionStatus.ACTIVE),
            fakeChatSession(id = "s2", status = ChatSessionStatus.ARCHIVED),
        )
        every { getChatSessionsUseCase() } returns flowOf(Result.Success(sessions))

        val vm = buildViewModel()
        advanceUntilIdle()

        val state = vm.listState.value as ChatListUiState.Success
        assertEquals(1, state.sessions.size)
        assertEquals("s1", state.sessions[0].id)
    }

    @Test
    fun `loadSessions con error produce ChatListUiState Error`() = runTest {
        every { getChatSessionsUseCase() } returns flowOf(Result.Error(AppException.NetworkException))

        val vm = buildViewModel()
        advanceUntilIdle()

        assertTrue(vm.listState.value is ChatListUiState.Error)
    }

    @Test
    fun `loadSessions con Loading produce ChatListUiState Loading`() = runTest {
        every { getChatSessionsUseCase() } returns flowOf(Result.Loading)

        val vm = buildViewModel()
        advanceUntilIdle()

        assertTrue(vm.listState.value is ChatListUiState.Loading)
    }

    // ── onNewSession ────────────────────────────────────────────────────────

    @Test
    fun `onNewSession envia NavigateToNewChat sin crear sesion`() = runTest {
        every { getChatSessionsUseCase() } returns flowOf(Result.Success(emptyList()))

        val vm = buildViewModel()
        advanceUntilIdle()

        vm.events.test {
            vm.onNewSession()
            advanceUntilIdle()

            val event = awaitItem()
            assertTrue(event is ChatUiEvent.NavigateToNewChat)
            coVerify(exactly = 0) { startChatSessionUseCase(any()) }

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onNewSession no emite snackbar de error porque no llama backend`() = runTest {
        every { getChatSessionsUseCase() } returns flowOf(Result.Success(emptyList()))

        val vm = buildViewModel()
        advanceUntilIdle()

        vm.events.test {
            vm.onNewSession()
            advanceUntilIdle()

            val event = awaitItem()
            assertTrue(event is ChatUiEvent.NavigateToNewChat)
            coVerify(exactly = 0) { startChatSessionUseCase(any()) }

            cancelAndIgnoreRemainingEvents()
        }
    }

    // ── onDeleteSession ─────────────────────────────────────────────────────

    @Test
    fun `onDeleteSession exitoso recarga sesiones y envia snackbar`() = runTest {
        every { getChatSessionsUseCase() } returns flowOf(Result.Success(emptyList()))
        coEvery { deleteChatSessionUseCase("s1") } returns Result.Success(Unit)

        val vm = buildViewModel()
        advanceUntilIdle()

        vm.events.test {
            vm.onDeleteSession("s1")
            advanceUntilIdle()

            val event = awaitItem()
            assertTrue(event is ChatUiEvent.ShowSnackbar)
            assertEquals("Sesión eliminada", (event as ChatUiEvent.ShowSnackbar).message)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onDeleteSession fallido envia snackbar de error`() = runTest {
        every { getChatSessionsUseCase() } returns flowOf(Result.Success(emptyList()))
        coEvery { deleteChatSessionUseCase("s1") } returns Result.Error(AppException.ServerException)

        val vm = buildViewModel()
        advanceUntilIdle()

        vm.events.test {
            vm.onDeleteSession("s1")
            advanceUntilIdle()

            val event = awaitItem()
            assertTrue(event is ChatUiEvent.ShowSnackbar)

            cancelAndIgnoreRemainingEvents()
        }
    }

    // ── onArchiveSession (list) ─────────────────────────────────────────────

    @Test
    fun `onArchiveSession exitoso recarga sesiones y envia snackbar`() = runTest {
        every { getChatSessionsUseCase() } returns flowOf(Result.Success(emptyList()))
        coEvery { archiveChatSessionUseCase("s1") } returns Result.Success(Unit)

        val vm = buildViewModel()
        advanceUntilIdle()

        vm.events.test {
            vm.onArchiveSession("s1")
            advanceUntilIdle()

            val event = awaitItem()
            assertTrue(event is ChatUiEvent.ShowSnackbar)
            assertEquals("Sesión archivada", (event as ChatUiEvent.ShowSnackbar).message)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onArchiveSession fallido envia snackbar de error`() = runTest {
        every { getChatSessionsUseCase() } returns flowOf(Result.Success(emptyList()))
        coEvery { archiveChatSessionUseCase("s1") } returns Result.Error(AppException.ServerException)

        val vm = buildViewModel()
        advanceUntilIdle()

        vm.events.test {
            vm.onArchiveSession("s1")
            advanceUntilIdle()

            val event = awaitItem()
            assertTrue(event is ChatUiEvent.ShowSnackbar)

            cancelAndIgnoreRemainingEvents()
        }
    }

    // ── Chat mode (with sessionId) ──────────────────────────────────────────

    @Test
    fun `con sessionId, init carga sesion y actualiza chatState`() = runTest {
        val session = fakeChatSession(id = "s1", title = "My Chat", messages = emptyList())
        every { getChatSessionUseCase("s1") } returns flowOf(Result.Success(session))

        val vm = buildViewModel(sessionId = "s1")
        advanceUntilIdle()

        val state = vm.chatState.value
        assertFalse(state.isLoading)
        assertNull(state.error)
        assertEquals("My Chat", state.sessionTitle)
        assertEquals(session.messages.size, state.messages.size)
    }

    @Test
    fun `loadSession con error actualiza chatState con error`() = runTest {
        every { getChatSessionUseCase("s1") } returns flowOf(Result.Error(AppException.ServerException))

        val vm = buildViewModel(sessionId = "s1")
        advanceUntilIdle()

        val state = vm.chatState.value
        assertFalse(state.isLoading)
        assertNotNull(state.error)
    }

    @Test
    fun `loadSession con Loading pone isLoading en true`() = runTest {
        every { getChatSessionUseCase("s1") } returns flowOf(Result.Loading)

        val vm = buildViewModel(sessionId = "s1")
        advanceUntilIdle()

        assertTrue(vm.chatState.value.isLoading)
    }

    // ── onInputChanged ──────────────────────────────────────────────────────

    @Test
    fun `onInputChanged actualiza inputText en chatState`() = runTest {
        every { getChatSessionUseCase("s1") } returns flowOf(Result.Success(fakeChatSession(messages = emptyList())))

        val vm = buildViewModel(sessionId = "s1")
        advanceUntilIdle()

        vm.onInputChanged("Hola mundo")

        assertEquals("Hola mundo", vm.chatState.value.inputText)
    }

    // ── onSendMessage ───────────────────────────────────────────────────────

    @Test
    fun `onSendMessage agrega mensaje USER optimistamente y luego agrega respuesta`() = runTest {
        val session = fakeChatSession(id = "s1", messages = emptyList())
        val assistantMsg = fakeChatMessage(id = "resp-1", role = ChatMessageRole.ASSISTANT, content = "Sure!")
        every { getChatSessionUseCase("s1") } returns flowOf(Result.Success(session))
        coEvery { sendChatMessageUseCase("s1", "Hello", null) } returns Result.Success(assistantMsg)
        coEvery { generateChatSessionTitleUseCase("s1") } returns Result.Success("My Chat")

        val vm = buildViewModel(sessionId = "s1")
        advanceUntilIdle()

        vm.onInputChanged("Hello")
        vm.onSendMessage()
        advanceUntilIdle()

        val state = vm.chatState.value
        // Should have USER (optimistic) + ASSISTANT (response) = 2
        assertEquals(2, state.messages.size)
        assertEquals(ChatMessageRole.USER, state.messages[0].role)
        assertEquals(ChatMessageRole.ASSISTANT, state.messages[1].role)
        assertFalse(state.isWaitingResponse)
        assertEquals("", state.inputText)
    }

    @Test
    fun `onSendMessage con error restaura isWaitingResponse y envia snackbar`() = runTest {
        val session = fakeChatSession(id = "s1", messages = emptyList())
        every { getChatSessionUseCase("s1") } returns flowOf(Result.Success(session))
        coEvery { sendChatMessageUseCase("s1", "Hello", null) } returns Result.Error(AppException.NetworkException)

        val vm = buildViewModel(sessionId = "s1")
        advanceUntilIdle()

        vm.events.test {
            vm.onInputChanged("Hello")
            vm.onSendMessage()
            advanceUntilIdle()

            val event = awaitItem()
            assertTrue(event is ChatUiEvent.ShowSnackbar)

            cancelAndIgnoreRemainingEvents()
        }

        assertFalse(vm.chatState.value.isWaitingResponse)
        // Optimistic message still present
        assertEquals(1, vm.chatState.value.messages.size)
    }

    @Test
    fun `onSendMessage sin sessionId crea sesion lazy y envia mensaje`() = runTest {
        every { getChatSessionsUseCase() } returns flowOf(Result.Success(emptyList()))
        coEvery { startChatSessionUseCase() } returns Result.Success(fakeChatSession(id = "new-session"))
        val assistantMsg = fakeChatMessage(id = "resp-1", role = ChatMessageRole.ASSISTANT, content = "Hola")
        coEvery { sendChatMessageUseCase("new-session", "Hello", null) } returns Result.Success(assistantMsg)
        coEvery { generateChatSessionTitleUseCase("new-session") } returns Result.Success("Nuevo Chat")

        val vm = buildViewModel(sessionId = null)
        advanceUntilIdle()

        vm.onInputChanged("Hello")
        vm.onSendMessage()
        advanceUntilIdle()

        // USER optimista + ASSISTANT respuesta
        assertEquals(2, vm.chatState.value.messages.size)
    }

    @Test
    fun `onSendMessage no hace nada con texto vacio`() = runTest {
        every { getChatSessionUseCase("s1") } returns flowOf(Result.Success(fakeChatSession(id = "s1", messages = emptyList())))

        val vm = buildViewModel(sessionId = "s1")
        advanceUntilIdle()

        val messagesBefore = vm.chatState.value.messages.size
        vm.onInputChanged("   ")
        vm.onSendMessage()
        advanceUntilIdle()

        assertEquals(messagesBefore, vm.chatState.value.messages.size)
    }

    @Test
    fun `onSendMessage no hace nada con texto mayor a 4000 caracteres`() = runTest {
        every { getChatSessionUseCase("s1") } returns flowOf(Result.Success(fakeChatSession(id = "s1", messages = emptyList())))

        val vm = buildViewModel(sessionId = "s1")
        advanceUntilIdle()

        val longText = "a".repeat(4001)
        vm.onInputChanged(longText)
        vm.onSendMessage()
        advanceUntilIdle()

        assertTrue(vm.chatState.value.messages.isEmpty())
    }

    // ── onArchiveCurrentSession ─────────────────────────────────────────────

    @Test
    fun `onArchiveCurrentSession exitoso envia snackbar y NavigateBack`() = runTest {
        every { getChatSessionUseCase("s1") } returns flowOf(Result.Success(fakeChatSession(id = "s1")))
        coEvery { archiveChatSessionUseCase("s1") } returns Result.Success(Unit)

        val vm = buildViewModel(sessionId = "s1")
        advanceUntilIdle()

        vm.events.test {
            vm.onArchiveCurrentSession()
            advanceUntilIdle()

            val snackbar = awaitItem()
            assertTrue(snackbar is ChatUiEvent.ShowSnackbar)
            assertEquals("Sesión archivada", (snackbar as ChatUiEvent.ShowSnackbar).message)

            val nav = awaitItem()
            assertTrue(nav is ChatUiEvent.NavigateBack)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onArchiveCurrentSession fallido envia snackbar de error`() = runTest {
        every { getChatSessionUseCase("s1") } returns flowOf(Result.Success(fakeChatSession(id = "s1")))
        coEvery { archiveChatSessionUseCase("s1") } returns Result.Error(AppException.ServerException)

        val vm = buildViewModel(sessionId = "s1")
        advanceUntilIdle()

        vm.events.test {
            vm.onArchiveCurrentSession()
            advanceUntilIdle()

            val event = awaitItem()
            assertTrue(event is ChatUiEvent.ShowSnackbar)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onArchiveCurrentSession sin sessionId no hace nada`() = runTest {
        every { getChatSessionsUseCase() } returns flowOf(Result.Success(emptyList()))

        val vm = buildViewModel(sessionId = null)
        advanceUntilIdle()

        // Should be a no-op without crashing
        vm.onArchiveCurrentSession()
        advanceUntilIdle()
    }
}


package com.jlsh.aifit.feature.chat.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.core.common.toMessage
import com.jlsh.aifit.feature.chat.domain.model.ChatMessage
import com.jlsh.aifit.feature.chat.domain.model.ChatMessageRole
import com.jlsh.aifit.feature.chat.domain.usecase.ArchiveChatSessionUseCase
import com.jlsh.aifit.feature.chat.domain.usecase.DeleteChatSessionUseCase
import com.jlsh.aifit.feature.chat.domain.usecase.GetChatSessionUseCase
import com.jlsh.aifit.feature.chat.domain.usecase.GetChatSessionsUseCase
import com.jlsh.aifit.feature.chat.domain.usecase.SendChatMessageUseCase
import com.jlsh.aifit.feature.chat.domain.usecase.StartChatSessionUseCase
import com.jlsh.aifit.feature.chat.ui.state.ChatListUiState
import com.jlsh.aifit.feature.chat.ui.state.ChatState
import com.jlsh.aifit.feature.chat.ui.state.ChatUiEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getChatSessionsUseCase: GetChatSessionsUseCase,
    private val getChatSessionUseCase: GetChatSessionUseCase,
    private val startChatSessionUseCase: StartChatSessionUseCase,
    private val sendChatMessageUseCase: SendChatMessageUseCase,
    private val archiveChatSessionUseCase: ArchiveChatSessionUseCase,
    private val deleteChatSessionUseCase: DeleteChatSessionUseCase,
) : ViewModel() {

    // ── Session List State ───────────────────────────────────────────────────
    private val _listState = MutableStateFlow<ChatListUiState>(ChatListUiState.Loading)
    val listState: StateFlow<ChatListUiState> = _listState.asStateFlow()

    // ── Active Chat State ────────────────────────────────────────────────────
    private val _chatState = MutableStateFlow(ChatState())
    val chatState: StateFlow<ChatState> = _chatState.asStateFlow()

    // ── Events ───────────────────────────────────────────────────────────────
    private val _events = Channel<ChatUiEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    private val sessionId: String? = savedStateHandle.get<String>("sessionId")

    init {
        if (sessionId != null) {
            loadSession(sessionId)
        } else {
            loadSessions()
        }
    }

    // ── Session List ─────────────────────────────────────────────────────────

    fun loadSessions() {
        viewModelScope.launch {
            getChatSessionsUseCase().collect { result ->
                when (result) {
                    is Result.Success -> {
                        _listState.value = ChatListUiState.Success(
                            sessions = result.data.filter {
                                it.status != com.jlsh.aifit.feature.chat.domain.model.ChatSessionStatus.ARCHIVED
                            },
                        )
                    }
                    is Result.Error -> {
                        _listState.value = ChatListUiState.Error(result.exception.toMessage())
                    }
                    is Result.Loading -> {
                        _listState.value = ChatListUiState.Loading
                    }
                }
            }
        }
    }

    fun onNewSession() {
        viewModelScope.launch {
            when (val result = startChatSessionUseCase()) {
                is Result.Success -> {
                    _events.send(ChatUiEvent.SessionCreated(result.data.id))
                }
                is Result.Error -> {
                    _events.send(ChatUiEvent.ShowSnackbar(result.exception.toMessage()))
                }
                else -> Unit
            }
        }
    }

    fun onDeleteSession(id: String) {
        viewModelScope.launch {
            when (val result = deleteChatSessionUseCase(id)) {
                is Result.Success -> {
                    loadSessions()
                    _events.send(ChatUiEvent.ShowSnackbar("Sesión eliminada"))
                }
                is Result.Error -> {
                    _events.send(ChatUiEvent.ShowSnackbar(result.exception.toMessage()))
                }
                else -> Unit
            }
        }
    }

    fun onArchiveSession(id: String) {
        viewModelScope.launch {
            when (val result = archiveChatSessionUseCase(id)) {
                is Result.Success -> {
                    loadSessions()
                    _events.send(ChatUiEvent.ShowSnackbar("Sesión archivada"))
                }
                is Result.Error -> {
                    _events.send(ChatUiEvent.ShowSnackbar(result.exception.toMessage()))
                }
                else -> Unit
            }
        }
    }

    // ── Active Chat ──────────────────────────────────────────────────────────

    fun loadSession(id: String) {
        viewModelScope.launch {
            _chatState.update { it.copy(isLoading = true, error = null) }
            getChatSessionUseCase(id).collect { result ->
                when (result) {
                    is Result.Success -> {
                        _chatState.update {
                            it.copy(
                                messages = result.data.messages,
                                sessionTitle = result.data.title,
                                isLoading = false,
                                error = null,
                            )
                        }
                    }
                    is Result.Error -> {
                        _chatState.update {
                            it.copy(isLoading = false, error = result.exception.toMessage())
                        }
                    }
                    is Result.Loading -> {
                        _chatState.update { it.copy(isLoading = true) }
                    }
                }
            }
        }
    }

    fun onInputChanged(text: String) {
        _chatState.update { it.copy(inputText = text) }
    }

    fun onSendMessage() {
        val sid = sessionId ?: return
        val content = _chatState.value.inputText.trim()
        if (content.isBlank() || content.length > 4000) return

        // Optimistic: add USER message immediately
        val userMessage = ChatMessage(
            id = UUID.randomUUID().toString(),
            role = ChatMessageRole.USER,
            content = content,
            createdAt = Instant.now().toString(),
        )

        _chatState.update {
            it.copy(
                messages = it.messages + userMessage,
                inputText = "",
                isWaitingResponse = true,
            )
        }

        viewModelScope.launch {
            when (val result = sendChatMessageUseCase(sid, content)) {
                is Result.Success -> {
                    _chatState.update {
                        it.copy(
                            messages = it.messages + result.data,
                            isWaitingResponse = false,
                        )
                    }
                }
                is Result.Error -> {
                    _chatState.update { it.copy(isWaitingResponse = false) }
                    _events.send(ChatUiEvent.ShowSnackbar(result.exception.toMessage()))
                }
                else -> Unit
            }
        }
    }

    fun onArchiveCurrentSession() {
        val sid = sessionId ?: return
        viewModelScope.launch {
            when (val result = archiveChatSessionUseCase(sid)) {
                is Result.Success -> {
                    _events.send(ChatUiEvent.ShowSnackbar("Sesión archivada"))
                    _events.send(ChatUiEvent.NavigateBack)
                }
                is Result.Error -> {
                    _events.send(ChatUiEvent.ShowSnackbar(result.exception.toMessage()))
                }
                else -> Unit
            }
        }
    }
}


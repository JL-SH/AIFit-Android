package com.jlsh.aifit.feature.chat.ui.state

import com.jlsh.aifit.core.ui.components.layout.UiStateHost
import com.jlsh.aifit.feature.chat.domain.model.ChatMessage
import com.jlsh.aifit.feature.chat.domain.model.ChatSession

sealed class ChatListUiState {
    data object Loading : ChatListUiState(), UiStateHost.Loading
    data class Error(override val message: String) : ChatListUiState(), UiStateHost.Error
    data class Success(
        val sessions: List<ChatSession>,
    ) : ChatListUiState(), UiStateHost.Success
}

data class ChatState(
    val messages: List<ChatMessage> = emptyList(),
    val isWaitingResponse: Boolean = false,
    val inputText: String = "",
    val sessionTitle: String = "",
    val isLoading: Boolean = true,
    val error: String? = null,
)


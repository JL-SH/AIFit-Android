package com.jlsh.aifit.feature.chat.ui.state

sealed class ChatUiEvent {
    data class NavigateToChat(val sessionId: String) : ChatUiEvent()
    data object NavigateToNewChat : ChatUiEvent()
    data object NavigateBack : ChatUiEvent()
    data class ShowSnackbar(val message: String) : ChatUiEvent()
    data class SessionCreated(val sessionId: String) : ChatUiEvent()
}


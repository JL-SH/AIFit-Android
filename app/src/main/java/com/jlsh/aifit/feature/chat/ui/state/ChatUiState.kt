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
    /** Bytes of the image pending to be sent (selected from the gallery).*/
    val pendingImageBytes: ByteArray? = null,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ChatState) return false
        return messages == other.messages &&
            isWaitingResponse == other.isWaitingResponse &&
            inputText == other.inputText &&
            sessionTitle == other.sessionTitle &&
            isLoading == other.isLoading &&
            error == other.error &&
            pendingImageBytes.contentEquals(other.pendingImageBytes)
    }

    override fun hashCode(): Int {
        var result = messages.hashCode()
        result = 31 * result + isWaitingResponse.hashCode()
        result = 31 * result + inputText.hashCode()
        result = 31 * result + sessionTitle.hashCode()
        result = 31 * result + isLoading.hashCode()
        result = 31 * result + (error?.hashCode() ?: 0)
        result = 31 * result + (pendingImageBytes?.contentHashCode() ?: 0)
        return result
    }
}

private fun ByteArray?.contentEquals(other: ByteArray?): Boolean {
    if (this == null && other == null) return true
    if (this == null || other == null) return false
    return this.contentEquals(other)
}


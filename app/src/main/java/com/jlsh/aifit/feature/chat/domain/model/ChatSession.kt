package com.jlsh.aifit.feature.chat.domain.model

enum class ChatSessionStatus {
    ACTIVE,
    ARCHIVED,
    UNKNOWN;

    companion object {
        fun fromString(value: String?): ChatSessionStatus =
            value?.let { runCatching { valueOf(it) }.getOrDefault(UNKNOWN) } ?: UNKNOWN
    }
}

data class ChatSession(
    val id: String,
    val title: String,
    val status: ChatSessionStatus,
    val messages: List<ChatMessage>,
    val createdAt: String,
    val updatedAt: String,
    val messageCount: Int = messages.size,
)


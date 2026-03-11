package com.jlsh.aifit.feature.chat.domain.model

enum class ChatMessageRole {
    USER,
    ASSISTANT,
    SYSTEM,
    UNKNOWN;

    companion object {
        fun fromString(value: String?): ChatMessageRole =
            value?.let { runCatching { valueOf(it) }.getOrDefault(UNKNOWN) } ?: UNKNOWN
    }
}

data class ChatMessage(
    val id: String,
    val role: ChatMessageRole,
    val content: String,
    val createdAt: String,
)


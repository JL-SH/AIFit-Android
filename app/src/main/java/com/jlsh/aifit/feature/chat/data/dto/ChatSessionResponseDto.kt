package com.jlsh.aifit.feature.chat.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class ChatSessionResponseDto(
    val id: String,
    val title: String,
    val status: String,
    val messages: List<ChatMessageResponseDto> = emptyList(),
    val createdAt: String,
    val updatedAt: String,
)


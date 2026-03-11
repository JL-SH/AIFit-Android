package com.jlsh.aifit.feature.chat.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class ChatMessageResponseDto(
    val id: String,
    val role: String,
    val content: String,
    val createdAt: String,
)


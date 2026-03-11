package com.jlsh.aifit.feature.chat.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class ChatSessionSummaryResponseDto(
    val id: String,
    val title: String,
    val status: String,
    val messageCount: Int,
    val createdAt: String,
    val updatedAt: String,
)


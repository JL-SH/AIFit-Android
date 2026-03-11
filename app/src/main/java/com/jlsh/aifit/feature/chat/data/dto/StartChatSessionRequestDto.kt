package com.jlsh.aifit.feature.chat.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class StartChatSessionRequestDto(
    val title: String? = null,
)


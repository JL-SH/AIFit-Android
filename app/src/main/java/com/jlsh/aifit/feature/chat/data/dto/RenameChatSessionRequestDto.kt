package com.jlsh.aifit.feature.chat.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class RenameChatSessionRequestDto(
    val title: String,
)


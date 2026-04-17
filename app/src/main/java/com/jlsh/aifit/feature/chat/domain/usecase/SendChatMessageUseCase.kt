package com.jlsh.aifit.feature.chat.domain.usecase

import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.feature.chat.domain.model.ChatMessage
import com.jlsh.aifit.feature.chat.domain.repository.ChatRepository
import javax.inject.Inject

class SendChatMessageUseCase @Inject constructor(
    private val repository: ChatRepository,
) {
    suspend operator fun invoke(sessionId: String, content: String, imageBase64: String? = null): Result<ChatMessage> =
        repository.sendMessage(sessionId, content, imageBase64)
}


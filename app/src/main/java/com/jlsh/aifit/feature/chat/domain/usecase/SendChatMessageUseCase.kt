package com.jlsh.aifit.feature.chat.domain.usecase

import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.feature.chat.domain.model.ChatMessage
import com.jlsh.aifit.feature.chat.domain.repository.ChatRepository
import javax.inject.Inject

/**
 * Use case that sends a message from the user in a chat session with the AI ​​Coach.
 *
 * @param repository Chat repository.
 */
class SendChatMessageUseCase @Inject constructor(
    private val repository: ChatRepository,
) {
    /**
     * Post a text message and, optionally, an attached image to the session.
     *
     * @param sessionId Identifier of the active chat session.
     * @param content Message text; It can be brief if only an image is sent.
     * @param imageBase64 Base64-encoded image, or `null` when there is no attachment.
     * @return [Result.Success] with the wizard response, or [Result.Error] if the send fails.
     */
    suspend operator fun invoke(sessionId: String, content: String, imageBase64: String? = null): Result<ChatMessage> =
        repository.sendMessage(sessionId, content, imageBase64)
}


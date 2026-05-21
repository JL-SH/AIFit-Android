package com.jlsh.aifit.feature.chat.domain.usecase

import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.feature.chat.domain.model.ChatMessage
import com.jlsh.aifit.feature.chat.domain.repository.ChatRepository
import javax.inject.Inject

/**
 * Caso de uso que envía un mensaje del usuario en una sesión de chat con el AI Coach.
 *
 * @param repository Repositorio de chat.
 */
class SendChatMessageUseCase @Inject constructor(
    private val repository: ChatRepository,
) {
    /**
     * Publica un mensaje de texto y, opcionalmente, una imagen adjunta en la sesión.
     *
     * @param sessionId Identificador de la sesión de chat activa.
     * @param content Texto del mensaje; puede ser breve si solo se envía imagen.
     * @param imageBase64 Imagen codificada en Base64, o `null` si no hay adjunto.
     * @return [Result.Success] con la respuesta del asistente, o [Result.Error] si falla el envío.
     */
    suspend operator fun invoke(sessionId: String, content: String, imageBase64: String? = null): Result<ChatMessage> =
        repository.sendMessage(sessionId, content, imageBase64)
}


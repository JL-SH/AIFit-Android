package com.jlsh.aifit.feature.chat.domain.usecase

import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.feature.chat.domain.model.ChatSession
import com.jlsh.aifit.feature.chat.domain.repository.ChatRepository
import javax.inject.Inject

class StartChatSessionUseCase @Inject constructor(
    private val repository: ChatRepository,
) {
    suspend operator fun invoke(title: String? = null): Result<ChatSession> =
        repository.startSession(title)
}


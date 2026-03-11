package com.jlsh.aifit.feature.chat.domain.usecase

import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.feature.chat.domain.model.ChatSession
import com.jlsh.aifit.feature.chat.domain.repository.ChatRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetChatSessionUseCase @Inject constructor(
    private val repository: ChatRepository,
) {
    operator fun invoke(id: String): Flow<Result<ChatSession>> =
        repository.getSession(id)
}


package com.jlsh.aifit.feature.chat.domain.usecase

import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.feature.chat.domain.repository.ChatRepository
import javax.inject.Inject

class RenameChatSessionUseCase @Inject constructor(
    private val repository: ChatRepository,
) {
    suspend operator fun invoke(id: String, title: String): Result<Unit> =
        repository.renameSession(id, title)
}


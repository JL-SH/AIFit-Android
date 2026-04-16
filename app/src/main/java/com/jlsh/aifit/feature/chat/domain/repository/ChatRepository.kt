package com.jlsh.aifit.feature.chat.domain.repository

import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.feature.chat.domain.model.ChatMessage
import com.jlsh.aifit.feature.chat.domain.model.ChatSession
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    fun getSessions(): Flow<Result<List<ChatSession>>>
    fun getSession(id: String): Flow<Result<ChatSession>>
    suspend fun startSession(title: String? = null): Result<ChatSession>
    suspend fun sendMessage(sessionId: String, content: String): Result<ChatMessage>
    suspend fun archiveSession(id: String): Result<Unit>
    suspend fun deleteSession(id: String): Result<Unit>
    suspend fun renameSession(id: String, title: String): Result<Unit>
    suspend fun generateSessionTitle(id: String): Result<String>
}


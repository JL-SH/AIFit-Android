package com.jlsh.aifit.feature.chat.data.repository

import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.core.network.BaseRemoteDataSource
import com.jlsh.aifit.feature.chat.data.api.ChatApiService
import com.jlsh.aifit.feature.chat.data.dto.SendChatMessageRequestDto
import com.jlsh.aifit.feature.chat.data.dto.StartChatSessionRequestDto
import com.jlsh.aifit.feature.chat.data.local.ChatDao
import com.jlsh.aifit.feature.chat.data.mapper.ChatMapper.toDomain
import com.jlsh.aifit.feature.chat.data.mapper.ChatMapper.toEntity
import com.jlsh.aifit.feature.chat.domain.model.ChatMessage
import com.jlsh.aifit.feature.chat.domain.model.ChatSession
import com.jlsh.aifit.feature.chat.domain.repository.ChatRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class ChatRepositoryImpl @Inject constructor(
    private val apiService: ChatApiService,
    private val chatDao: ChatDao,
) : BaseRemoteDataSource(), ChatRepository {

    override fun getSessions(): Flow<Result<List<ChatSession>>> = flow {
        emit(Result.Loading)

        // Emit cache first
        val cached = chatDao.getAllSessions()
        if (cached.isNotEmpty()) {
            emit(Result.Success(cached.map { it.toDomain() }))
        }

        // Fetch remote
        when (val remote = safeApiCall { apiService.getSessions() }) {
            is Result.Success -> {
                val entities = remote.data.map { it.toEntity() }
                chatDao.upsertAllSessions(entities)
                val sessions = remote.data.map { it.toDomain() }
                emit(Result.Success(sessions))
            }
            is Result.Error -> {
                if (cached.isEmpty()) emit(remote)
            }
            else -> Unit
        }
    }

    override fun getSession(id: String): Flow<Result<ChatSession>> = flow {
        emit(Result.Loading)

        // Emit cache first
        val cachedSession = chatDao.getSessionById(id)
        val cachedMessages = chatDao.getMessagesBySessionId(id)
        if (cachedSession != null) {
            emit(Result.Success(cachedSession.toDomain(cachedMessages.map { it.toDomain() })))
        }

        // Fetch remote
        when (val remote = safeApiCall { apiService.getSession(id) }) {
            is Result.Success -> {
                val dto = remote.data
                chatDao.upsertSession(dto.toEntity())
                dto.messages.forEach { msg ->
                    chatDao.insertMessage(msg.toEntity(id))
                }
                emit(Result.Success(dto.toDomain()))
            }
            is Result.Error -> {
                if (cachedSession == null) emit(remote)
            }
            else -> Unit
        }
    }

    override suspend fun startSession(title: String?): Result<ChatSession> {
        val request = StartChatSessionRequestDto(title = title)
        return when (val r = safeApiCall { apiService.startSession(request) }) {
            is Result.Success -> {
                val dto = r.data
                chatDao.upsertSession(dto.toEntity())
                dto.messages.forEach { msg ->
                    chatDao.insertMessage(msg.toEntity(dto.id))
                }
                Result.Success(dto.toDomain())
            }
            is Result.Error -> r
            else -> Result.Loading
        }
    }

    override suspend fun sendMessage(sessionId: String, content: String): Result<ChatMessage> {
        val request = SendChatMessageRequestDto(content = content)
        return when (val r = safeApiCall { apiService.sendMessage(sessionId, request) }) {
            is Result.Success -> {
                val dto = r.data
                chatDao.insertMessage(dto.toEntity(sessionId))
                Result.Success(dto.toDomain())
            }
            is Result.Error -> r
            else -> Result.Loading
        }
    }

    override suspend fun archiveSession(id: String): Result<Unit> {
        return try {
            val response = apiService.archiveSession(id)
            if (response.success) {
                val cached = chatDao.getSessionById(id)
                if (cached != null) {
                    chatDao.upsertSession(cached.copy(status = "ARCHIVED"))
                }
                Result.Success(Unit)
            } else {
                Result.Error(
                    com.jlsh.aifit.core.common.AppException.UnknownException(
                        response.message ?: "Error al archivar sesión"
                    )
                )
            }
        } catch (e: Exception) {
            Result.Error(com.jlsh.aifit.core.network.NetworkErrorMapper.map(e))
        }
    }

    override suspend fun deleteSession(id: String): Result<Unit> =
        when (val r = safeEmptyApiCall { apiService.deleteSession(id) }) {
            is Result.Success -> {
                chatDao.deleteMessagesBySessionId(id)
                chatDao.deleteSession(id)
                Result.Success(Unit)
            }
            is Result.Error -> r
            else -> Result.Loading
        }
}


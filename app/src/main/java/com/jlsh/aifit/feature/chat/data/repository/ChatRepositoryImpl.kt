package com.jlsh.aifit.feature.chat.data.repository

import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.core.network.BaseRemoteDataSource
import com.jlsh.aifit.feature.chat.data.api.ChatApiService
import com.jlsh.aifit.feature.chat.data.dto.RenameChatSessionRequestDto
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

/**
 * Implementation of [ChatRepository] with cache-first (Room) strategy and synchronization with the API.
 *
 * Emit local data immediately when it exists; updates from network and persists in Room.
 * If the network fails and there is no cache, it propagates the error.
 *
 * @param apiService HTTP client for chat sessions and messages.
 * @param chatDao Local access to sessions and messages.
 */
class ChatRepositoryImpl @Inject constructor(
    private val apiService: ChatApiService,
    private val chatDao: ChatDao,
) : BaseRemoteDataSource(), ChatRepository {

    /** {@inheritDoc} */
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

    /** {@inheritDoc} */
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

    /** {@inheritDoc} */
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

    /** {@inheritDoc} */
    override suspend fun sendMessage(sessionId: String, content: String, imageBase64: String?): Result<ChatMessage> {
        safeLogDebug("ChatRepo.sendMessage: inicio — sessionId=$sessionId, hasImage=${imageBase64 != null}, content='${content.take(50)}…'")
        val request = SendChatMessageRequestDto(content = content, imageBase64 = imageBase64)
        safeLogDebug("ChatRepo.sendMessage: llamando API POST chat/sessions/$sessionId/messages")
        return when (val r = safeApiCall { apiService.sendMessage(sessionId, request) }) {
            is Result.Success -> {
                val dto = r.data
                safeLogDebug("ChatRepo.sendMessage: API SUCCESS — msgId=${dto.id}, role=${dto.role}, content='${dto.content.take(80)}…'")
                chatDao.insertMessage(dto.toEntity(sessionId))
                Result.Success(dto.toDomain())
            }
            is Result.Error -> {
                safeLogError("ChatRepo.sendMessage: API ERROR — ${r.exception}")
                r
            }
            else -> {
                safeLogWarn("ChatRepo.sendMessage: resultado inesperado (Loading)")
                Result.Loading
            }
        }
    }

    private fun safeLogDebug(message: String) {
        runCatching { android.util.Log.d("AIFIT_DEBUG", message) }
    }

    private fun safeLogWarn(message: String) {
        runCatching { android.util.Log.w("AIFIT_DEBUG", message) }
    }

    private fun safeLogError(message: String) {
        runCatching { android.util.Log.e("AIFIT_DEBUG", message) }
    }

    /** {@inheritDoc} */
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

    /** {@inheritDoc} */
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

    /** {@inheritDoc} */
    override suspend fun renameSession(id: String, title: String): Result<Unit> {
        val request = RenameChatSessionRequestDto(title = title)
        return when (val r = safeApiCall { apiService.renameSession(id, request) }) {
            is Result.Success -> {
                val cached = chatDao.getSessionById(id)
                if (cached != null) chatDao.upsertSession(cached.copy(title = title))
                Result.Success(Unit)
            }
            is Result.Error -> r
            else -> Result.Loading
        }
    }

    /** {@inheritDoc} */
    override suspend fun generateSessionTitle(id: String): Result<String> =
        when (val r = safeApiCall { apiService.generateTitle(id) }) {
            is Result.Success -> {
                val title = r.data.title
                val cached = chatDao.getSessionById(id)
                if (cached != null) chatDao.upsertSession(cached.copy(title = title))
                Result.Success(title)
            }
            is Result.Error -> r
            else -> Result.Loading
        }
}


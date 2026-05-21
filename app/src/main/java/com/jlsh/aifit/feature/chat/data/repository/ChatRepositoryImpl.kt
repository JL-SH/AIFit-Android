package com.jlsh.aifit.feature.chat.data.repository

import android.util.Log
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
 * Implementación de [ChatRepository] con estrategia caché primero (Room) y sincronización con la API.
 *
 * Emite datos locales de inmediato cuando existen; actualiza desde red y persiste en Room.
 * Si la red falla y no hay caché, propaga el error.
 *
 * @param apiService Cliente HTTP de sesiones y mensajes de chat.
 * @param chatDao Acceso local a sesiones y mensajes.
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
        Log.d("AIFIT_DEBUG", "ChatRepo.sendMessage: inicio — sessionId=$sessionId, hasImage=${imageBase64 != null}, content='${content.take(50)}…'")
        val request = SendChatMessageRequestDto(content = content, imageBase64 = imageBase64)
        Log.d("AIFIT_DEBUG", "ChatRepo.sendMessage: llamando API POST chat/sessions/$sessionId/messages")
        return when (val r = safeApiCall { apiService.sendMessage(sessionId, request) }) {
            is Result.Success -> {
                val dto = r.data
                Log.d("AIFIT_DEBUG", "ChatRepo.sendMessage: API SUCCESS — msgId=${dto.id}, role=${dto.role}, content='${dto.content.take(80)}…'")
                chatDao.insertMessage(dto.toEntity(sessionId))
                Result.Success(dto.toDomain())
            }
            is Result.Error -> {
                Log.e("AIFIT_DEBUG", "ChatRepo.sendMessage: API ERROR — ${r.exception}")
                r
            }
            else -> {
                Log.w("AIFIT_DEBUG", "ChatRepo.sendMessage: resultado inesperado (Loading)")
                Result.Loading
            }
        }
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


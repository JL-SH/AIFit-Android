package com.jlsh.aifit.feature.chat.data.mapper

import com.jlsh.aifit.feature.chat.data.dto.ChatMessageResponseDto
import com.jlsh.aifit.feature.chat.data.dto.ChatSessionResponseDto
import com.jlsh.aifit.feature.chat.data.dto.ChatSessionSummaryResponseDto
import com.jlsh.aifit.feature.chat.data.local.ChatMessageEntity
import com.jlsh.aifit.feature.chat.data.local.ChatSessionEntity
import com.jlsh.aifit.feature.chat.domain.model.ChatMessage
import com.jlsh.aifit.feature.chat.domain.model.ChatMessageRole
import com.jlsh.aifit.feature.chat.domain.model.ChatSession
import com.jlsh.aifit.feature.chat.domain.model.ChatSessionStatus
import java.time.Instant

object ChatMapper {

    // ── DTO → Domain ─────────────────────────────────────────────────────────

    fun ChatSessionResponseDto.toDomain(): ChatSession = ChatSession(
        id = id,
        title = title,
        status = ChatSessionStatus.fromString(status),
        messages = messages.map { it.toDomain() },
        createdAt = createdAt,
        updatedAt = updatedAt,
    )

    fun ChatSessionSummaryResponseDto.toDomain(): ChatSession = ChatSession(
        id = id,
        title = title,
        status = ChatSessionStatus.fromString(status),
        messages = emptyList(),
        createdAt = createdAt,
        updatedAt = updatedAt,
        messageCount = messageCount,
    )

    fun ChatMessageResponseDto.toDomain(): ChatMessage = ChatMessage(
        id = id,
        role = ChatMessageRole.fromString(role),
        content = content,
        createdAt = createdAt,
    )

    // ── DTO → Entity ─────────────────────────────────────────────────────────

    fun ChatSessionResponseDto.toEntity(): ChatSessionEntity = ChatSessionEntity(
        id = id,
        title = title,
        status = status,
        messageCount = messages.size,
        createdAt = parseInstant(createdAt),
        updatedAt = parseInstant(updatedAt),
    )

    fun ChatSessionSummaryResponseDto.toEntity(): ChatSessionEntity = ChatSessionEntity(
        id = id,
        title = title,
        status = status,
        messageCount = messageCount,
        createdAt = parseInstant(createdAt),
        updatedAt = parseInstant(updatedAt),
    )

    fun ChatMessageResponseDto.toEntity(sessionId: String): ChatMessageEntity = ChatMessageEntity(
        id = id,
        sessionId = sessionId,
        role = role,
        content = content,
        createdAt = parseInstant(createdAt),
    )

    // ── Entity → Domain ──────────────────────────────────────────────────────

    fun ChatSessionEntity.toDomain(messages: List<ChatMessage> = emptyList()): ChatSession =
        ChatSession(
            id = id,
            title = title,
            status = ChatSessionStatus.fromString(status),
            messages = messages,
            createdAt = Instant.ofEpochMilli(createdAt).toString(),
            updatedAt = Instant.ofEpochMilli(updatedAt).toString(),
            messageCount = messageCount,
        )

    fun ChatMessageEntity.toDomain(): ChatMessage = ChatMessage(
        id = id,
        role = ChatMessageRole.fromString(role),
        content = content,
        createdAt = Instant.ofEpochMilli(createdAt).toString(),
    )

    // ── Domain → Entity ──────────────────────────────────────────────────────

    fun ChatMessage.toEntity(sessionId: String): ChatMessageEntity = ChatMessageEntity(
        id = id,
        sessionId = sessionId,
        role = role.name,
        content = content,
        createdAt = parseInstant(createdAt),
    )

    // ── Helpers ──────────────────────────────────────────────────────────────

    private fun parseInstant(value: String): Long =
        runCatching { Instant.parse(value).toEpochMilli() }.getOrDefault(0L)
}


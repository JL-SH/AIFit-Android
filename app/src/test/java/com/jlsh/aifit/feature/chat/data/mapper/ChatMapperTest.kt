package com.jlsh.aifit.feature.chat.data.mapper

import com.jlsh.aifit.feature.chat.data.mapper.ChatMapper.toDomain
import com.jlsh.aifit.feature.chat.data.mapper.ChatMapper.toEntity
import com.jlsh.aifit.feature.chat.domain.model.ChatMessageRole
import com.jlsh.aifit.feature.chat.domain.model.ChatSessionStatus
import com.jlsh.aifit.testutil.*
import org.junit.Assert.*
import org.junit.Test
import java.time.Instant

class ChatMapperTest {

    // ── ChatSessionResponseDto.toDomain ─────────────────────────────────────

    @Test
    fun `toDomain mapea ChatSessionResponseDto correctamente`() {
        val dto = fakeChatSessionResponseDto()
        val result = dto.toDomain()

        assertEquals("session-1", result.id)
        assertEquals("Workout Questions", result.title)
        assertEquals(ChatSessionStatus.ACTIVE, result.status)
        assertEquals(1, result.messages.size)
        assertEquals("2026-04-06T09:00:00Z", result.createdAt)
        assertEquals("2026-04-06T10:00:00Z", result.updatedAt)
    }

    @Test
    fun `toDomain mapea status desconocido a UNKNOWN en ChatSession`() {
        val dto = fakeChatSessionResponseDto(status = "DELETED")
        val result = dto.toDomain()

        assertEquals(ChatSessionStatus.UNKNOWN, result.status)
    }

    // ── ChatSessionSummaryResponseDto.toDomain ──────────────────────────────

    @Test
    fun `toDomain mapea ChatSessionSummaryResponseDto correctamente`() {
        val dto = fakeChatSessionSummaryResponseDto()
        val result = dto.toDomain()

        assertEquals("session-1", result.id)
        assertEquals("Workout Questions", result.title)
        assertEquals(ChatSessionStatus.ACTIVE, result.status)
        assertTrue(result.messages.isEmpty())
        assertEquals(3, result.messageCount)
    }

    // ── ChatMessageResponseDto.toDomain ─────────────────────────────────────

    @Test
    fun `toDomain mapea ChatMessageResponseDto correctamente`() {
        val dto = fakeChatMessageResponseDto()
        val result = dto.toDomain()

        assertEquals("msg-1", result.id)
        assertEquals(ChatMessageRole.ASSISTANT, result.role)
        assertEquals("Start with 5 minutes of light cardio.", result.content)
        assertEquals("2026-04-06T10:01:00Z", result.createdAt)
    }

    @Test
    fun `toDomain mapea role desconocido a UNKNOWN en ChatMessage`() {
        val dto = fakeChatMessageResponseDto(role = "SYSTEM")
        val result = dto.toDomain()

        assertEquals(ChatMessageRole.UNKNOWN, result.role)
    }

    // ── ChatSessionResponseDto.toEntity ─────────────────────────────────────

    @Test
    fun `toEntity mapea ChatSessionResponseDto a entidad correctamente`() {
        val dto = fakeChatSessionResponseDto()
        val entity = dto.toEntity()

        assertEquals("session-1", entity.id)
        assertEquals("Workout Questions", entity.title)
        assertEquals("ACTIVE", entity.status)
        assertEquals(1, entity.messageCount) // messages.size
        assertTrue(entity.createdAt > 0)
        assertTrue(entity.updatedAt > 0)
    }

    // ── ChatSessionSummaryResponseDto.toEntity ──────────────────────────────

    @Test
    fun `toEntity mapea ChatSessionSummaryResponseDto a entidad correctamente`() {
        val dto = fakeChatSessionSummaryResponseDto(messageCount = 5)
        val entity = dto.toEntity()

        assertEquals("session-1", entity.id)
        assertEquals(5, entity.messageCount)
        assertTrue(entity.createdAt > 0)
    }

    // ── ChatMessageResponseDto.toEntity ─────────────────────────────────────

    @Test
    fun `toEntity mapea ChatMessageResponseDto a entidad con sessionId`() {
        val dto = fakeChatMessageResponseDto()
        val entity = dto.toEntity(sessionId = "session-99")

        assertEquals("msg-1", entity.id)
        assertEquals("session-99", entity.sessionId)
        assertEquals("ASSISTANT", entity.role)
        assertEquals("Start with 5 minutes of light cardio.", entity.content)
        assertTrue(entity.createdAt > 0)
    }

    // ── ChatSessionEntity.toDomain ──────────────────────────────────────────

    @Test
    fun `toDomain mapea ChatSessionEntity correctamente sin mensajes`() {
        val entity = fakeChatSessionEntity()
        val result = entity.toDomain()

        assertEquals("session-1", result.id)
        assertEquals("Workout Questions", result.title)
        assertEquals(ChatSessionStatus.ACTIVE, result.status)
        assertTrue(result.messages.isEmpty())
        assertEquals(3, result.messageCount)
    }

    @Test
    fun `toDomain mapea ChatSessionEntity con mensajes`() {
        val entity = fakeChatSessionEntity()
        val messages = listOf(fakeChatMessage())
        val result = entity.toDomain(messages)

        assertEquals(1, result.messages.size)
        assertEquals("msg-1", result.messages[0].id)
    }

    @Test
    fun `toDomain mapea status desconocido de entidad a UNKNOWN`() {
        val entity = fakeChatSessionEntity(status = "INVALID")
        val result = entity.toDomain()

        assertEquals(ChatSessionStatus.UNKNOWN, result.status)
    }

    // ── ChatMessageEntity.toDomain ──────────────────────────────────────────

    @Test
    fun `toDomain mapea ChatMessageEntity correctamente`() {
        val entity = fakeChatMessageEntity()
        val result = entity.toDomain()

        assertEquals("msg-1", result.id)
        assertEquals(ChatMessageRole.USER, result.role)
        assertEquals("How should I warm up?", result.content)
        assertNotNull(result.createdAt)
    }

    @Test
    fun `toDomain mapea role desconocido de entidad a UNKNOWN`() {
        val entity = fakeChatMessageEntity(role = "MODERATOR")
        val result = entity.toDomain()

        assertEquals(ChatMessageRole.UNKNOWN, result.role)
    }

    // ── ChatMessage.toEntity ────────────────────────────────────────────────

    @Test
    fun `toEntity mapea ChatMessage de dominio a entidad correctamente`() {
        val message = fakeChatMessage(
            id = "msg-42",
            role = ChatMessageRole.USER,
            content = "Hello",
            createdAt = "2026-04-06T10:00:00Z",
        )
        val entity = message.toEntity(sessionId = "session-10")

        assertEquals("msg-42", entity.id)
        assertEquals("session-10", entity.sessionId)
        assertEquals("USER", entity.role)
        assertEquals("Hello", entity.content)
        assertEquals(Instant.parse("2026-04-06T10:00:00Z").toEpochMilli(), entity.createdAt)
    }

    // ── parseInstant edge case ──────────────────────────────────────────────

    @Test
    fun `toEntity con fecha invalida asigna 0L en createdAt`() {
        val dto = fakeChatMessageResponseDto(createdAt = "not-a-date")
        val entity = dto.toEntity(sessionId = "s1")

        assertEquals(0L, entity.createdAt)
    }

    // ── Enum fallback tests ─────────────────────────────────────────────────

    @Test
    fun `ChatSessionStatus fromString con null retorna UNKNOWN`() {
        assertEquals(ChatSessionStatus.UNKNOWN, ChatSessionStatus.fromString(null))
    }

    @Test
    fun `ChatSessionStatus fromString con valor valido retorna enum correcto`() {
        assertEquals(ChatSessionStatus.ARCHIVED, ChatSessionStatus.fromString("ARCHIVED"))
    }

    @Test
    fun `ChatSessionStatus fromString con valor desconocido retorna UNKNOWN`() {
        assertEquals(ChatSessionStatus.UNKNOWN, ChatSessionStatus.fromString("DELETED"))
    }

    @Test
    fun `ChatMessageRole fromString con null retorna UNKNOWN`() {
        assertEquals(ChatMessageRole.UNKNOWN, ChatMessageRole.fromString(null))
    }

    @Test
    fun `ChatMessageRole fromString con valor valido retorna enum correcto`() {
        assertEquals(ChatMessageRole.ASSISTANT, ChatMessageRole.fromString("ASSISTANT"))
    }

    @Test
    fun `ChatMessageRole fromString con valor desconocido retorna UNKNOWN`() {
        assertEquals(ChatMessageRole.UNKNOWN, ChatMessageRole.fromString("SYSTEM"))
    }
}


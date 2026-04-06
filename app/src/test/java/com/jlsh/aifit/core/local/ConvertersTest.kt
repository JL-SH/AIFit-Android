package com.jlsh.aifit.core.local

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime

class ConvertersTest {

    private val converters = Converters()

    // ─── LocalDate roundtrip ───────────────────────────────────────────────

    @Test
    fun `fromLocalDate returns non-null epoch day for a valid date`() {
        val date = LocalDate.of(2024, 6, 15)
        val epochDay = converters.fromLocalDate(date)
        assertEquals(date.toEpochDay(), epochDay)
    }

    @Test
    fun `toLocalDate restores original date from epoch day`() {
        val original = LocalDate.of(2024, 6, 15)
        val epochDay = converters.fromLocalDate(original)!!
        val restored = converters.toLocalDate(epochDay)
        assertEquals(original, restored)
    }

    @Test
    fun `LocalDate roundtrip is lossless for epoch start`() {
        val date = LocalDate.ofEpochDay(0)
        val restored = converters.toLocalDate(converters.fromLocalDate(date))
        assertEquals(date, restored)
    }

    @Test
    fun `fromLocalDate returns null for null input`() {
        assertNull(converters.fromLocalDate(null))
    }

    @Test
    fun `toLocalDate returns null for null input`() {
        assertNull(converters.toLocalDate(null))
    }

    // ─── LocalDateTime roundtrip ───────────────────────────────────────────

    @Test
    fun `fromLocalDateTime returns non-null epoch millis for a valid datetime`() {
        val dt = LocalDateTime.of(2024, 6, 15, 12, 30, 0)
        val millis = converters.fromLocalDateTime(dt)
        assertEquals(dt.toInstant(java.time.ZoneOffset.UTC).toEpochMilli(), millis)
    }

    @Test
    fun `toLocalDateTime restores original datetime from millis`() {
        val original = LocalDateTime.of(2024, 6, 15, 12, 30, 0)
        val millis = converters.fromLocalDateTime(original)!!
        val restored = converters.toLocalDateTime(millis)
        assertEquals(original, restored)
    }

    @Test
    fun `LocalDateTime roundtrip preserves hours minutes and seconds`() {
        val dt = LocalDateTime.of(2025, 1, 1, 23, 59, 59)
        val restored = converters.toLocalDateTime(converters.fromLocalDateTime(dt))
        assertEquals(dt, restored)
    }

    @Test
    fun `fromLocalDateTime returns null for null input`() {
        assertNull(converters.fromLocalDateTime(null))
    }

    @Test
    fun `toLocalDateTime returns null for null input`() {
        assertNull(converters.toLocalDateTime(null))
    }
}


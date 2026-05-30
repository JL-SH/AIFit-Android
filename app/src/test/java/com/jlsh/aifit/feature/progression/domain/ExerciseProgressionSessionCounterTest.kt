package com.jlsh.aifit.feature.progression.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class ExerciseProgressionSessionCounterTest {

    @Test
    fun `countDistinctSessions cuenta fechas unicas`() {
        val dates = listOf(
            "2026-04-01",
            "2026-04-01",
            "2026-04-08",
        )

        assertEquals(2, ExerciseProgressionSessionCounter.countDistinctSessions(dates))
    }
}

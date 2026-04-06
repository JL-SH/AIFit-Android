package com.jlsh.aifit.feature.nutrition.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class TargetSourceTest {

    @Test
    fun `fromString con valor MANUAL retorna MANUAL`() {
        assertEquals(TargetSource.MANUAL, TargetSource.fromString("MANUAL"))
    }

    @Test
    fun `fromString con valor AI_GENERATED retorna AI_GENERATED`() {
        assertEquals(TargetSource.AI_GENERATED, TargetSource.fromString("AI_GENERATED"))
    }

    @Test
    fun `fromString con valor desconocido retorna UNKNOWN sin crash`() {
        assertEquals(TargetSource.UNKNOWN, TargetSource.fromString("VALOR_INEXISTENTE"))
    }

    @Test
    fun `fromString con null retorna UNKNOWN`() {
        assertEquals(TargetSource.UNKNOWN, TargetSource.fromString(null))
    }

    @Test
    fun `fromString con string vacío retorna UNKNOWN`() {
        assertEquals(TargetSource.UNKNOWN, TargetSource.fromString(""))
    }
}


package com.jlsh.aifit.core.common

import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ResultFlowExtensionsTest {

    @Test
    fun `lastSuccessOrNull devuelve null si no hay Success`() = runTest {
        val result = flowOf(Result.Loading, Result.Error(com.jlsh.aifit.core.common.AppException.NetworkException))
            .lastSuccessOrNull()

        assertNull(result)
    }

    @Test
    fun `lastSuccessOrNull devuelve el ultimo Success en flujo cache mas red`() = runTest {
        val result = flow {
            emit(Result.Loading)
            emit(Result.Success("cache"))
            emit(Result.Success("api"))
        }.lastSuccessOrNull()

        assertEquals("api", result)
    }

    @Test
    fun `lastSuccessOrNull con un solo Success devuelve ese valor`() = runTest {
        val result = flowOf(Result.Success(42)).lastSuccessOrNull()

        assertEquals(42, result)
    }
}

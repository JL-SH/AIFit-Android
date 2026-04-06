package com.jlsh.aifit.feature.vision.domain.usecase

import com.jlsh.aifit.core.common.AppException
import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.feature.vision.domain.repository.VisionRepository
import com.jlsh.aifit.testutil.*
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class AnalyzeFoodPhotoUseCaseTest {

    private val repository: VisionRepository = mockk()
    private val useCase = AnalyzeFoodPhotoUseCase(repository)

    @Test
    fun `invoke retorna Success cuando repository responde ok`() = runTest {
        val result = fakeFoodPhotoAnalysisResult()
        coEvery { repository.analyzePhoto(any(), any()) } returns Result.Success(result)

        val actual = useCase(byteArrayOf(1, 2, 3))

        assertTrue(actual is Result.Success)
        assertEquals(result, (actual as Result.Success).data)
    }

    @Test
    fun `invoke retorna Error cuando repository falla`() = runTest {
        coEvery { repository.analyzePhoto(any(), any()) } returns Result.Error(AppException.ServerException)

        val actual = useCase(byteArrayOf(1, 2, 3))

        assertTrue(actual is Result.Error)
    }

    @Test
    fun `invoke usa image jpeg como contentType por defecto`() = runTest {
        val result = fakeFoodPhotoAnalysisResult()
        coEvery { repository.analyzePhoto(any(), eq("image/jpeg")) } returns Result.Success(result)

        val actual = useCase(byteArrayOf(1, 2, 3))

        assertTrue(actual is Result.Success)
    }
}


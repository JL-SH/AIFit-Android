package com.jlsh.aifit.feature.user.data.repository

import app.cash.turbine.test
import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.core.network.ApiResponse
import com.jlsh.aifit.core.datastore.AuthDataStore
import com.jlsh.aifit.feature.user.data.api.UserApiService
import com.jlsh.aifit.feature.user.data.local.UserProfileDao
import com.jlsh.aifit.feature.user.data.mapper.UserMapper.toDomain
import com.jlsh.aifit.feature.user.domain.model.UserProfile
import com.jlsh.aifit.testutil.fakeCreateUserProfileRequest
import com.jlsh.aifit.testutil.fakeUpdateUserProfileRequest
import com.jlsh.aifit.testutil.fakeUserProfileEntity
import com.jlsh.aifit.testutil.fakeUserProfileResponseDto
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException

class UserRepositoryImplTest {

    private val apiService: UserApiService = mockk()
    private val dao: UserProfileDao = mockk()
    private val authDataStore: AuthDataStore = mockk()
    private lateinit var sut: UserRepositoryImpl

    companion object {
        private const val FAKE_USER_ID = "user-123"
    }

    @Before
    fun setUp() {
        every { authDataStore.getUserId() } returns FAKE_USER_ID
        sut = UserRepositoryImpl(apiService, dao, authDataStore)
    }

    // ─── getProfile — cache-first ──────────────────────────────────────────────

    @Test
    fun `getProfile emite Loading, luego cache, luego dato fresco de API`() = runTest {
        val cached = fakeUserProfileEntity(id = FAKE_USER_ID)
        val fresh = fakeUserProfileResponseDto(id = "user-1", name = "Fresco")
        coEvery { dao.getById(FAKE_USER_ID) } returns cached
        coEvery { apiService.getProfile() } returns ApiResponse(success = true, data = fresh)
        coJustRun { dao.upsert(any()) }

        sut.getProfile().test {
            assertTrue(awaitItem() is Result.Loading)

            val cachedResult = awaitItem()
            assertTrue(cachedResult is Result.Success<*>)
            assertTrue((cachedResult as Result.Success<UserProfile>).data.id == cached.toDomain().id)

            val freshResult = awaitItem()
            assertTrue(freshResult is Result.Success<*>)
            assertTrue((freshResult as Result.Success<UserProfile>).data.name == "Fresco")

            coVerify(exactly = 1) { dao.upsert(any()) }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getProfile sin cache emite Loading luego dato fresco`() = runTest {
        val fresh = fakeUserProfileResponseDto()
        coEvery { dao.getById(FAKE_USER_ID) } returns null
        coEvery { apiService.getProfile() } returns ApiResponse(success = true, data = fresh)
        coJustRun { dao.upsert(any()) }

        sut.getProfile().test {
            assertTrue(awaitItem() is Result.Loading)
            assertTrue(awaitItem() is Result.Success<*>)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getProfile sin cache y API falla emite Loading luego Error`() = runTest {
        coEvery { dao.getById(FAKE_USER_ID) } returns null
        coEvery { apiService.getProfile() } throws IOException("timeout")

        sut.getProfile().test {
            assertTrue(awaitItem() is Result.Loading)
            assertTrue(awaitItem() is Result.Error)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getProfile con cache y API falla no emite Error`() = runTest {
        val cached = fakeUserProfileEntity(id = FAKE_USER_ID)
        coEvery { dao.getById(FAKE_USER_ID) } returns cached
        coEvery { apiService.getProfile() } throws IOException("sin red")

        sut.getProfile().test {
            assertTrue(awaitItem() is Result.Loading)
            assertTrue(awaitItem() is Result.Success<*>)
            // No debe emitir Error cuando ya hay cache
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ─── createProfile ────────────────────────────────────────────────────────

    @Test
    fun `createProfile happy path retorna Success y hace upsert en cache`() = runTest {
        val dto = fakeUserProfileResponseDto()
        coEvery { apiService.createProfile(any()) } returns ApiResponse(success = true, data = dto)
        coJustRun { dao.upsert(any()) }

        val result = sut.createProfile(fakeCreateUserProfileRequest())

        assertTrue(result is Result.Success<*>)
        coVerify(exactly = 1) { dao.upsert(any()) }
    }

    @Test
    fun `createProfile cuando API falla retorna Error`() = runTest {
        coEvery { apiService.createProfile(any()) } throws IOException("timeout")

        val result = sut.createProfile(fakeCreateUserProfileRequest())

        assertTrue(result is Result.Error)
        coVerify(exactly = 0) { dao.upsert(any()) }
    }

    @Test
    fun `createProfile cuando API retorna success=false retorna Error`() = runTest {
        coEvery { apiService.createProfile(any()) } returns
            ApiResponse(success = false, data = null, message = "Bad request")

        val result = sut.createProfile(fakeCreateUserProfileRequest())

        assertTrue(result is Result.Error)
    }

    // ─── updateProfile ────────────────────────────────────────────────────────

    @Test
    fun `updateProfile happy path retorna Success y actualiza cache`() = runTest {
        val dto = fakeUserProfileResponseDto()
        coEvery { apiService.updateProfile(any()) } returns ApiResponse(success = true, data = dto)
        coJustRun { dao.upsert(any()) }

        val result = sut.updateProfile(fakeUpdateUserProfileRequest())

        assertTrue(result is Result.Success<*>)
        coVerify(exactly = 1) { dao.upsert(any()) }
    }

    @Test
    fun `updateProfile cuando API falla retorna Error`() = runTest {
        coEvery { apiService.updateProfile(any()) } throws IOException("sin conexion")

        val result = sut.updateProfile(fakeUpdateUserProfileRequest())

        assertTrue(result is Result.Error)
        coVerify(exactly = 0) { dao.upsert(any()) }
    }
}

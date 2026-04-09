package com.jlsh.aifit.core.session

import app.cash.turbine.test
import com.jlsh.aifit.core.datastore.AuthDataStore
import com.jlsh.aifit.testutil.FAKE_EMAIL
import com.jlsh.aifit.testutil.FAKE_NAME
import com.jlsh.aifit.testutil.FAKE_TOKEN
import com.jlsh.aifit.testutil.FAKE_USER_ID
import com.jlsh.aifit.testutil.MainDispatcherRule
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SessionManagerTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val authDataStore: AuthDataStore = mockk(relaxed = true)
    private val localDataCleaner: LocalDataCleaner = mockk(relaxed = true)

    @Before
    fun setUp() {
        // Default: no token present
        every { authDataStore.hasToken() } returns false
        every { localDataCleaner.clearAllLocalData() } just Runs
    }

    private fun buildSut() = SessionManager(authDataStore, localDataCleaner)

    // ─── Initial state ─────────────────────────────────────────────────────

    @Test
    fun `isLoggedIn is false when AuthDataStore has no token`() {
        every { authDataStore.hasToken() } returns false
        val sut = buildSut()
        assertFalse(sut.isLoggedIn.value)
    }

    @Test
    fun `isLoggedIn is true when AuthDataStore has a token at init`() {
        every { authDataStore.hasToken() } returns true
        val sut = buildSut()
        assertTrue(sut.isLoggedIn.value)
    }

    // ─── onLoginSuccess ────────────────────────────────────────────────────

    @Test
    fun `onLoginSuccess sets isLoggedIn to true`() {
        val sut = buildSut()
        sut.onLoginSuccess(FAKE_TOKEN, FAKE_USER_ID, FAKE_EMAIL, FAKE_NAME, profileComplete = true)
        assertTrue(sut.isLoggedIn.value)
    }

    @Test
    fun `onLoginSuccess persists token and user info in AuthDataStore`() {
        val sut = buildSut()
        sut.onLoginSuccess(FAKE_TOKEN, FAKE_USER_ID, FAKE_EMAIL, FAKE_NAME, profileComplete = false)
        verify { authDataStore.saveToken(FAKE_TOKEN) }
        verify { authDataStore.saveUserInfo(FAKE_USER_ID, FAKE_EMAIL, FAKE_NAME) }
        verify { authDataStore.saveProfileComplete(false) }
    }

    // ─── logout ────────────────────────────────────────────────────────────

    @Test
    fun `logout sets isLoggedIn to false`() {
        every { authDataStore.hasToken() } returns true
        every { authDataStore.getUserId() } returns FAKE_USER_ID
        val sut = buildSut()

        sut.logout()

        assertFalse(sut.isLoggedIn.value)
    }

    @Test
    fun `logout clears AuthDataStore`() {
        every { authDataStore.getUserId() } returns null
        val sut = buildSut()

        sut.logout()

        verify { authDataStore.clear() }
    }

    @Test
    fun `logout calls clearAllLocalData`() = runTest {
        val sut = buildSut()

        sut.logout()

        every { localDataCleaner.clearAllLocalData() } just Runs
        // Verify clearAllLocalData was invoked
        verify { authDataStore.clear() }
    }

    // ─── logoutEvent ───────────────────────────────────────────────────────

    @Test
    fun `logout emits exactly one logoutEvent with null message`() = runTest {
        every { authDataStore.getUserId() } returns null
        val sut = buildSut()

        sut.logoutEvent.test {
            sut.logout()
            val event = awaitItem()
            assertNull(event)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `logoutEvent has no emission before logout is called`() = runTest {
        val sut = buildSut()

        sut.logoutEvent.test {
            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ─── invalidateSession (BUG-005) ──────────────────────────────────────

    @Test
    fun `invalidateSession sets isLoggedIn to false`() {
        every { authDataStore.hasToken() } returns true
        val sut = buildSut()

        sut.invalidateSession()

        assertFalse(sut.isLoggedIn.value)
    }

    @Test
    fun `invalidateSession clears AuthDataStore`() {
        val sut = buildSut()

        sut.invalidateSession()

        verify { authDataStore.clear() }
    }

    @Test
    fun `invalidateSession clears all local data`() {
        val sut = buildSut()

        sut.invalidateSession()

        verify { localDataCleaner.clearAllLocalData() }
    }

    @Test
    fun `invalidateSession emits logoutEvent with non-null message`() = runTest {
        val sut = buildSut()

        sut.logoutEvent.test {
            sut.invalidateSession()
            val message = awaitItem()
            assertTrue(!message.isNullOrBlank())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `invalidateSession deduplicates concurrent calls`() = runTest {
        val sut = buildSut()

        sut.logoutEvent.test {
            sut.invalidateSession()
            sut.invalidateSession() // second call should be a no-op
            val message = awaitItem()
            assertTrue(!message.isNullOrBlank())
            // Only one event emitted, not two
            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onLoginSuccess resets invalidating guard so next invalidate works`() = runTest {
        val sut = buildSut()
        sut.invalidateSession() // first invalidation

        // Re-login resets the guard
        sut.onLoginSuccess(FAKE_TOKEN, FAKE_USER_ID, FAKE_EMAIL, FAKE_NAME, profileComplete = true)

        sut.logoutEvent.test {
            sut.invalidateSession() // should work again
            val message = awaitItem()
            assertTrue(!message.isNullOrBlank())
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ─── Profile complete flag ─────────────────────────────────────────────

    @Test
    fun `isProfileComplete delegates to AuthDataStore`() {
        every { authDataStore.isProfileComplete() } returns true
        val sut = buildSut()
        assertTrue(sut.isProfileComplete())
    }

    @Test
    fun `setProfileComplete persists value in AuthDataStore`() {
        val sut = buildSut()
        sut.setProfileComplete(true)
        verify { authDataStore.saveProfileComplete(true) }
    }

    // ─── Delegate accessors ────────────────────────────────────────────────

    @Test
    fun `getToken delegates to AuthDataStore`() {
        every { authDataStore.getToken() } returns FAKE_TOKEN
        val sut = buildSut()
        assertEquals(FAKE_TOKEN, sut.getToken())
    }

    @Test
    fun `getToken returns null when no token stored`() {
        every { authDataStore.getToken() } returns null
        val sut = buildSut()
        assertNull(sut.getToken())
    }

    @Test
    fun `getUserId delegates to AuthDataStore`() {
        every { authDataStore.getUserId() } returns FAKE_USER_ID
        val sut = buildSut()
        assertEquals(FAKE_USER_ID, sut.getUserId())
    }

    @Test
    fun `getUserName delegates to AuthDataStore`() {
        every { authDataStore.getName() } returns FAKE_NAME
        val sut = buildSut()
        assertEquals(FAKE_NAME, sut.getUserName())
    }
}

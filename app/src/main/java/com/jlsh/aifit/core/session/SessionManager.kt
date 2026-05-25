package com.jlsh.aifit.core.session

import android.util.Log
import com.jlsh.aifit.core.datastore.AuthDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Application-wide singleton that owns the authentication state and
 * coordinates the session lifecycle.
 *
 * Consumers observe [isLoggedIn] to react to login/logout transitions and
 * [logoutEvent] to receive the optional human-readable message emitted when
 * a session is invalidated due to token expiry.
 *
 * All session-clearing work ([LocalDataCleaner], [AuthDataStore.clear]) is
 * executed on [Dispatchers.IO] via an internal supervisor scope so the
 * main thread is never blocked.
 */
@Singleton
class SessionManager @Inject constructor(
    private val authDataStore: AuthDataStore,
    private val localDataCleaner: LocalDataCleaner,
) {
    private val _isLoggedIn = MutableStateFlow(authDataStore.hasToken())

    /**
     * Hot flow that tracks whether the user is currently logged in.
     * Emits `true` immediately after a successful login and `false`
     * after any logout or session invalidation.
     */
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _logoutEvent = MutableSharedFlow<String?>(extraBufferCapacity = 1)

    /**
     * One-shot events emitted when the session ends.
     *
     * `null` indicates a voluntary logout; a non-null [String] contains the
     * human-readable reason (e.g. "Your session has expired...") to display as a
     * Snackbar on the login screen.
     */
    val logoutEvent: SharedFlow<String?> = _logoutEvent.asSharedFlow()

    /**
     * Dedicated IO scope for session-clearing work.
     * Lives as long as the singleton — no need to cancel it.
     */
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /**
     * Guard to prevent multiple concurrent invalidateSession() calls
     * (e.g. several 401 responses arriving at the same time).
     */
    private val invalidating = AtomicBoolean(false)

    /**
     * Persists credentials and user metadata after a successful authentication
     * and marks the session as active.
     *
     * If a **different** user was previously logged in, that user's cached
     * avatar is cleared before saving the new data.
     *
     * @param token The JWT to attach to subsequent API requests.
     * @param userId Unique identifier of the authenticated user.
     * @param email Email address of the authenticated user.
     * @param name Display name of the authenticated user.
     * @param profileComplete Whether the user has completed the onboarding profile.
     */
    fun onLoginSuccess(
        token: String,
        userId: String,
        email: String,
        name: String,
        profileComplete: Boolean,
    ) {
        invalidating.set(false)
        val previousUserId = authDataStore.getUserId()
        if (previousUserId != null && previousUserId != userId) {
            authDataStore.clearAvatarForUser(previousUserId)
        }
        authDataStore.saveToken(token)
        authDataStore.saveUserInfo(userId, email, name)
        authDataStore.saveProfileComplete(profileComplete)
        _isLoggedIn.value = true
    }

    /**
     * Updates the profile-completion flag stored in [AuthDataStore].
     *
     * Called after the user finishes the create-profile onboarding step.
     *
     * @param value `true` if the user's profile is fully configured.
     */
    fun setProfileComplete(value: Boolean) {
        authDataStore.saveProfileComplete(value)
    }

    /**
     * Returns `true` if the currently authenticated user has completed
     * the onboarding profile step.
     */
    fun isProfileComplete(): Boolean = authDataStore.isProfileComplete()

    /**
     * Voluntary logout triggered by the user.
     * Room is cleared on [Dispatchers.IO]; the navigation event fires only
     * AFTER the cleanup is complete so no stale data is ever shown post-logout.
     */
    fun logout() {
        Log.d("AIFIT", "logout() — clearing session data")
        scope.launch {
            clearSessionInternal()
            _logoutEvent.tryEmit(null)
        }
    }

    /**
     * Called from TokenAuthenticator when a 401 proves the token is irrecoverably
     * expired (no refresh-token endpoint exists in this backend).
     * Clears ALL local data and navigates the user to login with an explanatory message.
     */
    fun invalidateSession() {
        if (!invalidating.compareAndSet(false, true)) {
            Log.d("AIFIT", "invalidateSession() already in progress — skipping duplicate")
            return
        }
        Log.w("AIFIT", "invalidateSession() — token expired, clearing session")
        scope.launch {
            clearSessionInternal()
            _logoutEvent.tryEmit("Tu sesión ha caducado. Por favor, inicia sesión de nuevo.")
        }
    }

    /**
     * Clears Room tables (via [LocalDataCleaner], which uses [Dispatchers.IO] internally),
     * wipes the auth DataStore, and marks the session as logged-out.
     * Must be called from a coroutine.
     */
    private suspend fun clearSessionInternal() {
        localDataCleaner.clearAllLocalData() // suspends on Dispatchers.IO — never blocks Main
        authDataStore.clear()
        _isLoggedIn.value = false
    }

    /**
     * Returns the currently stored JWT, or `null` if the user is not logged in.
     */
    fun getToken(): String? = authDataStore.getToken()

    /**
     * Returns the unique identifier of the authenticated user, or `null`
     * if no session is active.
     */
    fun getUserId(): String? = authDataStore.getUserId()

    /**
     * Returns the display name of the authenticated user, or `null`
     * if no session is active.
     */
    fun getUserName(): String? = authDataStore.getName()
}

package com.jlsh.aifit.core.session

import android.util.Log
import com.jlsh.aifit.core.datastore.AuthDataStore
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionManager @Inject constructor(
    private val authDataStore: AuthDataStore,
    private val localDataCleaner: LocalDataCleaner,
) {
    private val _isLoggedIn = MutableStateFlow(authDataStore.hasToken())
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _logoutEvent = MutableSharedFlow<String?>(extraBufferCapacity = 1)
    val logoutEvent: SharedFlow<String?> = _logoutEvent.asSharedFlow()

    /**
     * Guard to prevent multiple concurrent invalidateSession() calls
     * (e.g. several 401 responses arriving at the same time).
     */
    private val invalidating = AtomicBoolean(false)

    fun onLoginSuccess(
        token: String,
        userId: String,
        email: String,
        name: String,
        profileComplete: Boolean,
    ) {
        invalidating.set(false)
        authDataStore.saveToken(token)
        authDataStore.saveUserInfo(userId, email, name)
        authDataStore.saveProfileComplete(profileComplete)
        _isLoggedIn.value = true
    }

    fun setProfileComplete(value: Boolean) {
        authDataStore.saveProfileComplete(value)
    }

    fun isProfileComplete(): Boolean = authDataStore.isProfileComplete()

    fun logout() {
        clearSessionInternal()
        _logoutEvent.tryEmit(null)
    }

    /**
     * Called from [TokenAuthenticator] when a 401 proves the token is irrecoverably
     * expired (no refresh-token endpoint exists in this backend).
     * Clears ALL local data and navigates the user to login with an explanatory message.
     */
    fun invalidateSession() {
        if (!invalidating.compareAndSet(false, true)) {
            Log.d("AIFIT", "invalidateSession() already in progress — skipping duplicate")
            return
        }
        Log.w("AIFIT", "invalidateSession() — token expired, clearing session")
        clearSessionInternal()
        _logoutEvent.tryEmit("Tu sesión ha caducado. Por favor, inicia sesión de nuevo.")
    }

    private fun clearSessionInternal() {
        localDataCleaner.clearAllLocalData()
        authDataStore.clear()
        _isLoggedIn.value = false
    }

    fun getToken(): String? = authDataStore.getToken()

    fun getUserId(): String? = authDataStore.getUserId()

    fun getUserName(): String? = authDataStore.getName()
}
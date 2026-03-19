package com.jlsh.aifit.core.session

import com.jlsh.aifit.core.datastore.AuthDataStore
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionManager @Inject constructor(
    private val authDataStore: AuthDataStore,
    private val localDataCleaner: LocalDataCleaner,
) {
    private val _isLoggedIn = MutableStateFlow(authDataStore.hasToken())
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _logoutEvent = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val logoutEvent: SharedFlow<Unit> = _logoutEvent.asSharedFlow()

    fun onLoginSuccess(
        token: String,
        userId: String,
        email: String,
        name: String,
        profileComplete: Boolean,
    ) {
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
        val userId = authDataStore.getUserId()
        if (userId != null) {
            runBlocking { localDataCleaner.clearDataForUser(userId) }
        }
        authDataStore.clear()
        _isLoggedIn.value = false
        _logoutEvent.tryEmit(Unit)
    }

    fun getToken(): String? = authDataStore.getToken()

    fun getUserId(): String? = authDataStore.getUserId()

    fun getUserName(): String? = authDataStore.getName()
}
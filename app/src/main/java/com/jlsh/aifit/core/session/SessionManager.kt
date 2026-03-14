package com.jlsh.aifit.core.session

import com.jlsh.aifit.core.datastore.AuthDataStore
import com.jlsh.aifit.core.datastore.UserPreferencesDataStore
import com.jlsh.aifit.core.local.AiFitDatabase
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
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionManager @Inject constructor(
    private val authDataStore: AuthDataStore,
    private val userPreferencesDataStore: UserPreferencesDataStore,
    private val database: AiFitDatabase,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _isLoggedIn = MutableStateFlow(authDataStore.hasToken())
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _logoutEvent = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val logoutEvent: SharedFlow<Unit> = _logoutEvent.asSharedFlow()

    fun onLoginSuccess(token: String, userId: String, email: String, name: String) {
        authDataStore.saveToken(token)
        authDataStore.saveUserInfo(userId, email, name)
        _isLoggedIn.value = true
    }

    fun logout() {
        authDataStore.clear()
        _isLoggedIn.value = false
        scope.launch {
            userPreferencesDataStore.clearUserData()
            database.clearAllTables()
        }
        _logoutEvent.tryEmit(Unit)
    }

    fun getToken(): String? = authDataStore.getToken()

    fun getUserId(): String? = authDataStore.getUserId()

    fun getUserName(): String? = authDataStore.getName()
}
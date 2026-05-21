package com.jlsh.aifit.core.session

import com.jlsh.aifit.core.local.AiFitDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Singleton responsible for wiping all locally cached data stored in the
 * Room database.
 *
 * Used by [SessionManager] during voluntary logout and session-invalidation
 * flows to ensure no stale data from a previous session is ever visible
 * to the next user or after re-login.
 */
@Singleton
class LocalDataCleaner @Inject constructor(
    private val database: AiFitDatabase,
) {
    /**
     * Wipe **every** Room table so no stale data from a previous session
     * is visible to the next user (or the same user after re-login).
     *
     * Must be called from a coroutine — executes on [Dispatchers.IO] internally
     * to avoid blocking the main thread (Room forbids clearAllTables() on Main).
     */
    suspend fun clearAllLocalData() {
        withContext(Dispatchers.IO) {
            database.clearAllTables()
        }
    }
}

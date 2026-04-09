package com.jlsh.aifit.core.session

import com.jlsh.aifit.core.local.AiFitDatabase
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalDataCleaner @Inject constructor(
    private val database: AiFitDatabase,
) {
    /**
     * Wipe **every** Room table so no stale data from a previous session
     * is visible to the next user (or the same user after re-login).
     */
    fun clearAllLocalData() {
        database.clearAllTables()
    }
}


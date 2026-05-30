package com.jlsh.aifit.core.datastore

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Encrypted persistent storage for authentication credentials and user
 * metadata, backed by [EncryptedSharedPreferences].
 *
 * All read operations are synchronous and safe to call from any thread.
 * Write operations use `apply()` for non-blocking, asynchronous disk I/O.
 *
 * If the underlying [EncryptedSharedPreferences] file becomes corrupted
 * (e.g. after a key-store wipe), [createPrefs] deletes the corrupt file
 * and recreates it to avoid an unrecoverable crash loop.
 */
@Singleton
class AuthDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val PREFS_NAME = "aifit_secure_prefs"
        private const val KEY_TOKEN = "jwt_token"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_EMAIL = "email"
        private const val KEY_NAME = "name"
        private const val KEY_PROFILE_COMPLETE = "profile_complete"
        private const val KEY_AVATAR_URL = "avatar_url"
        private const val KEY_AVATAR_USER_ID = "avatar_user_id"
    }

    private val prefs: SharedPreferences = createPrefs()

    /** Touches encrypted prefs once so later reads on the main thread are faster. */
    fun warmup() {
        prefs.contains(KEY_TOKEN)
    }

    private fun createPrefs(): SharedPreferences {
        return try {
            EncryptedSharedPreferences.create(
                PREFS_NAME,
                MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC),
                context,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            context.deleteSharedPreferences(PREFS_NAME)
            EncryptedSharedPreferences.create(
                PREFS_NAME,
                MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC),
                context,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        }
    }

    /**
     * Persists the JWT received after a successful authentication.
     *
     * @param token The JWT string to store.
     */
    fun saveToken(token: String) {
        prefs.edit().putString(KEY_TOKEN, token).apply()
    }

    /**
     * @return The stored JWT, or `null` if no token has been saved yet.
     */
    fun getToken(): String? = prefs.getString(KEY_TOKEN, null)

    /**
     * Atomically persists the authenticated user's core profile data.
     *
     * @param userId Unique identifier assigned by the backend.
     * @param email User's email address.
     * @param name User's display name.
     */
    fun saveUserInfo(userId: String, email: String, name: String) {
        prefs.edit()
            .putString(KEY_USER_ID, userId)
            .putString(KEY_EMAIL, email)
            .putString(KEY_NAME, name)
            .apply()
    }

    /**
     * @return The stored user ID, or `null` if the user is not logged in.
     */
    fun getUserId(): String? = prefs.getString(KEY_USER_ID, null)

    /**
     * @return The stored email address, or `null` if not set.
     */
    fun getEmail(): String? = prefs.getString(KEY_EMAIL, null)

    /**
     * @return The stored display name, or `null` if not set.
     */
    fun getName(): String? = prefs.getString(KEY_NAME, null)

    /**
     * Persists whether the user has completed the onboarding profile step.
     *
     * @param complete `true` if the profile is fully configured.
     */
    fun saveProfileComplete(complete: Boolean) {
        prefs.edit().putBoolean(KEY_PROFILE_COMPLETE, complete).apply()
    }

    /**
     * @return `true` if the profile-completion flag is set, `false` otherwise
     *   (including when no flag has ever been written).
     */
    fun isProfileComplete(): Boolean = prefs.getBoolean(KEY_PROFILE_COMPLETE, false)

    /**
     * Persists the avatar URL for [userId] so it can be displayed immediately
     * on re-login while the remote profile is still being fetched.
     *
     * Silently ignored when [url] is blank, preventing empty strings from
     * overwriting a previously valid URL.
     *
     * @param userId The user whose avatar this URL belongs to.
     * @param url The avatar image URL to cache.
     */
    fun saveAvatarUrl(userId: String, url: String) {
        if (url.isBlank()) return
        prefs.edit()
            .putString(KEY_AVATAR_URL, url)
            .putString(KEY_AVATAR_USER_ID, userId)
            .apply()
    }

    /**
     * Returns the cached avatar URL for [userId], or `null` if none is stored
     * or the cached URL belongs to a different user.
     *
     * @param userId The user whose avatar URL is requested.
     * @return A non-blank URL string, or `null`.
     */
    fun getAvatarUrl(userId: String?): String? {
        if (userId == null || prefs.getString(KEY_AVATAR_USER_ID, null) != userId) return null
        return prefs.getString(KEY_AVATAR_URL, null)?.takeIf { it.isNotBlank() }
    }

    /**
     * @return `true` if a JWT is currently stored, meaning the user is
     *   considered logged in at app startup.
     */
    fun hasToken(): Boolean = getToken() != null

    /**
     * Wipes all stored credentials and user data, **except** the avatar URL.
     *
     * The avatar URL is preserved so the same user sees their photo immediately
     * after re-login while the remote profile fetch is still in progress.
     * It is removed when a different user signs in (see [clearAvatarForUser]).
     */
    fun clear() {
        // Preserve avatar URL keyed by userId so the same user sees their photo immediately
        // after re-login while GET /users/me is in flight (cleared when a different user signs in).
        val avatarUrl = prefs.getString(KEY_AVATAR_URL, null)
        val avatarUserId = prefs.getString(KEY_AVATAR_USER_ID, null)
        prefs.edit().clear().apply()
        if (!avatarUrl.isNullOrBlank() && !avatarUserId.isNullOrBlank()) {
            prefs.edit()
                .putString(KEY_AVATAR_URL, avatarUrl)
                .putString(KEY_AVATAR_USER_ID, avatarUserId)
                .apply()
        }
    }

    /**
     * Removes the cached avatar URL if it belongs to [userId].
     *
     * Called by [SessionManager.onLoginSuccess] when a different user signs in
     * to prevent stale avatars from appearing for the new user.
     *
     * @param userId The user whose cached avatar should be evicted.
     */
    fun clearAvatarForUser(userId: String) {
        if (prefs.getString(KEY_AVATAR_USER_ID, null) == userId) {
            prefs.edit()
                .remove(KEY_AVATAR_URL)
                .remove(KEY_AVATAR_USER_ID)
                .apply()
        }
    }
}

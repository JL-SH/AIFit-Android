package com.jlsh.aifit.core.datastore

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

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

    fun saveToken(token: String) {
        prefs.edit().putString(KEY_TOKEN, token).apply()
    }

    fun getToken(): String? = prefs.getString(KEY_TOKEN, null)

    fun saveUserInfo(userId: String, email: String, name: String) {
        prefs.edit()
            .putString(KEY_USER_ID, userId)
            .putString(KEY_EMAIL, email)
            .putString(KEY_NAME, name)
            .apply()
    }

    fun getUserId(): String? = prefs.getString(KEY_USER_ID, null)

    fun getEmail(): String? = prefs.getString(KEY_EMAIL, null)

    fun getName(): String? = prefs.getString(KEY_NAME, null)

    fun saveProfileComplete(complete: Boolean) {
        prefs.edit().putBoolean(KEY_PROFILE_COMPLETE, complete).apply()
    }

    fun isProfileComplete(): Boolean = prefs.getBoolean(KEY_PROFILE_COMPLETE, false)

    /** Last known avatar URL for [userId]; survives logout so re-login can show it if GET lags. */
    fun saveAvatarUrl(userId: String, url: String) {
        if (url.isBlank()) return
        prefs.edit()
            .putString(KEY_AVATAR_URL, url)
            .putString(KEY_AVATAR_USER_ID, userId)
            .apply()
    }

    fun getAvatarUrl(userId: String?): String? {
        if (userId == null || prefs.getString(KEY_AVATAR_USER_ID, null) != userId) return null
        return prefs.getString(KEY_AVATAR_URL, null)?.takeIf { it.isNotBlank() }
    }

    fun hasToken(): Boolean = getToken() != null

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

    fun clearAvatarForUser(userId: String) {
        if (prefs.getString(KEY_AVATAR_USER_ID, null) == userId) {
            prefs.edit()
                .remove(KEY_AVATAR_URL)
                .remove(KEY_AVATAR_USER_ID)
                .apply()
        }
    }
}
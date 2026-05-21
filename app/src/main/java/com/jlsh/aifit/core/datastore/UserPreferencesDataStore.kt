package com.jlsh.aifit.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = "aifit_user_preferences"
)

/**
 * Persistent storage for non-sensitive user preferences, backed by Jetpack
 * [DataStore].
 *
 * Exposes each preference as a [Flow] so the UI can react to changes
 * reactively without manual polling. Defaults: dark theme enabled,
 * knowledge level `"BEGINNER"`.
 */
@Singleton
class UserPreferencesDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private val KEY_IS_DARK_THEME = booleanPreferencesKey("is_dark_theme")
        private val KEY_KNOWLEDGE_LEVEL = stringPreferencesKey("knowledge_level")
    }

    /**
     * Emits `true` when the dark theme is active, `false` for light theme.
     * Defaults to `true` (dark) when no preference has been explicitly set.
     */
    val isDarkTheme: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_IS_DARK_THEME] ?: true
    }

    /**
     * Emits the user's self-reported fitness knowledge level
     * (e.g. `"BEGINNER"`, `"INTERMEDIATE"`, `"ADVANCED"`).
     * Defaults to `"BEGINNER"` when not yet set.
     */
    val knowledgeLevel: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_KNOWLEDGE_LEVEL] ?: "BEGINNER"
    }

    /**
     * Persists the user's theme preference.
     *
     * @param isDark `true` to enable the dark theme, `false` for light theme.
     */
    suspend fun setDarkTheme(isDark: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_IS_DARK_THEME] = isDark
        }
    }

    /**
     * Persists the user's fitness knowledge level.
     *
     * @param level A knowledge-level string (e.g. `"BEGINNER"`, `"INTERMEDIATE"`,
     *   `"ADVANCED"`).
     */
    suspend fun setKnowledgeLevel(level: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_KNOWLEDGE_LEVEL] = level
        }
    }
}

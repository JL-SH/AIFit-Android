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

@Singleton
class UserPreferencesDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private val KEY_IS_DARK_THEME = booleanPreferencesKey("is_dark_theme")
        private val KEY_HAS_COMPLETED_ONBOARDING = booleanPreferencesKey("has_completed_onboarding")
        private val KEY_KNOWLEDGE_LEVEL = stringPreferencesKey("knowledge_level")
    }

    val isDarkTheme: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_IS_DARK_THEME] ?: true
    }

    val hasCompletedOnboarding: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_HAS_COMPLETED_ONBOARDING] ?: false
    }

    val knowledgeLevel: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_KNOWLEDGE_LEVEL] ?: "BEGINNER"
    }

    suspend fun setDarkTheme(isDark: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_IS_DARK_THEME] = isDark
        }
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_HAS_COMPLETED_ONBOARDING] = completed
        }
    }

    suspend fun setKnowledgeLevel(level: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_KNOWLEDGE_LEVEL] = level
        }
    }

    suspend fun clearUserData() {
        context.dataStore.edit { preferences ->
            preferences.remove(KEY_HAS_COMPLETED_ONBOARDING)
            preferences.remove(KEY_KNOWLEDGE_LEVEL)
        }
    }
}


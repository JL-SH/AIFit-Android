package com.jlsh.aifit.feature.user.ui.state

import com.jlsh.aifit.core.ui.components.layout.UiStateHost
import com.jlsh.aifit.feature.user.domain.model.UserProfile

/**
 * UI states of the user module (profile, hub and forms).
 *
 * Consumido por [com.jlsh.aifit.feature.user.ui.UserViewModel] y pantallas asociadas.
 */
sealed class UserUiState {

    /** No active network operation; editable form in profile creation.*/
    data object Idle : UserUiState()

    /** Loading profile from repository or cache.*/
    data object Loading : UserUiState(), UiStateHost.Loading

    /**
     * Profile available to display or edit.
     *
     * @property profile User domain data.
     */
    data class Success(val profile: UserProfile) : UserUiState(), UiStateHost.Success

    /**
     * Failed to load profile.
     *
     * @property message Text to display in [com.jlsh.aifit.core.ui.components.feedback.ErrorScreen].
     */
    data class Error(override val message: String) : UserUiState(), UiStateHost.Error

    /** Saving profile creation or update.*/
    data object Saving : UserUiState()
}


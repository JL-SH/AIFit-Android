package com.jlsh.aifit.feature.user.ui.state

import com.jlsh.aifit.core.ui.components.layout.UiStateHost
import com.jlsh.aifit.feature.user.domain.model.UserProfile

/**
 * Estados de la UI del módulo de usuario (perfil, hub y formularios).
 *
 * Consumido por [com.jlsh.aifit.feature.user.ui.UserViewModel] y pantallas asociadas.
 */
sealed class UserUiState {

    /** Sin operación de red activa; formulario editable en creación de perfil. */
    data object Idle : UserUiState()

    /** Cargando perfil desde repositorio o caché. */
    data object Loading : UserUiState(), UiStateHost.Loading

    /**
     * Perfil disponible para mostrar o editar.
     *
     * @property profile Datos de dominio del usuario.
     */
    data class Success(val profile: UserProfile) : UserUiState(), UiStateHost.Success

    /**
     * Fallo al cargar el perfil.
     *
     * @property message Texto para mostrar en [com.jlsh.aifit.core.ui.components.feedback.ErrorScreen].
     */
    data class Error(override val message: String) : UserUiState(), UiStateHost.Error

    /** Guardando creación o actualización del perfil. */
    data object Saving : UserUiState()
}


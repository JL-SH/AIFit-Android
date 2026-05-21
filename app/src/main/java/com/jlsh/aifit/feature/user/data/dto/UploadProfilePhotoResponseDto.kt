package com.jlsh.aifit.feature.user.data.dto

import kotlinx.serialization.Serializable

/**
 * DTO específico para la respuesta del endpoint POST users/me/photo.
 *
 * El backend devuelve únicamente la URL de la foto actualizada, no el perfil completo.
 * Se aceptan los dos nombres de campo más comunes para cubrir posibles variaciones del backend:
 *   - profilePictureUrl  (convención usada en el resto de DTOs de este proyecto)
 *   - profileImageUrl    (convención alternativa)
 *
 * El repositorio usa `profilePictureUrl ?: profileImageUrl` para obtener la URL resultante.
 */
@Serializable
data class UploadProfilePhotoResponseDto(
    val profilePictureUrl: String? = null,
    val profileImageUrl: String? = null,
)

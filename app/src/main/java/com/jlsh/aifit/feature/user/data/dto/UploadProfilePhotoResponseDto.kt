package com.jlsh.aifit.feature.user.data.dto

import kotlinx.serialization.Serializable

/**
 * Specific DTO for the POST users/me/photo endpoint response.
 *
 * The backend returns only the URL of the updated photo, not the full profile.
 * The two most common field names are accepted to cover possible backend variations:
 * - profilePictureUrl (convention used in the rest of the DTOs of this project)
 * - profileImageUrl (alternative convention)
 *
 * The repository uses `profilePictureUrl ?: profileImageUrl` to get the resulting URL.
 */
@Serializable
data class UploadProfilePhotoResponseDto(
    val profilePictureUrl: String? = null,
    val profileImageUrl: String? = null,
)

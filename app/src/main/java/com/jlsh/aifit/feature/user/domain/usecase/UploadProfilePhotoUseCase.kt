package com.jlsh.aifit.feature.user.domain.usecase

import android.net.Uri
import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.feature.user.domain.model.UserProfile
import com.jlsh.aifit.feature.user.domain.repository.UserRepository
import javax.inject.Inject

class UploadProfilePhotoUseCase @Inject constructor(
    private val repository: UserRepository,
) {
    suspend operator fun invoke(uri: Uri): Result<UserProfile> =
        repository.uploadProfilePhoto(uri)
}

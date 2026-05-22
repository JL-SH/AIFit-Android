package com.jlsh.aifit.feature.user.ui

import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.*
import android.content.res.Configuration
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jlsh.aifit.core.ui.components.buttons.PrimaryButton
import com.jlsh.aifit.core.ui.components.display.AvatarSize
import com.jlsh.aifit.core.ui.components.display.UserAvatar
import com.jlsh.aifit.core.ui.components.feedback.ErrorScreen
import com.jlsh.aifit.core.ui.components.feedback.LoadingScreen
import com.jlsh.aifit.core.ui.components.inputs.AiFitDatePickerBottomSheet
import com.jlsh.aifit.core.ui.components.inputs.AiFitDropdown
import com.jlsh.aifit.core.ui.components.inputs.AiFitNumberField
import com.jlsh.aifit.core.ui.components.inputs.AiFitTextField
import com.jlsh.aifit.core.ui.components.layout.AiFitTopBar
import com.jlsh.aifit.core.ui.theme.AIFitTheme
import com.jlsh.aifit.core.ui.theme.AiFitSpacing
import com.jlsh.aifit.feature.user.domain.model.ActivityLevel
import com.jlsh.aifit.feature.user.domain.model.Gender
import com.jlsh.aifit.feature.user.ui.state.UserUiEvent
import com.jlsh.aifit.feature.user.ui.state.UserUiState
import androidx.compose.ui.res.stringResource
import com.jlsh.aifit.R

/**
 * Pantalla de edición del perfil (modo `edit`).
 *
 * Muestra avatar con selector de galería y overlay de carga, nombre, fecha de nacimiento
 * con bottom sheet, género, altura, nivel de actividad y botón guardar. Reacciona a
 * [UserUiState] y eventos de [UserViewModel].
 *
 * @param onNavigateBack Vuelve tras guardar o desde la barra superior.
 * @param viewModel ViewModel de usuario inyectado por Hilt.
 */
@Composable
fun UserProfileScreen(
    onNavigateBack: () -> Unit,
    viewModel: UserViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val name by viewModel.name.collectAsStateWithLifecycle()
    val birthDate by viewModel.birthDate.collectAsStateWithLifecycle()
    val birthDateError by viewModel.birthDateError.collectAsStateWithLifecycle()
    val gender by viewModel.gender.collectAsStateWithLifecycle()
    val height by viewModel.height.collectAsStateWithLifecycle()
    val activityLevel by viewModel.activityLevel.collectAsStateWithLifecycle()
    val profilePictureUrl by viewModel.profilePictureUrl.collectAsStateWithLifecycle()
    val pendingPhotoUri by viewModel.pendingPhotoUri.collectAsStateWithLifecycle()
    val isUploadingPhoto by viewModel.isUploadingPhoto.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    var showDatePicker by remember { mutableStateOf(false) }

    // Prefer the locally selected photo; fall back to the server URL.
    // Coil handles both https:// URLs and content:// URIs from a String.
    val displayImageUrl: String? = pendingPhotoUri?.toString() ?: profilePictureUrl

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri: Uri? ->
        uri?.let { viewModel.onProfilePictureSelected(it) }
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is UserUiEvent.NavigateBack -> onNavigateBack()
                is UserUiEvent.ShowSnackbar -> snackbarHostState.showSnackbar(event.message)
                else -> Unit
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            AiFitTopBar(
                title = stringResource(R.string.profile_edit_title),
                onBack = onNavigateBack,
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { paddingValues ->
        when {
            uiState is UserUiState.Loading -> LoadingScreen()
            uiState is UserUiState.Error -> ErrorScreen(
                message = (uiState as UserUiState.Error).message,
                onRetry = viewModel::onRefresh,
            )
            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(paddingValues)
                        .padding(horizontal = AiFitSpacing.md)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(AiFitSpacing.md),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Spacer(Modifier.height(AiFitSpacing.md))

                    // ── Foto de perfil ────────────────────────────────────────────
                    Box(
                        modifier = Modifier
                            .size(AvatarSize.LARGE.size)
                            .clickable(
                                enabled = !isUploadingPhoto,
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() },
                            ) {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(
                                        ActivityResultContracts.PickVisualMedia.ImageOnly,
                                    ),
                                )
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        UserAvatar(
                            name = name.ifBlank { "?" },
                            imageUrl = displayImageUrl,
                            size = AvatarSize.LARGE,
                        )
                        if (isUploadingPhoto) {
                            // Loading overlay while the photo is being uploaded
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .background(
                                        color = MaterialTheme.colorScheme.background.copy(alpha = 0.7f),
                                        shape = CircleShape,
                                    ),
                                contentAlignment = Alignment.Center,
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(28.dp),
                                    strokeWidth = 2.5.dp,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                            }
                        } else {
                            // Camera badge — overlaps the bottom-right corner of the avatar circle
                            Box(
                                modifier = Modifier
                                    .size(22.dp)
                                    .align(Alignment.BottomEnd)
                                    .background(
                                        color = MaterialTheme.colorScheme.primary,
                                        shape = CircleShape,
                                    ),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = PhosphorIcons.Regular.Camera,
                                    contentDescription = stringResource(R.string.user_profile_change_photo),
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(13.dp),
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(AiFitSpacing.xs))

                    // ── Nombre ────────────────────────────────────────────────────
                    AiFitTextField(
                        value = name,
                        onValueChange = viewModel::onNameChanged,
                        label = stringResource(R.string.profile_field_name),
                        modifier = Modifier.fillMaxWidth(),
                    )

                    // ── Fecha de nacimiento ───────────────────────────────────────
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() },
                            ) { showDatePicker = true },
                    ) {
                        AiFitTextField(
                            value = if (birthDate.isNotBlank()) birthDate else "",
                            onValueChange = {},
                            label = stringResource(R.string.profile_field_birthday),
                            error = birthDateError,
                            enabled = false,
                            trailingIcon = PhosphorIcons.Regular.Calendar,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }

                    // ── Género ────────────────────────────────────────────────────
                    AiFitDropdown(
                        selectedValue = gender,
                        options = Gender.entries.filter { it != Gender.UNKNOWN }.map { it.name },
                        onOptionSelected = viewModel::onGenderChanged,
                        label = stringResource(R.string.profile_field_gender),
                        displayMapper = { it.toGenderDisplay() },
                    )

                    // ── Altura ────────────────────────────────────────────────────
                    AiFitNumberField(
                        value = height,
                        onValueChange = viewModel::onHeightChanged,
                        label = stringResource(R.string.profile_field_height),
                        suffix = "cm",
                    )

                    // ── Nivel de actividad ────────────────────────────────────────
                    AiFitDropdown(
                        selectedValue = activityLevel,
                        options = ActivityLevel.entries
                            .filter { it != ActivityLevel.UNKNOWN }
                            .map { it.name },
                        onOptionSelected = viewModel::onActivityLevelChanged,
                        label = stringResource(R.string.profile_field_activity),
                        displayMapper = { it.toActivityLevelDisplay() },
                    )

                    Spacer(Modifier.height(AiFitSpacing.sm))

                    PrimaryButton(
                        text = stringResource(R.string.common_save),
                        onClick = viewModel::onSaveProfile,
                        isLoading = uiState is UserUiState.Saving,
                        modifier = Modifier.fillMaxWidth(),
                    )

                    Spacer(Modifier.height(AiFitSpacing.lg))
                }
            }
        }
    }

    AiFitDatePickerBottomSheet(
        isVisible = showDatePicker,
        initialDate = birthDate.takeIf { it.isNotBlank() },
        onDateSelected = { isoDate ->
            viewModel.onBirthDateChanged(isoDate)
            showDatePicker = false
        },
        onDismiss = { showDatePicker = false },
    )
}

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "UserProfileScreen Dark",
)
@Composable
private fun UserProfileScreenPreview() {
    AIFitTheme(darkTheme = true) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            topBar = { AiFitTopBar(title = "Editar perfil", onBack = {}) },
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(Modifier.height(16.dp))

                Box(
                    modifier = Modifier.size(AvatarSize.LARGE.size),
                    contentAlignment = Alignment.Center,
                ) {
                    UserAvatar(name = "Carlos García", size = AvatarSize.LARGE)
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .align(Alignment.BottomEnd)
                            .background(
                                color = MaterialTheme.colorScheme.primary,
                                shape = CircleShape,
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = PhosphorIcons.Regular.Camera,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(13.dp),
                        )
                    }
                }

                Spacer(Modifier.height(4.dp))
                AiFitTextField(
                    value = "Carlos García",
                    onValueChange = {},
                    label = "Nombre completo",
                    modifier = Modifier.fillMaxWidth(),
                )
                AiFitTextField(
                    value = "1990-05-15",
                    onValueChange = {},
                    label = "Fecha de nacimiento",
                    enabled = false,
                    trailingIcon = PhosphorIcons.Regular.Calendar,
                    modifier = Modifier.fillMaxWidth(),
                )
                AiFitDropdown(
                    selectedValue = "MALE",
                    options = listOf("MALE", "FEMALE", "OTHER"),
                    onOptionSelected = {},
                    label = "Género",
                    displayMapper = { it.toGenderDisplay() },
                )
                AiFitNumberField(
                    value = "180",
                    onValueChange = {},
                    label = "Altura",
                    suffix = "cm",
                )
                AiFitDropdown(
                    selectedValue = "MODERATE",
                    options = listOf("SEDENTARY", "LIGHT", "MODERATE", "ACTIVE", "VERY_ACTIVE"),
                    onOptionSelected = {},
                    label = "Nivel de actividad",
                    displayMapper = { it.toActivityLevelDisplay() },
                )
                Spacer(Modifier.height(8.dp))
                PrimaryButton(text = stringResource(R.string.common_save), onClick = {}, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

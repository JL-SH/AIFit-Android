package com.jlsh.aifit.feature.user.ui

import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Fill
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.fill.*
import com.adamglin.phosphoricons.regular.*
import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jlsh.aifit.core.ui.components.display.AvatarSize
import com.jlsh.aifit.core.ui.components.display.UserAvatar
import com.jlsh.aifit.core.ui.components.feedback.ConfirmationDialog
import com.jlsh.aifit.core.ui.components.layout.AiFitTopBar
import com.jlsh.aifit.core.ui.components.layout.ScreenScaffold
import com.jlsh.aifit.core.ui.components.layout.SectionHeader
import com.jlsh.aifit.core.ui.theme.AIFitTheme
import com.jlsh.aifit.core.ui.theme.AiFitSpacing
import com.jlsh.aifit.feature.education.ui.components.GlossaryIntroSheet
import com.jlsh.aifit.feature.user.domain.model.GoalType
import com.jlsh.aifit.feature.user.domain.model.UserProfile
import com.jlsh.aifit.feature.user.ui.state.UserUiEvent
import com.jlsh.aifit.feature.user.ui.state.UserUiState
import androidx.compose.ui.res.stringResource
import com.jlsh.aifit.R

/**
 * Hub central del perfil: cabecera con avatar, estadísticas y menús por secciones.
 *
 * Agrupa accesos a edición, progreso, logros, glosario, tema oscuro y cierre de sesión.
 * Refresca el perfil al reanudar el ciclo de vida para reflejar fotos subidas en edición.
 *
 * @param onNavigateToEditProfile Abre [UserProfileScreen].
 * @param onNavigateToDashboard Panel de progreso.
 * @param onNavigateToBodyWeight Registro de peso corporal.
 * @param onNavigateToMetabolic Datos metabólicos.
 * @param onNavigateToExport Exportación de datos.
 * @param onNavigateToGamification Pantalla de gamificación; el parámetro distingue logros/récords.
 * @param onNavigateToGlossary Glosario educativo.
 * @param viewModel ViewModel de usuario inyectado por Hilt.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileHubScreen(
    onNavigateToEditProfile: () -> Unit,
    onNavigateToDashboard: () -> Unit,
    onNavigateToBodyWeight: () -> Unit,
    onNavigateToMetabolic: () -> Unit,
    onNavigateToExport: () -> Unit,
    onNavigateToGamification: (String) -> Unit,
    onNavigateToGlossary: () -> Unit,
    viewModel: UserViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showGlossaryIntro by remember { mutableStateOf(false) }
    val isDarkTheme by viewModel.isDarkTheme.collectAsStateWithLifecycle(initialValue = true)
    val streakCount by viewModel.streakCount.collectAsStateWithLifecycle()
    val achievementsCount by viewModel.achievementsCount.collectAsStateWithLifecycle()
    val recordsCount by viewModel.recordsCount.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current

    // Re-fetch the profile every time this screen comes back into focus so that
    // a photo uploaded on UserProfileScreen is immediately reflected here.
    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            viewModel.onRefresh()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is UserUiEvent.NavigateToEditProfile -> onNavigateToEditProfile()
                is UserUiEvent.ShowSnackbar -> snackbarHostState.showSnackbar(event.message)
                is UserUiEvent.Logout -> { /* handled by SessionManager */ }
                else -> Unit
            }
        }
    }

    ScreenScaffold<UserUiState.Success>(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        topBar = {
            AiFitTopBar(
                title = stringResource(R.string.profile_title),
                background = MaterialTheme.colorScheme.secondaryContainer,
            )
        },
        onRetry = viewModel::onRefresh,
    ) { paddingValues, successState ->
        ProfileHubContent(
            paddingValues = paddingValues,
            profile = successState.profile,
            isDarkTheme = isDarkTheme,
            streakCount = streakCount,
            achievementsCount = achievementsCount,
            recordsCount = recordsCount,
            onEditProfile = onNavigateToEditProfile,
            onDashboard = onNavigateToDashboard,
            onBodyWeight = onNavigateToBodyWeight,
            onMetabolic = onNavigateToMetabolic,
            onExport = onNavigateToExport,
            onAchievements = { onNavigateToGamification("ACHIEVEMENTS") },
            onRecords = { onNavigateToGamification("RECORDS") },
            onGlossary = { showGlossaryIntro = true },
            onThemeToggle = { viewModel.onToggleTheme() },
            onLogout = { showLogoutDialog = true },
        )
    }

    if (showGlossaryIntro) {
        GlossaryIntroSheet(
            onDismiss = { showGlossaryIntro = false },
            onConfirm = {
                showGlossaryIntro = false
                onNavigateToGlossary()
            },
        )
    }

    if (showLogoutDialog) {
        ConfirmationDialog(
            title = stringResource(R.string.profile_sign_out_title),
            message = stringResource(R.string.profile_sign_out_message),
            onConfirm = {
                viewModel.onLogout()
                showLogoutDialog = false
            },
            onDismiss = { showLogoutDialog = false },
        )
    }

}

@Composable
private fun ProfileHubContent(
    paddingValues: PaddingValues,
    profile: UserProfile,
    isDarkTheme: Boolean,
    streakCount: String = "—",
    achievementsCount: String = "—",
    recordsCount: String = "—",
    onEditProfile: () -> Unit,
    onDashboard: () -> Unit,
    onBodyWeight: () -> Unit,
    onMetabolic: () -> Unit,
    onExport: () -> Unit,
    onAchievements: () -> Unit,
    onRecords: () -> Unit,
    onGlossary: () -> Unit,
    onThemeToggle: () -> Unit,
    onLogout: () -> Unit,
) {
    LazyColumn(
        contentPadding = PaddingValues(
            start = AiFitSpacing.md,
            end = AiFitSpacing.md,
            top = AiFitSpacing.sm,
            bottom = 88.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(AiFitSpacing.xs),
        modifier = Modifier
            .padding(paddingValues)
            .testTag("profile_hub_list"),
    ) {
        // Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = AiFitSpacing.md),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                UserAvatar(
                    name = profile.name,
                    imageUrl = profile.profilePictureUrl,
                    size = AvatarSize.LARGE,
                )
                Spacer(modifier = Modifier.width(AiFitSpacing.md))
                Column {
                    Text(
                        text = profile.name,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    profile.goalType?.takeIf { it != GoalType.UNKNOWN }?.let {
                        Text(
                            text = it.displayName(),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }

        // Stats row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                StatColumn(value = streakCount, label = stringResource(R.string.profile_stat_streak))
                StatColumn(value = achievementsCount, label = stringResource(R.string.profile_stat_achievements))
                StatColumn(value = recordsCount, label = stringResource(R.string.profile_stat_records))
            }
            Spacer(modifier = Modifier.height(AiFitSpacing.sm))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        }

        // MI CUENTA
        item { SectionHeader(title = stringResource(R.string.profile_section_my_account)) }
        item {
            MenuRow(
                icon = PhosphorIcons.Regular.UserCircle,
                label = stringResource(R.string.profile_edit_label),
                onClick = onEditProfile,
            )
        }

        // PROGRESO
        item { SectionHeader(title = stringResource(R.string.profile_section_progress)) }
        item { MenuRow(PhosphorIcons.Regular.ChartLine, stringResource(R.string.profile_menu_dashboard), onDashboard) }
        item { MenuRow(PhosphorIcons.Regular.Scales, stringResource(R.string.profile_menu_body_weight), onBodyWeight) }
        item { MenuRow(PhosphorIcons.Regular.Flask, stringResource(R.string.profile_menu_metabolic), onMetabolic) }
        item { MenuRow(PhosphorIcons.Regular.ArrowsLeftRight, stringResource(R.string.profile_menu_export), onExport) }

        // LOGROS
        item { SectionHeader(title = stringResource(R.string.profile_section_achievements)) }
        item { MenuRow(PhosphorIcons.Fill.Trophy, stringResource(R.string.profile_menu_achievements), onAchievements) }
        item { MenuRow(PhosphorIcons.Regular.Barbell, stringResource(R.string.profile_menu_records), onRecords) }

        // HERRAMIENTAS
        item { SectionHeader(title = stringResource(R.string.profile_section_tools)) }
        item { MenuRow(PhosphorIcons.Regular.Books, stringResource(R.string.profile_menu_glossary), onGlossary) }

        // APP
        item { SectionHeader(title = stringResource(R.string.profile_section_app)) }
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = AiFitSpacing.sm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = PhosphorIcons.Regular.Moon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp),
                )
                Spacer(modifier = Modifier.width(AiFitSpacing.md))
                Text(
                    text = stringResource(R.string.profile_dark_mode),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                )
                Switch(
                    checked = isDarkTheme,
                    onCheckedChange = { onThemeToggle() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.primaryContainer,
                        checkedTrackColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                    ),
                )
            }
        }
        item {
            MenuRow(
                icon = PhosphorIcons.Regular.SignOut,
                label = stringResource(R.string.profile_sign_out_label),
                onClick = onLogout,
            )
        }
    }
}

@Composable
private fun StatColumn(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primaryContainer,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun MenuRow(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = AiFitSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp),
        )
        Spacer(modifier = Modifier.width(AiFitSpacing.md))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
        Icon(
            imageVector = PhosphorIcons.Regular.CaretRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.size(20.dp),
        )
    }
}

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "ProfileHubScreen Dark",
)
@Composable
private fun ProfileHubContentPreview() {
    AIFitTheme(darkTheme = true) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
        ) {
            ProfileHubContent(
                paddingValues = PaddingValues(),
                profile = UserProfile(
                    id = "1",
                    name = "Carlos García",
                    email = "carlos@ejemplo.com",
                    authProvider = "LOCAL",
                    goalType = GoalType.GAIN_MUSCLE,
                ),
                isDarkTheme = true,
                onEditProfile = {},
                onDashboard = {},
                onBodyWeight = {},
                onMetabolic = {},
                onExport = {},
                onAchievements = {},
                onRecords = {},
                onGlossary = {},
                onThemeToggle = {},
                onLogout = {},
            )
        }
    }
}

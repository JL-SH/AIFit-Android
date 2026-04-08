package com.jlsh.aifit.feature.user.ui

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ExitToApp
import androidx.compose.material.icons.automirrored.rounded.LibraryBooks
import androidx.compose.material.icons.automirrored.rounded.ShowChart
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.ImportExport
import androidx.compose.material.icons.rounded.MonitorWeight
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Science
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

    LaunchedEffect(Unit) {
        viewModel.onRefresh()
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
                title = "Yo",
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
            title = "Cerrar sesión",
            message = "¿Seguro que quieres cerrar sesión?",
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
                StatColumn(value = streakCount, label = "Racha")
                StatColumn(value = achievementsCount, label = "Logros")
                StatColumn(value = recordsCount, label = "Récords")
            }
            Spacer(modifier = Modifier.height(AiFitSpacing.sm))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        }

        // MI CUENTA
        item { SectionHeader(title = "Mi cuenta") }
        item {
            MenuRow(
                icon = Icons.Rounded.Person,
                label = "Editar perfil",
                onClick = onEditProfile,
            )
        }

        // PROGRESO
        item { SectionHeader(title = "Progreso") }
        item { MenuRow(Icons.AutoMirrored.Rounded.ShowChart, "Dashboard", onDashboard) }
        item { MenuRow(Icons.Rounded.MonitorWeight, "Peso corporal", onBodyWeight) }
        item { MenuRow(Icons.Rounded.Science, "Análisis metabólico", onMetabolic) }
        item { MenuRow(Icons.Rounded.ImportExport, "Exportar", onExport) }

        // LOGROS
        item { SectionHeader(title = "Logros") }
        item { MenuRow(Icons.Rounded.EmojiEvents, "Logros y rachas", onAchievements) }
        item { MenuRow(Icons.Rounded.FitnessCenter, "Récords personales", onRecords) }

        // HERRAMIENTAS
        item { SectionHeader(title = "Herramientas") }
        item { MenuRow(Icons.AutoMirrored.Rounded.LibraryBooks, "Glosario", onGlossary) }

        // APP
        item { SectionHeader(title = "App") }
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = AiFitSpacing.sm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Rounded.DarkMode,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp),
                )
                Spacer(modifier = Modifier.width(AiFitSpacing.md))
                Text(
                    text = "Tema oscuro",
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
                icon = Icons.AutoMirrored.Rounded.ExitToApp,
                label = "Cerrar sesión",
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
            imageVector = Icons.Rounded.ChevronRight,
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






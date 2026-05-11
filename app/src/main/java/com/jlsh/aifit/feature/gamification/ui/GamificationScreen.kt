package com.jlsh.aifit.feature.gamification.ui

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.FileDownload
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Whatshot
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jlsh.aifit.R
import com.jlsh.aifit.core.ui.components.display.AiFitCard
import com.jlsh.aifit.core.ui.components.display.StreakBadge
import com.jlsh.aifit.core.ui.components.feedback.EmptyStateView
import com.jlsh.aifit.core.ui.components.feedback.InlineLoadingIndicator
import com.jlsh.aifit.core.ui.components.layout.AiFitTabRow
import com.jlsh.aifit.core.ui.components.layout.AiFitTopBar
import com.jlsh.aifit.core.ui.theme.AIFitTheme
import com.jlsh.aifit.core.ui.theme.AiFitSpacing
import com.jlsh.aifit.feature.gamification.domain.model.AchievementDefinition
import com.jlsh.aifit.feature.gamification.domain.model.AchievementRarity
import com.jlsh.aifit.feature.gamification.domain.model.PersonalRecord
import com.jlsh.aifit.feature.gamification.domain.model.Streak
import com.jlsh.aifit.feature.gamification.domain.model.StreakStatus
import com.jlsh.aifit.feature.gamification.domain.model.StreakType
import com.jlsh.aifit.feature.gamification.domain.model.UserAchievement
import com.jlsh.aifit.feature.gamification.ui.state.GamificationUiEvent
import com.jlsh.aifit.feature.gamification.ui.state.GamificationUiState
import com.jlsh.aifit.feature.user.ui.toStringRes
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import com.jlsh.aifit.core.ui.components.display.StreakStatus as BadgeStreakStatus

@Composable
fun GamificationScreen(
    onNavigateBack: () -> Unit,
    onNavigateToExport: () -> Unit,
    viewModel: GamificationViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is GamificationUiEvent.NavigateToExport -> onNavigateToExport()
                is GamificationUiEvent.NavigateBack -> onNavigateBack()
                is GamificationUiEvent.ShowSnackbar -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        AiFitTopBar(
            title = stringResource(R.string.gamification_title),
            onBack = onNavigateBack,
            background = MaterialTheme.colorScheme.background,
            actions = {
                IconButton(onClick = { viewModel.onNavigateToExport() }) {
                    Icon(
                        imageVector = Icons.Rounded.FileDownload,
                        contentDescription = stringResource(R.string.gamification_export_cd),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            },
        )

        when (val state = uiState) {
            is GamificationUiState.Loading -> {
                InlineLoadingIndicator(
                    message = stringResource(R.string.common_loading),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(AiFitSpacing.xl),
                )
            }

            is GamificationUiState.Error -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(AiFitSpacing.md),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = state.message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                    )
                }
            }

            is GamificationUiState.Success -> {
                AiFitTabRow(
                    tabs = listOf(
                        stringResource(R.string.gamification_tab_streaks),
                        stringResource(R.string.gamification_tab_achievements),
                        stringResource(R.string.gamification_tab_records),
                    ),
                    selectedIndex = state.selectedTabIndex,
                    onTabSelected = viewModel::onTabSelected,
                )

                when (state.selectedTabIndex) {
                    0 -> StreaksTab(streaks = state.streaks)
                    1 -> AchievementsTab(
                        userAchievements = state.achievements,
                        allDefinitions = state.allDefinitions,
                    )
                    2 -> RecordsTab(records = state.personalRecords)
                }
            }
        }
    }
}

// ── Tab: RACHAS ──────────────────────────────────────────────────────────────

@Composable
private fun StreaksTab(streaks: List<Streak>) {
    if (streaks.isEmpty()) {
        EmptyStateView(
            icon = Icons.Rounded.Whatshot,
            title = stringResource(R.string.gamification_streak_empty_title),
            subtitle = stringResource(R.string.gamification_streak_empty_subtitle),
            modifier = Modifier
                .fillMaxSize()
                .padding(top = AiFitSpacing.xxl),
        )
        return
    }

    LazyColumn(
        contentPadding = PaddingValues(AiFitSpacing.md),
        verticalArrangement = Arrangement.spacedBy(AiFitSpacing.sm),
    ) {
        items(streaks, key = { it.type.name }) { streak ->
            AiFitCard {
                Column(
                    modifier = Modifier.padding(AiFitSpacing.md),
                    verticalArrangement = Arrangement.spacedBy(AiFitSpacing.sm),
                ) {
                    StreakBadge(
                        count = streak.currentCount,
                        label = streakTypeLabel(streak.type),
                        status = streak.status.toBadgeStatus(),
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column {
                            Text(
                                text = stringResource(R.string.gamification_streak_current_label),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                text = stringResource(R.string.gamification_streak_days, streak.currentCount),
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = stringResource(R.string.gamification_streak_best_label),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                text = stringResource(R.string.gamification_streak_days, streak.longestCount),
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.primaryContainer,
                            )
                        }
                    }
                    Text(
                        text = stringResource(R.string.gamification_streak_last_activity, streak.lastActivityDate.format(DateTimeFormatter.ofPattern("dd MMM yyyy"))),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

// ── Tab: LOGROS ──────────────────────────────────────────────────────────────

@Composable
private fun AchievementsTab(
    userAchievements: List<UserAchievement>,
    allDefinitions: List<AchievementDefinition>,
) {
    // Los "conseguidos" vienen directamente del servidor (datos verídicos con IDs reales).
    // NO los derivamos de allDefinitions para evitar depender de que la API funcione.
    val unlocked: List<AchievementDefinition> = userAchievements.map { it.achievement }
    val unlockedCodes: Set<String> = unlocked.map { it.code }.toSet()

    // Los "bloqueados" son las definiciones (API o fallback local) que el usuario aún no tiene.
    val locked: List<AchievementDefinition> = allDefinitions.filter { it.code !in unlockedCodes }

    if (unlocked.isEmpty() && locked.isEmpty()) {
        EmptyStateView(
            icon = Icons.Rounded.EmojiEvents,
            title = stringResource(R.string.gamification_achievement_empty_title),
            subtitle = stringResource(R.string.gamification_achievement_empty_subtitle),
            modifier = Modifier
                .fillMaxSize()
                .padding(top = AiFitSpacing.xxl),
        )
        return
    }

    LazyColumn(
        contentPadding = PaddingValues(AiFitSpacing.md),
        verticalArrangement = Arrangement.spacedBy(AiFitSpacing.sm),
    ) {
        // ── Logros conseguidos ──
        if (unlocked.isNotEmpty()) {
            item(key = "header_unlocked") {
                Text(
                    text = stringResource(R.string.gamification_achievement_unlocked_header, unlocked.size),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = AiFitSpacing.xs),
                )
            }
            items(unlocked, key = { "unlocked_${it.id}" }) { definition ->
                val userAchievement = userAchievements.find { it.achievement.id == definition.id }
                AchievementCard(
                    definition = definition,
                    isUnlocked = true,
                    unlockedAt = userAchievement?.unlockedAt,
                )
            }
        }

        // ── Próximos logros ──
        if (locked.isNotEmpty()) {
            item(key = "header_locked") {
                Text(
                    text = stringResource(R.string.gamification_achievement_locked_header, locked.size),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(
                        top = if (unlocked.isNotEmpty()) AiFitSpacing.md else 0.dp,
                        bottom = AiFitSpacing.xs,
                    ),
                )
            }
            items(locked, key = { "locked_${it.id}" }) { definition ->
                AchievementCard(
                    definition = definition,
                    isUnlocked = false,
                    unlockedAt = null,
                )
            }
        }
    }
}

@Composable
private fun AchievementCard(
    definition: AchievementDefinition,
    isUnlocked: Boolean,
    unlockedAt: String?,
) {
    val alpha = if (isUnlocked) 1f else 0.55f
    val rarityColor = when (definition.rarity) {
        AchievementRarity.COMMON -> MaterialTheme.colorScheme.surfaceVariant
        AchievementRarity.UNCOMMON -> MaterialTheme.colorScheme.surfaceVariant
        AchievementRarity.RARE -> MaterialTheme.colorScheme.tertiaryContainer
        AchievementRarity.LEGENDARY -> MaterialTheme.colorScheme.errorContainer
        AchievementRarity.UNKNOWN -> MaterialTheme.colorScheme.surfaceVariant
    }
    val iconTint = if (isUnlocked) MaterialTheme.colorScheme.primaryContainer
    else MaterialTheme.colorScheme.onSurfaceVariant

    AiFitCard(modifier = Modifier.alpha(alpha)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AiFitSpacing.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AiFitSpacing.md),
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(rarityColor, MaterialTheme.shapes.medium),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = if (isUnlocked) Icons.Rounded.EmojiEvents else Icons.Rounded.Lock,
                    contentDescription = null,
                    modifier = Modifier.size(28.dp),
                    tint = iconTint,
                )
            }

            // Text content
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(AiFitSpacing.xs),
            ) {
                Text(
                    text = definition.name,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                )

                Text(
                    text = if (isUnlocked) definition.description else stringResource(R.string.gamification_achievement_how_to, definition.description),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 3,
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(AiFitSpacing.sm),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // Rarity badge
                    Box(
                        modifier = Modifier
                            .background(rarityColor, MaterialTheme.shapes.small)
                            .padding(horizontal = 8.dp, vertical = 2.dp),
                    ) {
                        Text(
                            text = stringResource(definition.rarity.toStringRes()),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isUnlocked) MaterialTheme.colorScheme.onSurface
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    if (isUnlocked && unlockedAt != null) {
                        Text(
                            text = unlockedAt.take(10),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    } else if (!isUnlocked) {
                        Text(
                            text = stringResource(R.string.gamification_achievement_locked),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

// ── Tab: RÉCORDS ─────────────────────────────────────────────────────────────

@Composable
private fun RecordsTab(records: List<PersonalRecord>) {
    if (records.isEmpty()) {
        EmptyStateView(
            icon = Icons.Rounded.FitnessCenter,
            title = stringResource(R.string.gamification_record_empty_title),
            subtitle = stringResource(R.string.gamification_record_empty_subtitle),
            modifier = Modifier
                .fillMaxSize()
                .padding(top = AiFitSpacing.xxl),
        )
        return
    }

    val grouped = records.groupBy { it.exerciseName }

    LazyColumn(
        contentPadding = PaddingValues(AiFitSpacing.md),
        verticalArrangement = Arrangement.spacedBy(AiFitSpacing.sm),
    ) {
        grouped.forEach { (exerciseName, prs) ->
            val best = prs.maxByOrNull { it.weightKg } ?: prs.first()
            item(key = "pr_$exerciseName") {
                AiFitCard {
                    Column(
                        modifier = Modifier.padding(AiFitSpacing.md),
                        verticalArrangement = Arrangement.spacedBy(AiFitSpacing.xs),
                    ) {
                        Text(
                            text = exerciseName,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom,
                        ) {
                            Column {
                                Text(
                                    text = "${best.weightKg} kg",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                )
                                Text(
                                    text = "${best.reps} reps",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = stringResource(R.string.gamification_record_one_rm),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                Text(
                                    text = "${"%.1f".format(best.estimatedOneRepMax)} kg",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                            }
                        }
                        Text(
                            text = best.achievedAt.take(10),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

// ── Helpers ──────────────────────────────────────────────────────────────────

@Composable
private fun streakTypeLabel(type: StreakType): String = when (type) {
    StreakType.TRAINING -> stringResource(R.string.gamification_streak_type_training)
    StreakType.NUTRITION -> stringResource(R.string.gamification_streak_type_nutrition)
    StreakType.COMBINED -> stringResource(R.string.gamification_streak_type_combined)
    StreakType.UNKNOWN -> stringResource(R.string.gamification_streak_type_unknown)
}

private fun StreakStatus.toBadgeStatus(): BadgeStreakStatus = when (this) {
    StreakStatus.ACTIVE -> BadgeStreakStatus.ACTIVE
    StreakStatus.BROKEN -> BadgeStreakStatus.BROKEN
    StreakStatus.RECOVERING -> BadgeStreakStatus.FROZEN
    StreakStatus.UNKNOWN -> BadgeStreakStatus.BROKEN
}

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "GamificationScreen Dark",
)
@Composable
private fun GamificationScreenPreview() {
    AIFitTheme(darkTheme = true) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
        ) {
            StreaksTab(
                streaks = listOf(
                    Streak(
                        type = StreakType.TRAINING,
                        status = StreakStatus.ACTIVE,
                        currentCount = 12,
                        longestCount = 15,
                        lastActivityDate = LocalDate.now(),
                        startedAt = "2025-01-01T00:00:00Z",
                    ),
                    Streak(
                        type = StreakType.NUTRITION,
                        status = StreakStatus.RECOVERING,
                        currentCount = 3,
                        longestCount = 20,
                        lastActivityDate = LocalDate.now().minusDays(2),
                        startedAt = "2025-02-01T00:00:00Z",
                    ),
                ),
            )
        }
    }
}

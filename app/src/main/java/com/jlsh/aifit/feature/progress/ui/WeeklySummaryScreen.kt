package com.jlsh.aifit.feature.progress.ui

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jlsh.aifit.R
import com.jlsh.aifit.core.ui.components.display.AdherenceBar
import com.jlsh.aifit.core.ui.components.display.AiFitCard
import com.jlsh.aifit.core.ui.components.layout.AiFitTopBar
import com.jlsh.aifit.core.ui.components.layout.ScreenScaffold
import com.jlsh.aifit.core.ui.components.layout.SectionHeader
import com.jlsh.aifit.core.ui.theme.AIFitTheme
import com.jlsh.aifit.core.ui.theme.AiFitSpacing
import com.jlsh.aifit.feature.progress.domain.model.WeeklyProgressSummary
import com.jlsh.aifit.feature.progress.ui.state.ProgressUiEvent
import com.jlsh.aifit.feature.progress.ui.state.WeeklySummaryUiState

@Composable
fun WeeklySummaryScreen(
    onNavigateBack: () -> Unit,
    viewModel: ProgressViewModel = hiltViewModel(),
) {
    val state by viewModel.weeklySummaryState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.loadWeeklySummary()
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is ProgressUiEvent.ShowSnackbar -> snackbarHostState.showSnackbar(event.message)
                is ProgressUiEvent.NavigateBack -> onNavigateBack()
                else -> {}
            }
        }
    }

    ScreenScaffold<WeeklySummaryUiState.Success>(
        uiState = state,
        snackbarHostState = snackbarHostState,
        topBar = {
            AiFitTopBar(
                title = stringResource(R.string.progress_weekly_title),
                onBack = onNavigateBack,
            )
        },
        onRetry = viewModel::loadWeeklySummary,
    ) { paddingValues, successState ->
        WeeklySummaryContent(
            summary = successState.summary,
            modifier = Modifier.padding(paddingValues),
        )
    }
}

@Composable
private fun WeeklySummaryContent(
    summary: WeeklyProgressSummary,
    modifier: Modifier = Modifier,
) {
    val workoutPercentage = if (summary.workoutsTarget > 0) {
        (summary.workoutsThisWeek.toFloat() / summary.workoutsTarget * 100f)
    } else {
        0f
    }

    val caloriePercentage = if (summary.calorieTarget > 0) {
        (summary.averageCaloriesToday.toFloat() / summary.calorieTarget * 100f)
    } else {
        0f
    }

    LazyColumn(
        contentPadding = PaddingValues(
            start = AiFitSpacing.md,
            end = AiFitSpacing.md,
            top = AiFitSpacing.sm,
            bottom = 88.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(AiFitSpacing.md),
        modifier = modifier.fillMaxSize(),
    ) {
        // Workouts
        item(key = "workouts") {
            SectionHeader(title = stringResource(R.string.progress_weekly_workouts_header))
            AiFitCard {
                Column(
                    modifier = Modifier.padding(AiFitSpacing.md),
                    verticalArrangement = Arrangement.spacedBy(AiFitSpacing.sm),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "${summary.workoutsThisWeek} / ${summary.workoutsTarget}",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = stringResource(R.string.progress_weekly_sessions),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    AdherenceBar(percentage = workoutPercentage)
                }
            }
        }

        // Nutrition
        item(key = "nutrition") {
            SectionHeader(title = stringResource(R.string.progress_weekly_nutrition_header))
            AiFitCard {
                Column(
                    modifier = Modifier.padding(AiFitSpacing.md),
                    verticalArrangement = Arrangement.spacedBy(AiFitSpacing.sm),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column {
                            Text(
                                text = "${summary.averageCaloriesToday.toInt()}",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Text(
                                text = stringResource(R.string.progress_weekly_avg_kcal),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "${summary.calorieTarget}",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primaryContainer,
                            )
                            Text(
                                text = stringResource(R.string.progress_weekly_goal),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    AdherenceBar(percentage = caloriePercentage)
                }
            }
        }

        // Streak
        item(key = "streak") {
            SectionHeader(title = stringResource(R.string.progress_weekly_streak_header))
            AiFitCard {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(AiFitSpacing.md),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(AiFitSpacing.md),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.LocalFireDepartment,
                        contentDescription = stringResource(R.string.progress_weekly_streak_cd),
                        tint = MaterialTheme.colorScheme.primaryContainer,
                    )
                    Column {
                        Text(
                            text = stringResource(R.string.progress_weekly_streak_days, summary.currentStreak),
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = stringResource(R.string.progress_weekly_streak_current),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }

        // Weight section removed — weight is displayed in BodyWeightScreen
    }
}

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "WeeklySummary Dark",
)
@Composable
private fun WeeklySummaryPreview() {
    AIFitTheme(darkTheme = true) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
        ) {
            WeeklySummaryContent(
                summary = WeeklyProgressSummary(
                    workoutsThisWeek = 3,
                    workoutsTarget = 5,
                    averageCaloriesToday = 2100.0,
                    calorieTarget = 2200,
                    currentStreak = 5,
                    bodyWeight = 76.2,
                ),
            )
        }
    }
}

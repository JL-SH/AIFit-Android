package com.jlsh.aifit.feature.progress.ui

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jlsh.aifit.core.ui.components.buttons.PrimaryButton
import com.jlsh.aifit.core.ui.components.buttons.SecondaryButton
import com.jlsh.aifit.core.ui.components.display.AdherenceBar
import com.jlsh.aifit.core.ui.components.display.AiFitCard
import com.jlsh.aifit.core.ui.components.display.ChartEntry
import com.jlsh.aifit.core.ui.components.display.LineChartView
import com.jlsh.aifit.core.ui.components.inputs.AiFitChipGroup
import com.jlsh.aifit.core.ui.components.layout.AiFitTopBar
import com.jlsh.aifit.core.ui.components.layout.ScreenScaffold
import com.jlsh.aifit.core.ui.components.layout.SectionHeader
import com.jlsh.aifit.core.ui.theme.AIFitTheme
import com.jlsh.aifit.core.ui.theme.AiFitSpacing
import com.jlsh.aifit.feature.progress.domain.model.NutritionAdherence
import com.jlsh.aifit.feature.progress.domain.model.ProgressDashboard
import com.jlsh.aifit.feature.progress.domain.model.StrengthProgress
import com.jlsh.aifit.feature.progress.domain.model.WeightEntry
import com.jlsh.aifit.feature.progress.domain.model.WeightProgress
import com.jlsh.aifit.feature.progress.domain.model.WeightTrend
import com.jlsh.aifit.feature.progress.domain.model.WorkoutAdherence
import com.jlsh.aifit.feature.progress.ui.state.DashboardUiState
import com.jlsh.aifit.feature.progress.ui.state.ProgressUiEvent
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val PERIOD_OPTIONS = listOf("7 days", "30 days", "90 days")

@Composable
fun ProgressDashboardScreen(
    onNavigateBack: () -> Unit,
    onNavigateToBodyWeight: () -> Unit,
    onNavigateToWeeklySummary: () -> Unit,
    onNavigateToMetabolic: () -> Unit,
    viewModel: ProgressViewModel = hiltViewModel(),
) {
    val dashboardState by viewModel.dashboardState.collectAsStateWithLifecycle()
    val selectedPeriod by viewModel.selectedPeriod.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is ProgressUiEvent.NavigateToBodyWeight -> onNavigateToBodyWeight()
                is ProgressUiEvent.NavigateToWeeklySummary -> onNavigateToWeeklySummary()
                is ProgressUiEvent.NavigateToMetabolic -> onNavigateToMetabolic()
                is ProgressUiEvent.NavigateBack -> onNavigateBack()
                is ProgressUiEvent.ShowSnackbar -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    ScreenScaffold<DashboardUiState.Success>(
        uiState = dashboardState,
        snackbarHostState = snackbarHostState,
        topBar = {
            AiFitTopBar(
                title = "Progress Dashboard",
                onBack = onNavigateBack,
            )
        },
        onRetry = viewModel::onRefreshDashboard,
    ) { paddingValues, successState ->
        DashboardContent(
            state = successState,
            selectedPeriod = selectedPeriod,
            onPeriodSelected = viewModel::onPeriodSelected,
            onLogWeight = viewModel::onNavigateToBodyWeight,
            onWeeklyDetail = viewModel::onNavigateToWeeklySummary,
            onMetabolic = viewModel::onNavigateToMetabolic,
            modifier = Modifier.padding(paddingValues),
        )
    }
}

@Composable
private fun DashboardContent(
    state: DashboardUiState.Success,
    selectedPeriod: String,
    onPeriodSelected: (String) -> Unit,
    onLogWeight: () -> Unit,
    onWeeklyDetail: () -> Unit,
    onMetabolic: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val dashboard = state.dashboard
    val dateFormatter = remember { DateTimeFormatter.ofPattern("dd/MM") }

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
        // Period selector
        item(key = "period") {
            AiFitChipGroup(
                options = PERIOD_OPTIONS,
                selected = setOf(selectedPeriod),
                onSelectionChanged = { selection ->
                    selection.firstOrNull()?.let { onPeriodSelected(it) }
                },
                multiSelect = false,
            )
        }

        // Workout Adherence
        item(key = "workout") {
            SectionHeader(title = "WORKOUT ADHERENCE")
            AiFitCard {
                Column(
                    modifier = Modifier.padding(AiFitSpacing.md),
                    verticalArrangement = Arrangement.spacedBy(AiFitSpacing.sm),
                ) {
                    AdherenceBar(percentage = dashboard.workoutAdherence.adherencePercentage.toFloat())

                    Spacer(modifier = Modifier.height(AiFitSpacing.xs))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        StatItem(
                            value = "${dashboard.workoutAdherence.completedSessions}/${dashboard.workoutAdherence.plannedSessions}",
                            label = "SESSIONS",
                        )
                        StatItem(
                            value = "${dashboard.workoutAdherence.currentStreak}",
                            label = "STREAK",
                        )
                        StatItem(
                            value = "${dashboard.workoutAdherence.longestStreak}",
                            label = "BEST",
                        )
                    }
                }
            }
        }

        // Weight Trend
        item(key = "weight") {
            SectionHeader(title = "WEIGHT TREND")
            AiFitCard {
                Column(
                    modifier = Modifier.padding(AiFitSpacing.md),
                    verticalArrangement = Arrangement.spacedBy(AiFitSpacing.sm),
                ) {
                    LineChartView(
                        entries = dashboard.weightProgress.entries.map { entry ->
                            ChartEntry(
                                label = entry.date.format(dateFormatter),
                                value = entry.weight.toFloat(),
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        StatItem(
                            value = dashboard.weightProgress.startWeight
                                ?.let { String.format(java.util.Locale.getDefault(), "%.1f", it) } ?: "--",
                            label = "START",
                        )
                        StatItem(
                            value = dashboard.weightProgress.currentWeight
                                ?.let { String.format(java.util.Locale.getDefault(), "%.1f", it) } ?: "--",
                            label = "CURRENT",
                        )
                        StatItem(
                            value = dashboard.weightProgress.change
                                ?.let { String.format(java.util.Locale.getDefault(), "%+.1f", it) } ?: "--",
                            label = "CHANGE",
                            valueColor = when {
                                dashboard.weightProgress.change == null ->
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                dashboard.weightProgress.change <= 0 ->
                                    MaterialTheme.colorScheme.primaryContainer
                                else ->
                                    MaterialTheme.colorScheme.error
                            },
                        )
                    }
                }
            }
        }

        // Nutrition Adherence
        item(key = "nutrition") {
            SectionHeader(title = "NUTRITION ADHERENCE")
            AiFitCard {
                Column(
                    modifier = Modifier.padding(AiFitSpacing.md),
                    verticalArrangement = Arrangement.spacedBy(AiFitSpacing.sm),
                ) {
                    AdherenceBar(percentage = dashboard.nutritionAdherence.adherencePercentage.toFloat())

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        StatItem(
                            value = "${dashboard.nutritionAdherence.averageCalories.toInt()}",
                            label = "AVG KCAL",
                        )
                        StatItem(
                            value = "${dashboard.nutritionAdherence.calorieTarget}",
                            label = "TARGET",
                        )
                    }
                }
            }
        }

        // Strength Progress
        if (dashboard.strengthProgress.isNotEmpty()) {
            item(key = "strength_header") {
                SectionHeader(title = "STRENGTH PROGRESS")
            }
            items(dashboard.strengthProgress, key = { it.exerciseName }) { progress ->
                AiFitCard {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(AiFitSpacing.md),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = progress.exerciseName,
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Text(
                                text = "${String.format("%.1f", progress.startMax)} → ${String.format("%.1f", progress.currentMax)} kg",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Text(
                            text = "${String.format("%+.0f", progress.changePercentage)}%",
                            style = MaterialTheme.typography.titleMedium,
                            color = if (progress.changePercentage >= 0)
                                MaterialTheme.colorScheme.primaryContainer
                            else
                                MaterialTheme.colorScheme.error,
                        )
                    }
                }
            }
        }

        // Action buttons
        item(key = "actions") {
            Spacer(modifier = Modifier.height(AiFitSpacing.sm))
            Column(verticalArrangement = Arrangement.spacedBy(AiFitSpacing.sm)) {
                PrimaryButton(
                    text = "LOG WEIGHT",
                    onClick = onLogWeight,
                    modifier = Modifier.fillMaxWidth(),
                )
                SecondaryButton(
                    text = "WEEKLY DETAIL",
                    onClick = onWeeklyDetail,
                    modifier = Modifier.fillMaxWidth(),
                )
                SecondaryButton(
                    text = "METABOLIC ANALYSIS",
                    onClick = onMetabolic,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun StatItem(
    value: String,
    label: String,
    valueColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.primaryContainer,
) {
    Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = valueColor,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "ProgressDashboard Dark",
)
@Composable
private fun ProgressDashboardPreview() {
    AIFitTheme(darkTheme = true) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
        ) {
            DashboardContent(
                state = DashboardUiState.Success(
                    dashboard = ProgressDashboard(
                        periodFrom = LocalDate.now().minusDays(30),
                        periodTo = LocalDate.now(),
                        workoutAdherence = WorkoutAdherence(
                            plannedSessions = 12,
                            completedSessions = 9,
                            adherencePercentage = 75.0,
                            currentStreak = 5,
                            longestStreak = 8,
                        ),
                        weightProgress = WeightProgress(
                            startWeight = 78.5,
                            currentWeight = 76.2,
                            targetWeight = 74.0,
                            change = -2.3,
                            trend = WeightTrend.LOSING,
                            entries = listOf(
                                WeightEntry(LocalDate.now().minusDays(6), 78.5),
                                WeightEntry(LocalDate.now().minusDays(5), 78.1),
                                WeightEntry(LocalDate.now().minusDays(4), 77.8),
                                WeightEntry(LocalDate.now().minusDays(3), 77.2),
                                WeightEntry(LocalDate.now().minusDays(1), 76.5),
                                WeightEntry(LocalDate.now(), 76.2),
                            ),
                        ),
                        nutritionAdherence = NutritionAdherence(
                            averageCalories = 2050.0,
                            calorieTarget = 2200,
                            adherencePercentage = 93.2,
                        ),
                        strengthProgress = listOf(
                            StrengthProgress("Bench Press", 60.0, 70.0, 16.7),
                            StrengthProgress("Squat", 80.0, 95.0, 18.8),
                        ),
                    ),
                ),
                selectedPeriod = "30 days",
                onPeriodSelected = {},
                onLogWeight = {},
                onWeeklyDetail = {},
                onMetabolic = {},
            )
        }
    }
}




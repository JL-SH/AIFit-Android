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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jlsh.aifit.R
import com.jlsh.aifit.core.ui.components.buttons.PrimaryButton
import com.jlsh.aifit.core.ui.components.buttons.SecondaryButton
import com.jlsh.aifit.core.ui.components.display.AdherenceBar
import com.jlsh.aifit.core.ui.components.display.AiFitCard
import com.jlsh.aifit.core.ui.components.display.CardVariant
import com.jlsh.aifit.core.ui.components.display.MetricStatItem
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

/**
 * Pantalla del dashboard de progreso: adherencia, tendencia de peso, nutrici?n y fuerza.
 *
 * Muestra un selector de periodo (chips), tarjetas con gr?ficos y barras de adherencia,
 * y accesos a registro de peso, detalle semanal y an?lisis metab?lico. Gestiona estados
 * de carga/error mediante [ScreenScaffold] y reacciona a [ProgressUiEvent] del [viewModel].
 *
 * @param onNavigateBack Callback al pulsar atr?s en la barra superior.
 * @param onNavigateToBodyWeight Navegaci?n a la pantalla de peso corporal.
 * @param onNavigateToWeeklySummary Navegaci?n al resumen semanal.
 * @param onNavigateToMetabolic Navegaci?n al an?lisis metab?lico.
 * @param viewModel Origen del [DashboardUiState] y eventos de navegaci?n.
 */
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
                is ProgressUiEvent.WeightLoggedSuccessfully -> Unit
            }
        }
    }

    ScreenScaffold<DashboardUiState.Success>(
        uiState = dashboardState,
        snackbarHostState = snackbarHostState,
        topBar = {
            AiFitTopBar(
                title = stringResource(R.string.progress_dashboard_title),
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
    val periodOptions = listOf(
        stringResource(R.string.progress_dashboard_period_7d),
        stringResource(R.string.progress_dashboard_period_30d),
        stringResource(R.string.progress_dashboard_period_90d),
    )

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
                options = periodOptions,
                selected = setOf(selectedPeriod),
                onSelectionChanged = { selection ->
                    selection.firstOrNull()?.let { onPeriodSelected(it) }
                },
                multiSelect = false,
            )
        }

        // Workout Adherence
        item(key = "workout") {
            SectionHeader(title = stringResource(R.string.progress_dashboard_workout_adherence))
            AiFitCard(variant = CardVariant.Subtle) {
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
                        MetricStatItem(
                            value = "${dashboard.workoutAdherence.completedSessions}/${dashboard.workoutAdherence.plannedSessions}",
                            label = stringResource(R.string.progress_dashboard_sessions),
                        )
                        MetricStatItem(
                            targetInt = dashboard.workoutAdherence.currentStreak,
                            label = stringResource(R.string.progress_dashboard_streak),
                        )
                        MetricStatItem(
                            targetInt = dashboard.workoutAdherence.longestStreak,
                            label = stringResource(R.string.progress_dashboard_best),
                        )
                    }
                }
            }
        }

        // Weight Trend
        item(key = "weight") {
            SectionHeader(title = stringResource(R.string.progress_dashboard_weight_trend))
            AiFitCard(variant = CardVariant.Subtle) {
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

                    val startWeight = dashboard.weightProgress.startWeight
                    val currentWeight = dashboard.weightProgress.currentWeight
                    val weightChange = dashboard.weightProgress.change
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        if (startWeight != null) {
                            MetricStatItem(
                                targetFloat = startWeight.toFloat(),
                                label = stringResource(R.string.progress_dashboard_start),
                            )
                        } else {
                            MetricStatItem(
                                value = "--",
                                label = stringResource(R.string.progress_dashboard_start),
                            )
                        }
                        if (currentWeight != null) {
                            MetricStatItem(
                                targetFloat = currentWeight.toFloat(),
                                label = stringResource(R.string.progress_dashboard_current),
                            )
                        } else {
                            MetricStatItem(
                                value = "--",
                                label = stringResource(R.string.progress_dashboard_current),
                            )
                        }
                        if (weightChange != null) {
                            MetricStatItem(
                                targetFloat = weightChange.toFloat(),
                                label = stringResource(R.string.progress_dashboard_change),
                                prefix = if (weightChange >= 0) "+" else "",
                                valueColor = if (weightChange <= 0) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.error
                                },
                            )
                        } else {
                            MetricStatItem(
                                value = "--",
                                label = stringResource(R.string.progress_dashboard_change),
                                valueColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }

        // Nutrition Adherence
        item(key = "nutrition") {
            SectionHeader(title = stringResource(R.string.progress_dashboard_nutrition_adherence))
            AiFitCard(variant = CardVariant.Subtle) {
                Column(
                    modifier = Modifier.padding(AiFitSpacing.md),
                    verticalArrangement = Arrangement.spacedBy(AiFitSpacing.sm),
                ) {
                    AdherenceBar(percentage = dashboard.nutritionAdherence.adherencePercentage.toFloat())

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        MetricStatItem(
                            targetInt = dashboard.nutritionAdherence.averageCalories.toInt(),
                            label = stringResource(R.string.progress_dashboard_avg_kcal),
                        )
                        MetricStatItem(
                            targetInt = dashboard.nutritionAdherence.calorieTarget,
                            label = stringResource(R.string.progress_dashboard_goal),
                        )
                    }
                }
            }
        }

        // Strength Progress
        if (dashboard.strengthProgress.isNotEmpty()) {
            item(key = "strength_header") {
                SectionHeader(title = stringResource(R.string.progress_dashboard_strength))
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
                                text = "${String.format("%.1f", progress.startMax)}  ${String.format("%.1f", progress.currentMax)} kg",
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
                    text = stringResource(R.string.progress_dashboard_log_weight_btn),
                    onClick = onLogWeight,
                    modifier = Modifier.fillMaxWidth(),
                )
                SecondaryButton(
                    text = stringResource(R.string.progress_dashboard_weekly_detail_btn),
                    onClick = onWeeklyDetail,
                    modifier = Modifier.fillMaxWidth(),
                )
                SecondaryButton(
                    text = stringResource(R.string.progress_dashboard_metabolic_btn),
                    onClick = onMetabolic,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
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

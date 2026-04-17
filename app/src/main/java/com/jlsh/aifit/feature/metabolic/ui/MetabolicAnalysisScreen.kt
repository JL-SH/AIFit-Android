package com.jlsh.aifit.feature.metabolic.ui

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.TrendingDown
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jlsh.aifit.R
import com.jlsh.aifit.core.ui.components.buttons.PrimaryButton
import com.jlsh.aifit.core.ui.components.display.AdherenceBar
import com.jlsh.aifit.core.ui.components.display.AiFitCard
import com.jlsh.aifit.core.ui.components.display.PlanStatusBadge
import com.jlsh.aifit.core.ui.components.feedback.ConfirmationDialog
import com.jlsh.aifit.core.ui.components.feedback.EmptyStateView
import com.jlsh.aifit.core.ui.components.layout.AiFitTopBar
import com.jlsh.aifit.core.ui.components.layout.ScreenScaffold
import com.jlsh.aifit.core.ui.components.layout.SectionHeader
import com.jlsh.aifit.core.ui.theme.AIFitTheme
import com.jlsh.aifit.core.ui.theme.AiFitSpacing
import com.jlsh.aifit.feature.metabolic.domain.model.AdjustmentMagnitude
import com.jlsh.aifit.feature.metabolic.domain.model.AdjustmentType
import com.jlsh.aifit.feature.metabolic.domain.model.AdjustmentUrgency
import com.jlsh.aifit.feature.metabolic.domain.model.MetabolicAdjustmentRecommendation
import com.jlsh.aifit.feature.metabolic.domain.model.MetabolicAnalysis
import com.jlsh.aifit.feature.metabolic.domain.model.MetabolicInsight
import com.jlsh.aifit.feature.metabolic.domain.model.MetabolicStatus
import com.jlsh.aifit.feature.metabolic.domain.model.WeightTrend
import com.jlsh.aifit.feature.metabolic.ui.state.MetabolicUiEvent
import com.jlsh.aifit.feature.metabolic.ui.state.MetabolicUiState

@Composable
fun MetabolicAnalysisScreen(
    onNavigateBack: () -> Unit,
    viewModel: MetabolicViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is MetabolicUiEvent.AdjustmentApplied -> { /* stay on screen, data refreshed */ }
                is MetabolicUiEvent.NavigateBack -> onNavigateBack()
                is MetabolicUiEvent.ShowSnackbar -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    // Estado específico: datos insuficientes — mostrar pantalla dedicada (puede venir por InsufficientData
    // o por Success con status == INSUFFICIENT_DATA devuelto por el backend)
    val currentState = uiState
    val isInsufficientData = currentState is MetabolicUiState.InsufficientData ||
        (currentState is MetabolicUiState.Success &&
            currentState.analysis.status == MetabolicStatus.INSUFFICIENT_DATA)
    if (isInsufficientData) {
        MetabolicInsufficientDataScreen(onBack = onNavigateBack)
        return
    }

    ScreenScaffold<MetabolicUiState.Success>(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        topBar = {
            AiFitTopBar(
                title = stringResource(R.string.metabolic_title),
                onBack = onNavigateBack,
                background = MaterialTheme.colorScheme.background,
            )
        },
        onRetry = viewModel::loadAll,
    ) { paddingValues, successState ->
        MetabolicContent(
            paddingValues = paddingValues,
            state = successState,
            onApplyAdjustment = viewModel::onApplyAdjustment,
        )
    }
}

@Composable
private fun MetabolicInsufficientDataScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            AiFitTopBar(
                title = stringResource(R.string.metabolic_title),
                onBack = onBack,
                background = MaterialTheme.colorScheme.background,
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp),
                modifier = Modifier.padding(horizontal = 16.dp),
            ) {
                EmptyStateView(
                    icon = Icons.Rounded.BarChart,
                    title = stringResource(R.string.metabolic_insufficient_data_title),
                    subtitle = stringResource(R.string.metabolic_insufficient_data_subtitle),
                )
                PrimaryButton(
                    text = stringResource(R.string.common_back),
                    onClick = onBack,
                )
            }
        }
    }
}

@Composable
private fun MetabolicContent(
    paddingValues: PaddingValues,
    state: MetabolicUiState.Success,
    onApplyAdjustment: () -> Unit,
) {
    var showConfirmDialog by remember { mutableStateOf(false) }

    LazyColumn(
        contentPadding = PaddingValues(
            start = AiFitSpacing.md,
            end = AiFitSpacing.md,
            top = AiFitSpacing.sm,
            bottom = 88.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(AiFitSpacing.md),
        modifier = Modifier.padding(paddingValues),
    ) {
        // 1. Status badge
        item(key = "status") {
            StatusSection(status = state.analysis.status)
        }

        // 2. Weight Trend
        item(key = "weight_trend") {
            val trend = state.analysis.weightTrend
            if (trend != null && trend.trend != "INSUFFICIENT_DATA") {
                WeightTrendCard(trend = trend)
            }
        }

        // 3. Calorie Adherence
        item(key = "adherence") {
            CalorieAdherenceCard(
                adherenceRate = state.analysis.calorieAdherenceRate,
                deficitSurplus = state.analysis.averageCalorieDeficitSurplus,
            )
        }

        // 4. Rationale
        item(key = "rationale") {
            RationaleCard(rationale = state.analysis.rationale)
        }

        // 5. Recommendation
        item(key = "recommendation") {
            if (state.analysis.recommendation != null) {
                RecommendationCard(
                    recommendation = state.analysis.recommendation,
                    isApplying = state.isApplying,
                    onApply = { showConfirmDialog = true },
                )
            } else {
                AiFitCard {
                    Column(modifier = Modifier.padding(AiFitSpacing.md)) {
                        Text(
                            text = stringResource(R.string.metabolic_no_adjustment_title),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Spacer(modifier = Modifier.height(AiFitSpacing.xs))
                        Text(
                            text = stringResource(R.string.metabolic_no_adjustment_subtitle),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }

        // 6. Insights history
        if (state.insights.isNotEmpty()) {
            item(key = "insights_header") {
                SectionHeader(title = stringResource(R.string.metabolic_insights_header))
            }
            items(state.insights, key = { it.id }) { insight ->
                InsightRow(insight = insight)
            }
        }
    }

    if (showConfirmDialog && state.analysis.recommendation != null) {
        val rec = state.analysis.recommendation
        ConfirmationDialog(
            title = stringResource(R.string.metabolic_apply_dialog_title),
            message = stringResource(
                R.string.metabolic_apply_dialog_message,
                rec.suggestedCalorieTarget,
                "%.0f".format(rec.suggestedProteinTarget),
                "%.0f".format(rec.suggestedCarbsTarget),
                "%.0f".format(rec.suggestedFatTarget),
            ),
            confirmText = stringResource(R.string.metabolic_apply_btn),
            onConfirm = {
                showConfirmDialog = false
                onApplyAdjustment()
            },
            onDismiss = { showConfirmDialog = false },
        )
    }
}

// ── 1. Status ────────────────────────────────────────────────────────────────

@Composable
private fun StatusSection(status: MetabolicStatus) {
    val statusLabel = when (status) {
        MetabolicStatus.ON_TRACK -> stringResource(R.string.metabolic_status_on_track)
        MetabolicStatus.STAGNATED -> stringResource(R.string.metabolic_status_stagnated)
        MetabolicStatus.UNDER_EATING_SIGNAL -> stringResource(R.string.metabolic_status_under_eating)
        MetabolicStatus.OVER_EATING_SIGNAL -> stringResource(R.string.metabolic_status_over_eating)
        MetabolicStatus.PROGRESSING_TOO_FAST -> stringResource(R.string.metabolic_status_too_fast)
        MetabolicStatus.INSUFFICIENT_DATA -> stringResource(R.string.metabolic_status_insufficient_data)
        MetabolicStatus.UNKNOWN -> stringResource(R.string.metabolic_status_unknown)
    }
    PlanStatusBadge(status = statusLabel)
}

// ── 2. Weight Trend ──────────────────────────────────────────────────────────

@Composable
private fun WeightTrendCard(trend: WeightTrend) {
    val trendIcon = when {
        trend.averageWeeklyChange > 0.05 -> Icons.AutoMirrored.Rounded.TrendingUp
        trend.averageWeeklyChange < -0.05 -> Icons.AutoMirrored.Rounded.TrendingDown
        else -> Icons.Rounded.Remove
    }

    AiFitCard {
        Column(
            modifier = Modifier.padding(AiFitSpacing.md),
            verticalArrangement = Arrangement.spacedBy(AiFitSpacing.sm),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AiFitSpacing.sm),
            ) {
                Text(
                    text = stringResource(R.string.metabolic_weight_trend_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Icon(
                    imageVector = trendIcon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(20.dp),
                )
            }

            TrendStatRow(stringResource(R.string.metabolic_avg_weekly_change), "${"%.2f".format(trend.averageWeeklyChange)} kg")
            TrendStatRow(stringResource(R.string.metabolic_expected_weekly_change), "${"%.2f".format(trend.expectedWeeklyChange)} kg")
            TrendStatRow(stringResource(R.string.metabolic_deviation), "${"%.2f".format(trend.deviationFromExpected)} kg")
            TrendStatRow(stringResource(R.string.metabolic_data_points), "${trend.dataPoints}")
            TrendStatRow(stringResource(R.string.metabolic_trend_label), translateTrend(trend.trend))
        }
    }
}

@Composable
private fun TrendStatRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

// ── 3. Calorie Adherence ─────────────────────────────────────────────────────

@Composable
private fun CalorieAdherenceCard(
    adherenceRate: Double,
    deficitSurplus: Double,
) {
    val sign = if (deficitSurplus >= 0) "+" else ""

    AiFitCard {
        Column(
            modifier = Modifier.padding(AiFitSpacing.md),
            verticalArrangement = Arrangement.spacedBy(AiFitSpacing.sm),
        ) {
            Text(
                text = stringResource(R.string.metabolic_calorie_adherence_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            AdherenceBar(percentage = (adherenceRate * 100).toFloat().coerceIn(0f, 100f))
            Text(
                text = "$sign${"%.0f".format(deficitSurplus)} kcal/día",
                style = MaterialTheme.typography.titleSmall,
                color = if (deficitSurplus >= 0) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.error,
            )
        }
    }
}

// ── 4. Rationale ─────────────────────────────────────────────────────────────

@Composable
private fun RationaleCard(rationale: String) {
    AiFitCard {
        Column(modifier = Modifier.padding(AiFitSpacing.md)) {
            Text(
                text = stringResource(R.string.metabolic_rationale_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.height(AiFitSpacing.xs))
            Text(
                text = rationale,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

// ── 5. Recommendation ────────────────────────────────────────────────────────

@Composable
private fun RecommendationCard(
    recommendation: MetabolicAdjustmentRecommendation,
    isApplying: Boolean,
    onApply: () -> Unit,
) {
    AiFitCard(
        containerColor = MaterialTheme.colorScheme.secondaryContainer,
    ) {
        Column(
            modifier = Modifier.padding(AiFitSpacing.md),
            verticalArrangement = Arrangement.spacedBy(AiFitSpacing.sm),
        ) {
            Text(
                text = stringResource(R.string.metabolic_recommendation_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
            )

            PlanStatusBadge(status = translateAdjustmentType(recommendation.type))

            TrendStatRow(stringResource(R.string.metabolic_suggested_calories), "${recommendation.suggestedCalorieTarget} kcal")
            TrendStatRow(stringResource(R.string.macro_protein), "${"%.0f".format(recommendation.suggestedProteinTarget)}g")
            TrendStatRow(stringResource(R.string.macro_carbs), "${"%.0f".format(recommendation.suggestedCarbsTarget)}g")
            TrendStatRow(stringResource(R.string.diet_detail_macro_fat), "${"%.0f".format(recommendation.suggestedFatTarget)}g")

            Row(
                horizontalArrangement = Arrangement.spacedBy(AiFitSpacing.sm),
            ) {
                PlanStatusBadge(status = translateAdjustmentMagnitude(recommendation.magnitude))
                PlanStatusBadge(status = translateAdjustmentUrgency(recommendation.urgency))
            }

            PrimaryButton(
                text = if (isApplying) stringResource(R.string.metabolic_applying_btn) else stringResource(R.string.metabolic_apply_adjustment_btn),
                onClick = onApply,
                enabled = !isApplying,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

// ── 6. Insights ──────────────────────────────────────────────────────────────

@Composable
private fun InsightRow(insight: MetabolicInsight) {
    AiFitCard {
        Column(
            modifier = Modifier.padding(AiFitSpacing.md),
            verticalArrangement = Arrangement.spacedBy(AiFitSpacing.xs),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                PlanStatusBadge(status = translateMetabolicStatus(insight.statusAtTime))
                Text(
                    text = insight.appliedAt.take(10),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(AiFitSpacing.xs),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "${insight.previousCalorieTarget}",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = "→",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primaryContainer,
                )
                Text(
                    text = "${insight.newCalorieTarget} kcal",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primaryContainer,
                )
                PlanStatusBadge(status = translateAdjustmentMagnitude(insight.magnitude))
            }

            Text(
                text = insight.rationale,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
            )
        }
    }
}

// ── Helpers de traducción ─────────────────────────────────────────────────────

@Composable
private fun translateMetabolicStatus(status: MetabolicStatus): String = when (status) {
    MetabolicStatus.ON_TRACK -> stringResource(R.string.metabolic_status_on_track)
    MetabolicStatus.STAGNATED -> stringResource(R.string.metabolic_status_stagnated)
    MetabolicStatus.UNDER_EATING_SIGNAL -> stringResource(R.string.metabolic_status_under_eating)
    MetabolicStatus.OVER_EATING_SIGNAL -> stringResource(R.string.metabolic_status_over_eating)
    MetabolicStatus.PROGRESSING_TOO_FAST -> stringResource(R.string.metabolic_status_too_fast)
    MetabolicStatus.INSUFFICIENT_DATA -> stringResource(R.string.metabolic_status_insufficient_data)
    MetabolicStatus.UNKNOWN -> stringResource(R.string.metabolic_status_unknown)
}

@Composable
private fun translateAdjustmentType(type: AdjustmentType): String = when (type) {
    AdjustmentType.INCREASE_CALORIES -> stringResource(R.string.metabolic_adjustment_increase_calories)
    AdjustmentType.DECREASE_CALORIES -> stringResource(R.string.metabolic_adjustment_decrease_calories)
    AdjustmentType.INCREASE_PROTEIN -> stringResource(R.string.metabolic_adjustment_increase_protein)
    AdjustmentType.REBALANCE_MACROS -> stringResource(R.string.metabolic_adjustment_rebalance_macros)
    AdjustmentType.MAINTAIN -> stringResource(R.string.metabolic_adjustment_maintain)
    AdjustmentType.UNKNOWN -> stringResource(R.string.metabolic_adjustment_unknown)
}

@Composable
private fun translateAdjustmentMagnitude(magnitude: AdjustmentMagnitude): String = when (magnitude) {
    AdjustmentMagnitude.MINOR -> stringResource(R.string.metabolic_magnitude_minor)
    AdjustmentMagnitude.MODERATE -> stringResource(R.string.metabolic_magnitude_moderate)
    AdjustmentMagnitude.SIGNIFICANT -> stringResource(R.string.metabolic_magnitude_significant)
    AdjustmentMagnitude.UNKNOWN -> stringResource(R.string.metabolic_magnitude_unknown)
}

@Composable
private fun translateAdjustmentUrgency(urgency: AdjustmentUrgency): String = when (urgency) {
    AdjustmentUrgency.INFORMATIONAL -> stringResource(R.string.metabolic_urgency_informational)
    AdjustmentUrgency.SUGGESTED -> stringResource(R.string.metabolic_urgency_suggested)
    AdjustmentUrgency.RECOMMENDED -> stringResource(R.string.metabolic_urgency_recommended)
    AdjustmentUrgency.UNKNOWN -> stringResource(R.string.metabolic_urgency_unknown)
}

@Composable
private fun translateTrend(trend: String): String = when (trend.uppercase()) {
    "STABLE" -> stringResource(R.string.metabolic_trend_stable)
    "LOSING" -> stringResource(R.string.metabolic_trend_losing)
    "GAINING" -> stringResource(R.string.metabolic_trend_gaining)
    "INSUFFICIENT_DATA" -> stringResource(R.string.metabolic_status_insufficient_data)
    else -> trend
}

// ── Preview ──────────────────────────────────────────────────────────────────

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "MetabolicAnalysisScreen Dark",
)
@Composable
private fun MetabolicScreenPreview() {
    AIFitTheme(darkTheme = true) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
        ) {
            MetabolicContent(
                paddingValues = PaddingValues(),
                state = MetabolicUiState.Success(
                    analysis = MetabolicAnalysis(
                        status = MetabolicStatus.STAGNATED,
                        weightTrend = WeightTrend(
                            averageWeeklyChange = -0.05,
                            trend = "STABLE",
                            expectedWeeklyChange = -0.5,
                            deviationFromExpected = 0.45,
                            dataPoints = 14,
                        ),
                        calorieAdherenceRate = 0.82,
                        averageCalorieDeficitSurplus = 150.0,
                        recommendation = MetabolicAdjustmentRecommendation(
                            type = AdjustmentType.DECREASE_CALORIES,
                            suggestedCalorieTarget = 2050,
                            suggestedProteinTarget = 140.0,
                            suggestedCarbsTarget = 220.0,
                            suggestedFatTarget = 65.0,
                            magnitude = AdjustmentMagnitude.MINOR,
                            urgency = AdjustmentUrgency.SUGGESTED,
                        ),
                        rationale = "Your weight has plateaued for 2 weeks despite being in a caloric deficit. Consider reducing calories slightly to restart progress.",
                    ),
                    insights = listOf(
                        MetabolicInsight(
                            id = "1",
                            statusAtTime = MetabolicStatus.PROGRESSING_TOO_FAST,
                            adjustmentType = AdjustmentType.INCREASE_CALORIES,
                            previousCalorieTarget = 1800,
                            newCalorieTarget = 2000,
                            magnitude = AdjustmentMagnitude.MODERATE,
                            rationale = "Weight loss was too rapid, risking muscle loss.",
                            appliedAt = "2025-02-15T10:00:00Z",
                        ),
                    ),
                ),
                onApplyAdjustment = {},
            )
        }
    }
}


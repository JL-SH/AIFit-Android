package com.jlsh.aifit.feature.gamification.ui

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jlsh.aifit.R
import com.jlsh.aifit.core.ui.components.buttons.PrimaryButton
import com.jlsh.aifit.core.ui.components.display.AiFitCard
import com.jlsh.aifit.core.ui.components.feedback.InlineLoadingIndicator
import com.jlsh.aifit.core.ui.components.inputs.AiFitChipGroup
import com.jlsh.aifit.core.ui.components.layout.AiFitTopBar
import com.jlsh.aifit.core.ui.theme.AIFitTheme
import com.jlsh.aifit.core.ui.theme.AiFitSpacing
import com.jlsh.aifit.feature.gamification.domain.model.ExportPeriod
import com.jlsh.aifit.feature.gamification.domain.model.ProgressExport
import com.jlsh.aifit.feature.gamification.domain.model.toExportPeriodDisplayString
import com.jlsh.aifit.feature.gamification.ui.state.ExportUiState

@Composable
fun ProgressExportScreen(
    onNavigateBack: () -> Unit,
    viewModel: GamificationViewModel = hiltViewModel(),
) {
    val exportState by viewModel.exportState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    var selectedPeriod by remember { mutableStateOf(ExportPeriod.LAST_MONTH) }

    // Auto-trigger export with the pre-selected period on first composition.
    LaunchedEffect(Unit) {
        viewModel.loadExport(selectedPeriod)
    }

    // Build localized labels for each period using string resources.
    // These are the values passed directly to AiFitChipGroup so that no raw enum
    // names (e.g. "LAST_THREE_MONTHS") are ever visible in the chip UI.
    val periodLabels: Map<ExportPeriod, String> = mapOf(
        ExportPeriod.LAST_WEEK         to stringResource(R.string.export_period_last_week),
        ExportPeriod.LAST_MONTH        to stringResource(R.string.export_period_last_month),
        ExportPeriod.LAST_THREE_MONTHS to stringResource(R.string.export_period_last_3_months),
        ExportPeriod.ALL_TIME          to stringResource(R.string.export_period_all_time),
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                AiFitTopBar(
                    title = stringResource(R.string.gamification_export_title),
                    onBack = onNavigateBack,
                    background = MaterialTheme.colorScheme.background,
                )
            },
            snackbarHost = { SnackbarHost(snackbarHostState) },
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = AiFitSpacing.md),
            ) {
                Spacer(modifier = Modifier.height(AiFitSpacing.md))

                // Options and selected are already-localized strings; no displayMapper needed.
                // This avoids passing a lambda with stringResource captures into a chip slot,
                // which can violate Compose's composable-context rules and cause a crash.
                AiFitChipGroup(
                    options = ExportPeriod.entries.map { periodLabels.getValue(it) },
                    selected = setOf(periodLabels.getValue(selectedPeriod)),
                    onSelectionChanged = { selectedLabels ->
                        val label = selectedLabels.firstOrNull() ?: return@AiFitChipGroup
                        val period = periodLabels.entries
                            .firstOrNull { it.value == label }?.key
                            ?: return@AiFitChipGroup
                        selectedPeriod = period
                        viewModel.loadExport(period)
                    },
                    multiSelect = false,
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(AiFitSpacing.lg))

                when (val state = exportState) {
                    is ExportUiState.Idle -> {
                        Text(
                            text = stringResource(R.string.gamification_export_idle_text),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    is ExportUiState.Loading -> {
                        InlineLoadingIndicator(
                            message = stringResource(R.string.gamification_export_loading),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = AiFitSpacing.lg),
                        )
                    }

                    is ExportUiState.Success -> {
                        ExportSummaryCard(
                            export = state.export,
                            periodLabels = periodLabels,
                        )
                        Spacer(modifier = Modifier.height(AiFitSpacing.lg))
                        PrimaryButton(
                            text = stringResource(R.string.gamification_export_share_btn),
                            onClick = { shareExport(context, state.export, periodLabels) },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }

                    is ExportUiState.Error -> {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(
                                text = state.message,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.error,
                            )
                            TextButton(onClick = { viewModel.loadExport(selectedPeriod) }) {
                                Text(stringResource(R.string.gamification_export_retry))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ExportSummaryCard(
    export: ProgressExport,
    periodLabels: Map<ExportPeriod, String>,
) {
    // Resolve the backend period string → localized label.
    // Falls back to toExportPeriodDisplayString() for unknown period values that the
    // server might add in the future before the app is updated.
    val periodDisplay = ExportPeriod.entries
        .firstOrNull { it.apiValue == export.period }
        ?.let { periodLabels[it] }
        ?: export.period.toExportPeriodDisplayString()

    AiFitCard {
        Column(
            modifier = Modifier.padding(AiFitSpacing.md),
            verticalArrangement = Arrangement.spacedBy(AiFitSpacing.sm),
        ) {
            Text(
                text = stringResource(R.string.gamification_export_report_title, export.userName),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = stringResource(R.string.gamification_export_period_row, periodDisplay),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(AiFitSpacing.xs))

            ExportStatRow(stringResource(R.string.gamification_export_total_workouts), "${export.totalWorkouts}")
            ExportStatRow(stringResource(R.string.gamification_export_total_prs), "${export.totalPRs}")
            ExportStatRow(
                stringResource(R.string.gamification_export_current_streak),
                stringResource(R.string.gamification_export_streak_days, export.currentStreak),
            )
            ExportStatRow(stringResource(R.string.gamification_export_achievements), "${export.achievementsUnlocked}")
            export.weightChange?.let {
                ExportStatRow(
                    stringResource(R.string.gamification_export_weight_change),
                    "${"%.1f".format(it)} kg",
                )
            }

            if (export.topExercises.isNotEmpty()) {
                Spacer(modifier = Modifier.height(AiFitSpacing.xs))
                Text(
                    text = stringResource(R.string.gamification_export_top_exercises),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                export.topExercises.forEach { exercise ->
                    Text(
                        text = "· $exercise",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }
    }
}

@Composable
private fun ExportStatRow(label: String, value: String) {
    Box(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.align(Alignment.CenterStart),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.align(Alignment.CenterEnd),
        )
    }
}

/**
 * Builds the plain-text share payload.
 *
 * [periodLabels] is captured from the composable scope so the period label is already
 * localized without needing a Context or stringResource inside this non-composable function.
 * Falls back to [toExportPeriodDisplayString] for unknown period values.
 */
private fun shareExport(
    context: Context,
    export: ProgressExport,
    periodLabels: Map<ExportPeriod, String>,
) {
    val periodDisplay = ExportPeriod.entries
        .firstOrNull { it.apiValue == export.period }
        ?.let { periodLabels[it] }
        ?: export.period.toExportPeriodDisplayString()

    val text = buildString {
        appendLine("AIFit — Informe de progreso — ${export.userName}")
        appendLine("Período: $periodDisplay")
        appendLine()
        appendLine("Entrenamientos completados: ${export.totalWorkouts}")
        appendLine("Récords personales: ${export.totalPRs}")
        appendLine("Racha actual: ${export.currentStreak} días")
        appendLine("Logros desbloqueados: ${export.achievementsUnlocked}")
        export.weightChange?.let { appendLine("Cambio de peso: ${"%.1f".format(it)} kg") }
        if (export.topExercises.isNotEmpty()) {
            appendLine()
            appendLine("Mejores ejercicios:")
            export.topExercises.forEach { appendLine("  · $it") }
        }
    }

    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, text)
        type = "text/plain"
    }
    context.startActivity(Intent.createChooser(sendIntent, "Compartir progreso"))
}

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "ProgressExportScreen Dark",
)
@Composable
private fun ProgressExportScreenPreview() {
    AIFitTheme(darkTheme = true) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
        ) {
            ExportSummaryCard(
                export = ProgressExport(
                    userId = "1",
                    userName = "Carlos García",
                    period = "LAST_MONTH",
                    generatedAt = "2025-03-01T12:00:00Z",
                    totalWorkouts = 18,
                    totalPRs = 5,
                    currentStreak = 12,
                    achievementsUnlocked = 8,
                    weightChange = -1.5,
                    topExercises = listOf("Bench Press: +15%", "Squat: +10%"),
                ),
                // In previews stringResource isn't available; the fallback in ExportSummaryCard
                // will use toExportPeriodDisplayString() when the map doesn't contain the period.
                periodLabels = emptyMap(),
            )
        }
    }
}

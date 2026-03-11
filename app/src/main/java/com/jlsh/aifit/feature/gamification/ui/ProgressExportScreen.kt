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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jlsh.aifit.core.ui.components.buttons.PrimaryButton
import com.jlsh.aifit.core.ui.components.display.AiFitCard
import com.jlsh.aifit.core.ui.components.feedback.InlineLoadingIndicator
import com.jlsh.aifit.core.ui.components.inputs.AiFitChipGroup
import com.jlsh.aifit.core.ui.components.layout.AiFitTopBar
import com.jlsh.aifit.core.ui.theme.AIFitTheme
import com.jlsh.aifit.core.ui.theme.AiFitSpacing
import com.jlsh.aifit.feature.gamification.domain.model.ExportPeriod
import com.jlsh.aifit.feature.gamification.domain.model.ProgressExport
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

    val periodOptions = ExportPeriod.entries.map { it.name }
    val periodDisplayMapper: (String) -> String = { key ->
        when (key) {
            "LAST_WEEK" -> "Last week"
            "LAST_MONTH" -> "Last month"
            "LAST_3_MONTHS" -> "Last 3 months"
            "ALL_TIME" -> "All time"
            else -> key
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                AiFitTopBar(
                    title = "Export Progress",
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

                AiFitChipGroup(
                    options = periodOptions,
                    selected = setOf(selectedPeriod.name),
                    onSelectionChanged = { selected ->
                        val periodName = selected.firstOrNull() ?: return@AiFitChipGroup
                        val period = ExportPeriod.entries.find { it.name == periodName } ?: return@AiFitChipGroup
                        selectedPeriod = period
                        viewModel.loadExport(period)
                    },
                    multiSelect = false,
                    displayMapper = periodDisplayMapper,
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(AiFitSpacing.lg))

                when (val state = exportState) {
                    is ExportUiState.Idle -> {
                        Text(
                            text = "Select a period to generate your progress report",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    is ExportUiState.Loading -> {
                        InlineLoadingIndicator(
                            message = "Generating report...",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = AiFitSpacing.lg),
                        )
                    }

                    is ExportUiState.Success -> {
                        ExportSummaryCard(export = state.export)
                        Spacer(modifier = Modifier.height(AiFitSpacing.lg))
                        PrimaryButton(
                            text = "SHARE",
                            onClick = { shareExport(context, state.export) },
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
                                Text("Retry")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ExportSummaryCard(export: ProgressExport) {
    AiFitCard {
        Column(
            modifier = Modifier.padding(AiFitSpacing.md),
            verticalArrangement = Arrangement.spacedBy(AiFitSpacing.sm),
        ) {
            Text(
                text = "Progress Report — ${export.userName}",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = "Period: ${export.period}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(AiFitSpacing.xs))

            ExportStatRow("Workouts completed", "${export.totalWorkouts}")
            ExportStatRow("Personal records", "${export.totalPRs}")
            ExportStatRow("Current streak", "${export.currentStreak} days")
            ExportStatRow("Achievements unlocked", "${export.achievementsUnlocked}")
            export.weightChange?.let {
                ExportStatRow("Weight change", "${"%.1f".format(it)} kg")
            }

            if (export.topExercises.isNotEmpty()) {
                Spacer(modifier = Modifier.height(AiFitSpacing.xs))
                Text(
                    text = "TOP EXERCISES",
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
    Box(
        modifier = Modifier.fillMaxWidth(),
    ) {
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

private fun shareExport(context: Context, export: ProgressExport) {
    val text = buildString {
        appendLine("AIFit Progress Report — ${export.userName}")
        appendLine("Period: ${export.period}")
        appendLine()
        appendLine("Workouts completed: ${export.totalWorkouts}")
        appendLine("Personal records: ${export.totalPRs}")
        appendLine("Current streak: ${export.currentStreak} days")
        appendLine("Achievements unlocked: ${export.achievementsUnlocked}")
        export.weightChange?.let { appendLine("Weight change: ${"%.1f".format(it)} kg") }
        if (export.topExercises.isNotEmpty()) {
            appendLine()
            appendLine("Top exercises:")
            export.topExercises.forEach { appendLine("  · $it") }
        }
    }

    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, text)
        type = "text/plain"
    }
    context.startActivity(Intent.createChooser(sendIntent, "Share Progress"))
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
            )
        }
    }
}


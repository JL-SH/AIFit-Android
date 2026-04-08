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

    // Auto-trigger export with pre-selected period on first composition
    LaunchedEffect(Unit) {
        viewModel.loadExport(selectedPeriod)
    }

    val periodOptions = ExportPeriod.entries.map { it.name }
    val periodDisplayMapper: (String) -> String = { key ->
        when (key) {
            "LAST_WEEK" -> "Última semana"
            "LAST_MONTH" -> "Último mes"
            "LAST_THREE_MONTHS" -> "Últimos 3 meses"
            "ALL_TIME" -> "Todo el historial"
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
                    title = "Exportar progreso",
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
                            text = "Selecciona un período para generar tu informe de progreso",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    is ExportUiState.Loading -> {
                        InlineLoadingIndicator(
                            message = "Generando informe...",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = AiFitSpacing.lg),
                        )
                    }

                    is ExportUiState.Success -> {
                        ExportSummaryCard(export = state.export)
                        Spacer(modifier = Modifier.height(AiFitSpacing.lg))
                        PrimaryButton(
                            text = "COMPARTIR",
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
                                Text("Reintentar")
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
                text = "Informe de progreso — ${export.userName}",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = "Período: ${export.period}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(AiFitSpacing.xs))

            ExportStatRow("Entrenamientos completados", "${export.totalWorkouts}")
            ExportStatRow("Récords personales", "${export.totalPRs}")
            ExportStatRow("Racha actual", "${export.currentStreak} días")
            ExportStatRow("Logros desbloqueados", "${export.achievementsUnlocked}")
            export.weightChange?.let {
                ExportStatRow("Cambio de peso", "${"%.1f".format(it)} kg")
            }

            if (export.topExercises.isNotEmpty()) {
                Spacer(modifier = Modifier.height(AiFitSpacing.xs))
                Text(
                    text = "MEJORES EJERCICIOS",
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
        appendLine("AIFit — Informe de progreso — ${export.userName}")
        appendLine("Período: ${export.period}")
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
            )
        }
    }
}


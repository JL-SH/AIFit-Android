package com.jlsh.aifit.feature.progress.ui

import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.*
import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jlsh.aifit.R
import com.jlsh.aifit.core.ui.components.buttons.PrimaryButton
import com.jlsh.aifit.core.ui.components.display.AiFitCard
import com.jlsh.aifit.core.ui.components.display.ChartEntry
import com.jlsh.aifit.core.ui.components.display.LineChartView
import com.jlsh.aifit.core.ui.components.feedback.EmptyStateKind
import com.jlsh.aifit.core.ui.components.feedback.EmptyStateView
import com.jlsh.aifit.core.ui.components.feedback.LoadingScreen
import com.jlsh.aifit.core.ui.components.inputs.AiFitNumberField
import com.jlsh.aifit.core.ui.components.inputs.AiFitTextField
import com.jlsh.aifit.core.ui.components.layout.AiFitTopBar
import com.jlsh.aifit.core.ui.components.layout.SectionHeader
import com.jlsh.aifit.core.ui.theme.AIFitTheme
import com.jlsh.aifit.core.ui.theme.AiFitSpacing
import com.jlsh.aifit.feature.progress.domain.model.BodyWeightLog
import com.jlsh.aifit.feature.progress.ui.state.BodyWeightUiState
import com.jlsh.aifit.feature.progress.ui.state.ProgressUiEvent
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun BodyWeightScreen(
    onNavigateBack: () -> Unit,
    viewModel: ProgressViewModel = hiltViewModel(),
) {
    val state by viewModel.bodyWeightState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.loadBodyWeightHistory()
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                AiFitTopBar(
                    title = stringResource(R.string.progress_weight_title),
                    onBack = onNavigateBack,
                    background = MaterialTheme.colorScheme.background,
                )
            },
            snackbarHost = { SnackbarHost(snackbarHostState) },
        ) { paddingValues ->
            if (state.isLoading && state.weightHistory.isEmpty()) {
                LoadingScreen()
            } else {
                BodyWeightContent(
                    state = state,
                    onWeightChanged = viewModel::onWeightChanged,
                    onNotesChanged = viewModel::onWeightNotesChanged,
                    onLogWeight = viewModel::onLogWeight,
                    modifier = Modifier.padding(paddingValues),
                )
            }
        }
    }
}

@Composable
private fun BodyWeightContent(
    state: BodyWeightUiState,
    onWeightChanged: (String) -> Unit,
    onNotesChanged: (String) -> Unit,
    onLogWeight: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val dateFormatter = remember { DateTimeFormatter.ofPattern("dd/MM") }
    val fullDateFormatter = remember { DateTimeFormatter.ofPattern("dd MMM yyyy") }

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
        // Chart
        item(key = "chart") {
            if (state.weightHistory.size >= 2) {
                AiFitCard {
                    Column(modifier = Modifier.padding(AiFitSpacing.md)) {
                        LineChartView(
                            entries = state.weightHistory.map { log ->
                                ChartEntry(
                                    label = log.date.format(dateFormatter),
                                    value = log.weight.toFloat(),
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                        )
                    }
                }
            } else {
                EmptyStateView(
                    icon = PhosphorIcons.Regular.Scales,
                    kind = EmptyStateKind.BodyWeight,
                    title = stringResource(R.string.progress_weight_chart_empty_title),
                    subtitle = stringResource(R.string.progress_weight_chart_empty_subtitle),
                )
            }
        }

        // Log form
        item(key = "form") {
            SectionHeader(title = stringResource(R.string.progress_weight_log_header))
            AiFitCard {
                Column(
                    modifier = Modifier.padding(AiFitSpacing.md),
                    verticalArrangement = Arrangement.spacedBy(AiFitSpacing.md),
                ) {
                    AiFitNumberField(
                        value = state.formWeight,
                        onValueChange = onWeightChanged,
                        label = stringResource(R.string.progress_weight_field_label),
                        suffix = "kg",
                        modifier = Modifier.fillMaxWidth(),
                    )

                    Text(
                        text = stringResource(R.string.progress_weight_date_format, state.formDate.format(fullDateFormatter)),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    AiFitTextField(
                        value = state.formNotes,
                        onValueChange = onNotesChanged,
                        label = stringResource(R.string.progress_weight_notes_label),
                        singleLine = false,
                        modifier = Modifier.fillMaxWidth(),
                    )

                    PrimaryButton(
                        text = stringResource(R.string.progress_weight_log_btn),
                        isLoading = state.isSaving,
                        onClick = onLogWeight,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }

        // Recent entries
        if (state.weightHistory.isNotEmpty()) {
            item(key = "history_header") {
                SectionHeader(title = stringResource(R.string.progress_weight_history_header))
            }
            items(state.weightHistory.take(20), key = { it.id }) { log ->
                val isInitialWeight = log.notes?.contains("Peso inicial", ignoreCase = true) == true
                AiFitCard {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(AiFitSpacing.md),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(AiFitSpacing.sm),
                            ) {
                                Text(
                                    text = log.date.format(fullDateFormatter),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                                if (isInitialWeight) {
                                    Surface(
                                        shape = MaterialTheme.shapes.small,
                                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                                    ) {
                                        Text(
                                            text = stringResource(R.string.progress_weight_initial_badge),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primaryContainer,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        )
                                    }
                                }
                            }
                            if (!log.notes.isNullOrBlank() && !isInitialWeight) {
                                Text(
                                    text = log.notes,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                        Text(
                            text = "${String.format("%.1f", log.weight)} kg",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primaryContainer,
                        )
                    }
                }
            }
        }
    }
}

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "BodyWeightScreen Dark",
)
@Composable
private fun BodyWeightScreenPreview() {
    AIFitTheme(darkTheme = true) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
        ) {
            BodyWeightContent(
                state = BodyWeightUiState(
                    weightHistory = listOf(
                        BodyWeightLog("1", 76.2, LocalDate.now(), null, LocalDate.now()),
                        BodyWeightLog("2", 76.5, LocalDate.now().minusDays(1), "Después de entrenar", LocalDate.now().minusDays(1)),
                        BodyWeightLog("3", 77.0, LocalDate.now().minusDays(3), null, LocalDate.now().minusDays(3)),
                        BodyWeightLog("4", 77.5, LocalDate.now().minusDays(5), null, LocalDate.now().minusDays(5)),
                    ),
                    isLoading = false,
                    formWeight = "",
                    formDate = LocalDate.now(),
                    formNotes = "",
                    isSaving = false,
                ),
                onWeightChanged = {},
                onNotesChanged = {},
                onLogWeight = {},
            )
        }
    }
}





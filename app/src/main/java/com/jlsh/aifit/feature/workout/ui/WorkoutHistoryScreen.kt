package com.jlsh.aifit.feature.workout.ui

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jlsh.aifit.R
import com.jlsh.aifit.core.ui.components.display.AiFitCard
import com.jlsh.aifit.core.ui.components.display.PlanStatusBadge
import com.jlsh.aifit.core.ui.components.feedback.EmptyStateView
import com.jlsh.aifit.core.ui.components.feedback.ErrorScreen
import com.jlsh.aifit.core.ui.components.feedback.LoadingScreen
import com.jlsh.aifit.core.ui.components.inputs.AiFitDatePickerBottomSheet
import com.jlsh.aifit.core.ui.theme.AIFitTheme
import com.jlsh.aifit.core.ui.theme.AiFitSpacing
import com.jlsh.aifit.feature.training.domain.model.TrainingPlan
import com.jlsh.aifit.feature.workout.domain.model.WorkoutLog
import com.jlsh.aifit.feature.workout.ui.state.WorkoutHistoryUiState
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

// ── Day-of-week labels (Spanish) ──

private val DAY_OF_WEEK_ENTRIES = listOf(
    DayOfWeek.MONDAY to "Lun",
    DayOfWeek.TUESDAY to "Mar",
    DayOfWeek.WEDNESDAY to "Mié",
    DayOfWeek.THURSDAY to "Jue",
    DayOfWeek.FRIDAY to "Vie",
    DayOfWeek.SATURDAY to "Sáb",
    DayOfWeek.SUNDAY to "Dom",
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WorkoutHistoryScreen(
    onNavigateToDetail: (logId: String) -> Unit,
    onNavigateBack: () -> Unit = {},
    viewModel: WorkoutViewModel = hiltViewModel(),
) {
    val historyState by viewModel.historyState.collectAsStateWithLifecycle()
    val availablePlans by viewModel.availablePlans.collectAsStateWithLifecycle()
    val selectedPlanFilter by viewModel.selectedPlanFilter.collectAsStateWithLifecycle()
    val dateFrom by viewModel.dateFromFilter.collectAsStateWithLifecycle()
    val dateTo by viewModel.dateToFilter.collectAsStateWithLifecycle()
    val dayOfWeekFilter by viewModel.dayOfWeekFilter.collectAsStateWithLifecycle()

    var showDateFromPicker by remember { mutableStateOf(false) }
    var showDateToPicker by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadAvailablePlans()
        viewModel.loadHistory()
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is com.jlsh.aifit.feature.workout.ui.state.WorkoutUiEvent.NavigateToDetail ->
                    onNavigateToDetail(event.logId)
                else -> {}
            }
        }
    }

    // ── Date picker sheets ──
    AiFitDatePickerBottomSheet(
        isVisible = showDateFromPicker,
        initialDate = dateFrom,
        onDateSelected = { isoDate ->
            showDateFromPicker = false
            viewModel.onDateRangeFilterChanged(from = isoDate, to = dateTo)
        },
        onDismiss = { showDateFromPicker = false },
    )

    AiFitDatePickerBottomSheet(
        isVisible = showDateToPicker,
        initialDate = dateTo,
        onDateSelected = { isoDate ->
            showDateToPicker = false
            viewModel.onDateRangeFilterChanged(from = dateFrom, to = isoDate)
        },
        onDismiss = { showDateToPicker = false },
    )

    when (val state = historyState) {
        is WorkoutHistoryUiState.Loading -> LoadingScreen()
        is WorkoutHistoryUiState.Error -> ErrorScreen(
            message = state.message,
            onRetry = { viewModel.loadHistory() },
        )
        is WorkoutHistoryUiState.Success -> {
            Column(modifier = Modifier.fillMaxSize()) {
                // ── Filters bar ──
                HistoryFiltersBar(
                    plans = availablePlans,
                    selectedPlanId = selectedPlanFilter,
                    dateFrom = dateFrom,
                    dateTo = dateTo,
                    selectedDayOfWeek = dayOfWeekFilter,
                    onPlanSelected = { viewModel.onPlanFilterChanged(it) },
                    onDateFromClick = { showDateFromPicker = true },
                    onDateToClick = { showDateToPicker = true },
                    onClearDateFilter = {
                        viewModel.onDateRangeFilterChanged(from = null, to = null)
                    },
                    onDayOfWeekSelected = { viewModel.onDayOfWeekFilterChanged(it) },
                )

                if (state.logs.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = AiFitSpacing.xxl),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        EmptyStateView(
                            icon = Icons.Rounded.FitnessCenter,
                            title = stringResource(R.string.workout_history_empty_title),
                            subtitle = stringResource(R.string.workout_history_empty_subtitle),
                        )
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(
                            start = AiFitSpacing.md,
                            top = AiFitSpacing.sm,
                            end = AiFitSpacing.md,
                            bottom = AiFitSpacing.xxl + AiFitSpacing.xxl,
                        ),
                        verticalArrangement = Arrangement.spacedBy(AiFitSpacing.sm),
                    ) {
                        state.logsByMonth.forEach { (monthLabel, logsInMonth) ->
                            // ── Month header ──
                            item(key = "header_$monthLabel") {
                                Text(
                                    text = monthLabel,
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                    ),
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(
                                        top = AiFitSpacing.md,
                                        bottom = AiFitSpacing.xs,
                                    ),
                                )
                            }

                            items(logsInMonth, key = { it.id }) { log ->
                                WorkoutLogCard(
                                    log = log,
                                    planName = state.planNameMap[log.trainingPlanId],
                                    onClick = { viewModel.onLogClicked(log.id) },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun HistoryFiltersBar(
    plans: List<TrainingPlan>,
    selectedPlanId: String?,
    dateFrom: String?,
    dateTo: String?,
    selectedDayOfWeek: DayOfWeek?,
    onPlanSelected: (String?) -> Unit,
    onDateFromClick: () -> Unit,
    onDateToClick: () -> Unit,
    onClearDateFilter: () -> Unit,
    onDayOfWeekSelected: (DayOfWeek?) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = AiFitSpacing.md)
            .padding(top = AiFitSpacing.sm, bottom = AiFitSpacing.xs),
        verticalArrangement = Arrangement.spacedBy(AiFitSpacing.xs),
    ) {
        // ── Plan chips ──
        if (plans.isNotEmpty()) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(AiFitSpacing.sm),
                verticalArrangement = Arrangement.spacedBy(AiFitSpacing.xs),
            ) {
                // "All" chip
                FilterChip(
                    selected = selectedPlanId == null,
                    onClick = { onPlanSelected(null) },
                    label = {
                        Text(
                            text = stringResource(R.string.workout_history_filter_all),
                            style = MaterialTheme.typography.labelMedium,
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    ),
                )

                plans.forEach { plan ->
                    FilterChip(
                        selected = selectedPlanId == plan.id,
                        onClick = { onPlanSelected(plan.id) },
                        label = {
                            Text(
                                text = plan.name,
                                style = MaterialTheme.typography.labelMedium,
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        ),
                    )
                }
            }
        }

        // ── Day of week chips ──
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(AiFitSpacing.sm),
            verticalArrangement = Arrangement.spacedBy(AiFitSpacing.xs),
        ) {
            DAY_OF_WEEK_ENTRIES.forEach { (day, label) ->
                FilterChip(
                    selected = selectedDayOfWeek == day,
                    onClick = {
                        onDayOfWeekSelected(if (selectedDayOfWeek == day) null else day)
                    },
                    label = {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelMedium,
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onTertiaryContainer,
                    ),
                )
            }
        }

        // ── Date filter chips ──
        Row(
            horizontalArrangement = Arrangement.spacedBy(AiFitSpacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            FilterChip(
                selected = dateFrom != null,
                onClick = onDateFromClick,
                label = {
                    Text(
                        text = dateFrom ?: stringResource(R.string.workout_history_filter_from),
                        style = MaterialTheme.typography.labelMedium,
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Rounded.CalendarMonth,
                        contentDescription = null,
                        modifier = Modifier.height(16.dp),
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ),
            )

            FilterChip(
                selected = dateTo != null,
                onClick = onDateToClick,
                label = {
                    Text(
                        text = dateTo ?: stringResource(R.string.workout_history_filter_to),
                        style = MaterialTheme.typography.labelMedium,
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Rounded.CalendarMonth,
                        contentDescription = null,
                        modifier = Modifier.height(16.dp),
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ),
            )

            if (dateFrom != null || dateTo != null) {
                FilterChip(
                    selected = false,
                    onClick = onClearDateFilter,
                    label = {
                        Text(
                            text = "✕",
                            style = MaterialTheme.typography.labelMedium,
                        )
                    },
                )
            }
        }
    }
}

@Composable
private fun WorkoutLogCard(
    log: WorkoutLog,
    planName: String? = null,
    onClick: () -> Unit,
) {
    val dayLabel = log.date.dayOfWeek
        .getDisplayName(TextStyle.FULL, Locale("es"))
        .replaceFirstChar { it.uppercase() }

    AiFitCard(onClick = onClick) {
        Column(
            modifier = Modifier.padding(AiFitSpacing.md),
            verticalArrangement = Arrangement.spacedBy(AiFitSpacing.xs),
        ) {
            // Row 1: Date with day name + status badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "$dayLabel, ${log.date.format(DateTimeFormatter.ofPattern("dd MMM yyyy"))}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                if (log.isLocked) {
                    PlanStatusBadge(status = "COMPLETED")
                } else {
                    PlanStatusBadge(status = "DRAFT")
                }
            }

            // Row 2: Plan name
            if (planName != null) {
                Text(
                    text = planName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            // Row 3: Duration, exercises, sets, RPE
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(AiFitSpacing.md),
                ) {
                    log.durationMinutes?.let { dur ->
                        Text(
                            text = "${dur}min",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Text(
                        text = "${log.totalExercises} ejercicios",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    if (log.sets.isNotEmpty()) {
                        Text(
                            text = stringResource(R.string.workout_history_sets, log.sets.size),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                log.perceivedExertion?.let { rpe ->
                    Text(
                        text = "RPE $rpe",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primaryContainer,
                    )
                }
            }
        }
    }
}

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "WorkoutHistoryScreen Dark",
)
@Composable
private fun WorkoutHistoryScreenPreview() {
    AIFitTheme(darkTheme = true) {
        val fakeLogs = listOf(
            WorkoutLog(
                id = "1", trainingPlanId = "p1", trainingDayId = "d1",
                date = LocalDate.now(), durationMinutes = 55,
                perceivedExertion = 7, notes = null, totalExercises = 5,
                completedAt = LocalDateTime.now(),
                isLocked = true,
            ),
            WorkoutLog(
                id = "2", trainingPlanId = "p1", trainingDayId = "d2",
                date = LocalDate.now().minusDays(2), durationMinutes = 42,
                perceivedExertion = 6, notes = null, totalExercises = 4,
                completedAt = LocalDateTime.now().minusDays(2),
                isLocked = false,
            ),
        )
        Column(modifier = Modifier.fillMaxSize()) {
            HistoryFiltersBar(
                plans = emptyList(),
                selectedPlanId = null,
                dateFrom = null,
                dateTo = null,
                selectedDayOfWeek = null,
                onPlanSelected = {},
                onDateFromClick = {},
                onDateToClick = {},
                onClearDateFilter = {},
                onDayOfWeekSelected = {},
            )
            LazyColumn(
                contentPadding = PaddingValues(AiFitSpacing.md),
                verticalArrangement = Arrangement.spacedBy(AiFitSpacing.sm),
            ) {
                items(fakeLogs, key = { it.id }) { log ->
                    WorkoutLogCard(log = log, planName = "Plan Fuerza 4x", onClick = {})
                }
            }
        }
    }
}

// U-12/U-13 compliance fix — light mode preview
@Preview(
    showBackground = true,
    name = "WorkoutHistoryScreen Light",
)
@Composable
private fun WorkoutHistoryScreenLightPreview() {
    AIFitTheme(darkTheme = false) {
        val fakeLogs = listOf(
            WorkoutLog(
                id = "1", trainingPlanId = "p1", trainingDayId = "d1",
                date = LocalDate.now(), durationMinutes = 55,
                perceivedExertion = 7, notes = null, totalExercises = 5,
                completedAt = LocalDateTime.now(),
                isLocked = true,
            ),
        )
        Column(modifier = Modifier.fillMaxSize()) {
            HistoryFiltersBar(
                plans = emptyList(),
                selectedPlanId = null,
                dateFrom = null,
                dateTo = null,
                selectedDayOfWeek = null,
                onPlanSelected = {},
                onDateFromClick = {},
                onDateToClick = {},
                onClearDateFilter = {},
                onDayOfWeekSelected = {},
            )
            LazyColumn(
                contentPadding = PaddingValues(AiFitSpacing.md),
                verticalArrangement = Arrangement.spacedBy(AiFitSpacing.sm),
            ) {
                items(fakeLogs, key = { it.id }) { log ->
                    WorkoutLogCard(log = log, planName = "Plan Hipertrofia", onClick = {})
                }
            }
        }
    }
}


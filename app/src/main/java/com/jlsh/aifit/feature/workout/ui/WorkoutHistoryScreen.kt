package com.jlsh.aifit.feature.workout.ui

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jlsh.aifit.core.ui.components.display.AiFitCard
import com.jlsh.aifit.core.ui.components.feedback.EmptyStateView
import com.jlsh.aifit.core.ui.components.feedback.ErrorScreen
import com.jlsh.aifit.core.ui.components.feedback.LoadingScreen
import com.jlsh.aifit.core.ui.theme.AIFitTheme
import com.jlsh.aifit.core.ui.theme.AiFitSpacing
import com.jlsh.aifit.feature.workout.domain.model.WorkoutLog
import com.jlsh.aifit.feature.workout.ui.state.WorkoutHistoryUiState
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun WorkoutHistoryScreen(
    onNavigateToDetail: (logId: String) -> Unit,
    viewModel: WorkoutViewModel = hiltViewModel(),
) {
    val historyState by viewModel.historyState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
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

    when (val state = historyState) {
        is WorkoutHistoryUiState.Loading -> LoadingScreen()
        is WorkoutHistoryUiState.Error -> ErrorScreen(
            message = state.message,
            onRetry = { viewModel.loadHistory() },
        )
        is WorkoutHistoryUiState.Success -> {
            if (state.logs.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = AiFitSpacing.xxl),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    EmptyStateView(
                        icon = Icons.Rounded.FitnessCenter,
                        title = "No has registrado sesiones aún",
                        subtitle = "Completa tu primer entrenamiento para ver el historial",
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(
                        start = AiFitSpacing.md,
                        top = AiFitSpacing.sm,
                        end = AiFitSpacing.md,
                        bottom = 88.dp,
                    ),
                    verticalArrangement = Arrangement.spacedBy(AiFitSpacing.sm),
                ) {
                    items(state.logs, key = { it.id }) { log ->
                        WorkoutLogCard(
                            log = log,
                            onClick = { viewModel.onLogClicked(log.id) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WorkoutLogCard(
    log: WorkoutLog,
    onClick: () -> Unit,
) {
    AiFitCard(onClick = onClick) {
        Column(
            modifier = Modifier.padding(AiFitSpacing.md),
            verticalArrangement = Arrangement.spacedBy(AiFitSpacing.xs),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = log.date.format(DateTimeFormatter.ofPattern("dd MMM yyyy")),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                log.perceivedExertion?.let { rpe ->
                    Text(
                        text = "RPE $rpe",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primaryContainer,
                    )
                }
            }
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
                    text = "${log.totalExercises} exercises",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
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
            ),
            WorkoutLog(
                id = "2", trainingPlanId = "p1", trainingDayId = "d2",
                date = LocalDate.now().minusDays(2), durationMinutes = 42,
                perceivedExertion = 6, notes = null, totalExercises = 4,
                completedAt = LocalDateTime.now().minusDays(2),
            ),
        )
        LazyColumn(
            contentPadding = PaddingValues(AiFitSpacing.md),
            verticalArrangement = Arrangement.spacedBy(AiFitSpacing.sm),
        ) {
            items(fakeLogs, key = { it.id }) { log ->
                WorkoutLogCard(log = log, onClick = {})
            }
        }
    }
}


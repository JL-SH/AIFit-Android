package com.jlsh.aifit.feature.workout.ui

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jlsh.aifit.core.ui.components.layout.AiFitTopBar
import com.jlsh.aifit.core.ui.components.layout.ScreenScaffold
import com.jlsh.aifit.core.ui.theme.AIFitTheme
import com.jlsh.aifit.core.ui.theme.AiFitSpacing
import com.jlsh.aifit.feature.workout.domain.model.WorkoutLog
import com.jlsh.aifit.feature.workout.domain.model.WorkoutSetLog
import com.jlsh.aifit.feature.workout.ui.state.WorkoutDetailUiState
import com.jlsh.aifit.feature.workout.ui.state.WorkoutUiEvent
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun WorkoutDetailScreen(
    logId: String,
    onNavigateBack: () -> Unit,
    viewModel: WorkoutViewModel = hiltViewModel(),
) {
    val detailState by viewModel.detailState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(logId) {
        viewModel.loadLogDetail(logId)
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is WorkoutUiEvent.NavigateBack -> onNavigateBack()
                is WorkoutUiEvent.ShowSnackbar -> snackbarHostState.showSnackbar(event.message)
                else -> {}
            }
        }
    }

    ScreenScaffold<WorkoutDetailUiState.Success>(
        uiState = detailState,
        snackbarHostState = snackbarHostState,
        topBar = {
            AiFitTopBar(
                title = "Workout Detail",
                onBack = onNavigateBack,
            )
        },
        onRetry = { viewModel.loadLogDetail(logId) },
    ) { paddingValues, successState ->
        WorkoutDetailContent(
            log = successState.log,
            modifier = Modifier.padding(paddingValues),
        )
    }
}

@Composable
private fun WorkoutDetailContent(
    log: WorkoutLog,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = AiFitSpacing.md,
            top = AiFitSpacing.sm,
            end = AiFitSpacing.md,
            bottom = 88.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(AiFitSpacing.sm),
    ) {
        // Summary header
        item(key = "summary") {
            Column(
                verticalArrangement = Arrangement.spacedBy(AiFitSpacing.xs),
            ) {
                Text(
                    text = log.date.format(DateTimeFormatter.ofPattern("EEEE, dd MMM yyyy")),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(AiFitSpacing.md),
                ) {
                    log.durationMinutes?.let { dur ->
                        Text(
                            text = "${dur} min",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    log.perceivedExertion?.let { rpe ->
                        Text(
                            text = "RPE $rpe/10",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primaryContainer,
                        )
                    }
                    Text(
                        text = "${log.totalExercises} exercises",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                log.notes?.let { notes ->
                    if (notes.isNotBlank()) {
                        Text(
                            text = notes,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(AiFitSpacing.sm))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)
            }
        }

        // Group sets by exercise
        val groupedSets = log.sets.groupBy { it.trainingExerciseId }
        groupedSets.forEach { (_, sets) ->
            val exerciseName = sets.firstOrNull()?.exerciseName ?: "Unknown"

            item(key = "exercise_header_${sets.firstOrNull()?.trainingExerciseId}") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = AiFitSpacing.sm),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = exerciseName,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Row {
                        IconButton(
                            onClick = { /* Stub — Sprint 10 */ },
                            modifier = Modifier.size(32.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Info,
                                contentDescription = "Info",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    }
                }
            }

            items(sets, key = { it.id }) { set ->
                SetDetailRow(set = set)
            }
        }
    }
}

@Composable
private fun SetDetailRow(set: WorkoutSetLog) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "Set ${set.exerciseSetNumber}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(0.2f),
        )
        Text(
            text = set.weightUsed?.let { "${it}kg" } ?: "—",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(0.25f),
        )
        Text(
            text = set.repsCompleted?.let { "${it} reps" } ?: "—",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(0.25f),
        )
        Icon(
            imageVector = if (set.completed) Icons.Rounded.Check else Icons.Rounded.Close,
            contentDescription = if (set.completed) "Completed" else "Not completed",
            tint = if (set.completed) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.error,
            modifier = Modifier.size(20.dp),
        )
    }
}

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "WorkoutDetailScreen Dark",
)
@Composable
private fun WorkoutDetailScreenPreview() {
    AIFitTheme(darkTheme = true) {
        val fakeLog = WorkoutLog(
            id = "1", trainingPlanId = "p1", trainingDayId = "d1",
            date = LocalDate.now(), durationMinutes = 55,
            perceivedExertion = 7, notes = "Good session",
            totalExercises = 2, completedAt = LocalDateTime.now(),
            sets = listOf(
                WorkoutSetLog("s1", "e1", "Bench Press", 1, 10, 80.0, null, true),
                WorkoutSetLog("s2", "e1", "Bench Press", 2, 8, 80.0, null, true),
                WorkoutSetLog("s3", "e1", "Bench Press", 3, 6, 80.0, null, false),
                WorkoutSetLog("s4", "e2", "Overhead Press", 1, 12, 40.0, null, true),
                WorkoutSetLog("s5", "e2", "Overhead Press", 2, 10, 40.0, null, true),
            ),
        )
        WorkoutDetailContent(log = fakeLog)
    }
}




package com.jlsh.aifit.feature.workout.ui

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jlsh.aifit.core.ui.components.layout.AiFitTopBar
import com.jlsh.aifit.core.ui.components.layout.ScreenScaffold
import com.jlsh.aifit.core.ui.theme.AIFitTheme
import com.jlsh.aifit.core.ui.theme.AiFitSpacing
import com.jlsh.aifit.feature.education.ui.EducationViewModel
import com.jlsh.aifit.feature.education.ui.components.ExerciseExplanationSheet
import com.jlsh.aifit.feature.progression.ui.ProgressionViewModel
import com.jlsh.aifit.feature.progression.ui.components.ProgressionRecommendationSheet
import com.jlsh.aifit.feature.workout.domain.model.WorkoutLog
import com.jlsh.aifit.feature.workout.domain.model.WorkoutSetLog
import com.jlsh.aifit.feature.workout.ui.state.WorkoutDetailUiState
import com.jlsh.aifit.feature.workout.ui.state.WorkoutUiEvent
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutDetailScreen(
    logId: String,
    onNavigateBack: () -> Unit,
    viewModel: WorkoutViewModel = hiltViewModel(),
    educationViewModel: EducationViewModel = hiltViewModel(),
    progressionViewModel: ProgressionViewModel = hiltViewModel(),
) {
    val detailState by viewModel.detailState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val explanationState by educationViewModel.explanationState.collectAsStateWithLifecycle()
    val recommendationState by progressionViewModel.recommendationState.collectAsStateWithLifecycle()

    var showExplanationForExerciseId by remember { mutableStateOf<String?>(null) }
    var showProgressionForExerciseId by remember { mutableStateOf<String?>(null) }

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

    // ── Sheets ──
    if (showExplanationForExerciseId != null) {
        ExerciseExplanationSheet(
            state = explanationState,
            onDismiss = {
                showExplanationForExerciseId = null
                educationViewModel.resetExplanationState()
            },
            onRetry = { showExplanationForExerciseId?.let { educationViewModel.loadExerciseExplanation(it) } },
        )
    }

    if (showProgressionForExerciseId != null) {
        ProgressionRecommendationSheet(
            state = recommendationState,
            onDismiss = {
                showProgressionForExerciseId = null
                progressionViewModel.resetRecommendationState()
            },
            onRetry = { showProgressionForExerciseId?.let { progressionViewModel.loadExerciseRecommendation(it) } },
        )
    }

    ScreenScaffold<WorkoutDetailUiState.Success>(
        uiState = detailState,
        snackbarHostState = snackbarHostState,
        topBar = {
            AiFitTopBar(
                title = "Sesión de entrenamiento",
                onBack = onNavigateBack,
            )
        },
        onRetry = { viewModel.loadLogDetail(logId) },
    ) { paddingValues, successState ->
        WorkoutDetailContent(
            log = successState.log,
            onExerciseInfoClick = { exerciseId ->
                showExplanationForExerciseId = exerciseId
                educationViewModel.loadExerciseExplanation(exerciseId)
            },
            onExerciseProgressionClick = { exerciseId ->
                showProgressionForExerciseId = exerciseId
                progressionViewModel.loadExerciseRecommendation(exerciseId)
            },
            modifier = Modifier.padding(paddingValues),
        )
    }
}

@Composable
private fun WorkoutDetailContent(
    log: WorkoutLog,
    onExerciseInfoClick: (exerciseId: String) -> Unit = {},
    onExerciseProgressionClick: (exerciseId: String) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val groupedSets = log.sets.groupBy { it.trainingExerciseId }
    val completedSets = log.sets.count { it.completed }
    val totalSets = log.sets.size

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 88.dp),
    ) {

        // ── Hero section ─────────────────────────────────────────────
        item(key = "hero") {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AiFitSpacing.md)
                    .padding(top = AiFitSpacing.md, bottom = AiFitSpacing.lg),
                verticalArrangement = Arrangement.spacedBy(AiFitSpacing.xs),
            ) {
                // Fecha — título dominante
                Text(
                    text = log.date.format(
                        DateTimeFormatter.ofPattern("EEEE, dd MMM yyyy", Locale("es"))
                    ).replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                // Notas opcionales
                log.notes?.let { notes ->
                    if (notes.isNotBlank()) {
                        Spacer(Modifier.height(AiFitSpacing.xs))
                        Text(
                            text = notes,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                Spacer(Modifier.height(AiFitSpacing.sm))

                // Stats row — datos clave de la sesión
                Row(modifier = Modifier.fillMaxWidth()) {
                    log.durationMinutes?.let { dur ->
                        StatCell(
                            value = "$dur",
                            label = "MIN",
                            modifier = Modifier.weight(1f),
                        )
                        StatDivider()
                    }
                    log.perceivedExertion?.let { rpe ->
                        StatCell(
                            value = "$rpe/10",
                            label = "RPE",
                            modifier = Modifier.weight(1f),
                        )
                        StatDivider()
                    }
                    StatCell(
                        value = "${log.totalExercises}",
                        label = "EJERCICIOS",
                        modifier = Modifier.weight(1f),
                    )
                    StatDivider()
                    StatCell(
                        value = "$completedSets/$totalSets",
                        label = "SERIES",
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }

        // Divisor
        item(key = "divider_top") {
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant,
                thickness = 0.5.dp,
            )
            Spacer(Modifier.height(AiFitSpacing.md))
        }

        // Section header
        item(key = "exercises_header") {
            Text(
                text = "EJERCICIOS",
                style = MaterialTheme.typography.labelSmall,
                letterSpacing = 1.5.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AiFitSpacing.md)
                    .padding(bottom = AiFitSpacing.sm),
            )
        }

        // ── Ejercicios agrupados ──────────────────────────────────────
        groupedSets.forEach { (exerciseId, sets) ->
            val exerciseName = sets.firstOrNull()?.exerciseName ?: "Ejercicio"
            val completedInGroup = sets.count { it.completed }

            item(key = "exercise_header_$exerciseId") {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = AiFitSpacing.md)
                        .padding(top = AiFitSpacing.sm),
                ) {
                    // Nombre del ejercicio + badge series + info
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = exerciseName,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f),
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(AiFitSpacing.xs),
                        ) {
                            // Badge series completadas
                            Surface(
                                color = if (completedInGroup == sets.size)
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)
                                else
                                    MaterialTheme.colorScheme.surfaceContainerHigh,
                                shape = RoundedCornerShape(6.dp),
                            ) {
                                Text(
                                    text = "$completedInGroup/${sets.size}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (completedInGroup == sets.size)
                                        MaterialTheme.colorScheme.primaryContainer
                                    else
                                        MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                )
                            }
                            IconButton(
                                onClick = { onExerciseInfoClick(exerciseId) },
                                modifier = Modifier.size(28.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Info,
                                    contentDescription = "Info",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp),
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(AiFitSpacing.xs))

                    // Cabecera de columnas
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "SERIE",
                            style = MaterialTheme.typography.labelSmall,
                            letterSpacing = 1.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(0.15f),
                        )
                        Text(
                            text = "PESO",
                            style = MaterialTheme.typography.labelSmall,
                            letterSpacing = 1.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.weight(0.3f),
                        )
                        Text(
                            text = "REPS",
                            style = MaterialTheme.typography.labelSmall,
                            letterSpacing = 1.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.weight(0.3f),
                        )
                        Text(
                            text = "OK",
                            style = MaterialTheme.typography.labelSmall,
                            letterSpacing = 1.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.End,
                            modifier = Modifier.weight(0.25f),
                        )
                    }

                    Spacer(Modifier.height(4.dp))
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant,
                        thickness = 0.5.dp,
                    )
                }
            }

            items(sets, key = { it.id }) { set ->
                SetDetailRow(
                    set = set,
                    modifier = Modifier.padding(horizontal = AiFitSpacing.md),
                )
            }

            item(key = "divider_$exerciseId") {
                Spacer(Modifier.height(AiFitSpacing.xs))
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant,
                    thickness = 0.5.dp,
                    modifier = Modifier.padding(horizontal = AiFitSpacing.md),
                )
                Spacer(Modifier.height(AiFitSpacing.xs))
            }
        }
    }
}

@Composable
private fun StatCell(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(vertical = AiFitSpacing.sm),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primaryContainer,
            textAlign = TextAlign.Center,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            letterSpacing = 1.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun StatDivider() {
    Box(
        modifier = Modifier.height(32.dp),
        contentAlignment = Alignment.Center,
    ) {
        HorizontalDivider(
            modifier = Modifier.size(width = 0.5.dp, height = 32.dp),
            color = MaterialTheme.colorScheme.outlineVariant,
            thickness = 0.5.dp,
        )
    }
}

@Composable
private fun SetDetailRow(
    set: WorkoutSetLog,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Número de serie
        Text(
            text = "${set.exerciseSetNumber}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(0.15f),
        )
        // Peso — dato importante
        Text(
            text = set.weightUsed?.let { "${it}kg" } ?: "—",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(0.3f),
        )
        // Reps — dato importante
        Text(
            text = set.repsCompleted?.let { "$it reps" } ?: "—",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(0.3f),
        )
        // Estado — icono con color semántico
        Box(
            modifier = Modifier.weight(0.25f),
            contentAlignment = Alignment.CenterEnd,
        ) {
            Icon(
                imageVector = if (set.completed) Icons.Rounded.Check else Icons.Rounded.Close,
                contentDescription = if (set.completed) "Completada" else "No completada",
                tint = if (set.completed)
                    MaterialTheme.colorScheme.primaryContainer
                else
                    MaterialTheme.colorScheme.error,
                modifier = Modifier.size(18.dp),
            )
        }
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
            perceivedExertion = 7, notes = "Buena sesión, subí peso en press banca.",
            totalExercises = 2, completedAt = LocalDateTime.now(),
            sets = listOf(
                WorkoutSetLog("s1", "e1", "Bench Press", 1, 10, 80.0, null, true),
                WorkoutSetLog("s2", "e1", "Bench Press", 2, 8, 80.0, null, true),
                WorkoutSetLog("s3", "e1", "Bench Press", 3, 6, 80.0, null, false),
                WorkoutSetLog("s4", "e2", "Overhead Press", 1, 12, 40.0, null, true),
                WorkoutSetLog("s5", "e2", "Overhead Press", 2, 10, 40.0, null, true),
            ),
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
        ) {
            WorkoutDetailContent(log = fakeLog)
        }
    }
}
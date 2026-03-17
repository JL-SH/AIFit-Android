package com.jlsh.aifit.feature.workout.ui

import android.content.res.Configuration
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jlsh.aifit.core.ui.components.buttons.PrimaryButton
import com.jlsh.aifit.core.ui.components.display.AiFitCard
import com.jlsh.aifit.core.ui.components.inputs.AiFitNumberField
import com.jlsh.aifit.core.ui.components.layout.AiFitTopBar
import com.jlsh.aifit.core.ui.theme.AIFitTheme
import com.jlsh.aifit.core.ui.theme.AiFitSpacing
import com.jlsh.aifit.feature.training.domain.model.MuscleGroup
import com.jlsh.aifit.feature.workout.domain.model.WorkoutSetLog
import com.jlsh.aifit.feature.workout.ui.components.RestTimerBanner
import com.jlsh.aifit.feature.workout.ui.components.VolumePanelSection
import com.jlsh.aifit.feature.workout.ui.state.SessionExercise
import com.jlsh.aifit.feature.workout.ui.state.WorkoutSessionData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutSessionScreen(
    sessionData: WorkoutSessionData,
    onRegisterSet: (exerciseId: String, weightKg: Double, reps: Int, rpe: Int) -> Unit,
    onFinalize: () -> Unit,
    restTimerSeconds: Int? = null,
    onDismissTimer: () -> Unit = {},
    onRequestSubstitution: (exerciseId: String) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val exercises = sessionData.exercises
    val pagerState = rememberPagerState(
        initialPage = sessionData.currentExerciseIndex,
        pageCount = { exercises.size },
    )
    val hasRegisteredSets = sessionData.registeredSets.isNotEmpty()

    Scaffold(
        topBar = {
            AiFitTopBar(
                title = "Exercise ${pagerState.currentPage + 1} of ${exercises.size}",
                actions = {
                    if (hasRegisteredSets) {
                        TextButton(onClick = onFinalize) {
                            Text(
                                text = "Finalize",
                                color = MaterialTheme.colorScheme.primaryContainer,
                            )
                        }
                    }
                },
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier,
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
            ) { page ->
                val exercise = exercises[page]
                val exerciseGhostSets = sessionData.ghostSets
                    .filter { it.trainingExerciseId == exercise.exerciseId }
                val exerciseRegisteredSets = sessionData.registeredSets
                    .filter { it.trainingExerciseId == exercise.exerciseId }

                ExercisePage(
                    exercise = exercise,
                    ghostSets = exerciseGhostSets,
                    registeredSets = exerciseRegisteredSets,
                    autoregulationSuggestion = sessionData.autoregulationSuggestion,
                    volumeByMuscleGroup = sessionData.volumeByMuscleGroup,
                    onRegisterSet = { weight, reps, rpe ->
                        onRegisterSet(exercise.exerciseId, weight, reps, rpe)
                    },
                    onRequestSubstitution = { onRequestSubstitution(exercise.exerciseId) },
                )
            }

            // Rest timer overlay at the bottom
            RestTimerBanner(
                seconds = restTimerSeconds,
                onDismiss = onDismissTimer,
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        }
    }
}

@Composable
private fun ExercisePage(
    exercise: SessionExercise,
    ghostSets: List<WorkoutSetLog>,
    registeredSets: List<WorkoutSetLog>,
    autoregulationSuggestion: Double?,
    volumeByMuscleGroup: Map<MuscleGroup, Double>,
    onRegisterSet: (weightKg: Double, reps: Int, rpe: Int) -> Unit,
    onRequestSubstitution: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = AiFitSpacing.md,
            top = AiFitSpacing.md,
            end = AiFitSpacing.md,
            bottom = AiFitSpacing.xxl,
        ),
        verticalArrangement = Arrangement.spacedBy(AiFitSpacing.sm),
    ) {
        // ── Exercise header ──
        item(key = "header") {
            ExerciseHeader(
                exercise = exercise,
                onRequestSubstitution = onRequestSubstitution,
            )
        }

        // ── Ghost data ──
        if (ghostSets.isNotEmpty()) {
            item(key = "ghost_header") {
                Text(
                    text = "Previous session",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                    modifier = Modifier.padding(top = AiFitSpacing.xs),
                )
            }
            itemsIndexed(ghostSets, key = { _, set -> "ghost_${set.id}" }) { _, set ->
                GhostSetRow(set = set)
            }
        }

        // ── Registered sets ──
        if (registeredSets.isNotEmpty()) {
            item(key = "registered_header") {
                Text(
                    text = "This session",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = AiFitSpacing.sm),
                )
            }
            itemsIndexed(registeredSets, key = { _, set -> "reg_${set.id}" }) { index, set ->
                RegisteredSetRow(set = set)
                if (index == registeredSets.lastIndex && autoregulationSuggestion != null) {
                    AutoregulationChip(suggestion = autoregulationSuggestion)
                }
            }
        }

        // ── Divider ──
        item(key = "divider") {
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant,
                thickness = 0.5.dp,
                modifier = Modifier.padding(vertical = AiFitSpacing.xs),
            )
        }

        // ── Set registration form ──
        item(key = "form") {
            SetRegistrationForm(onRegisterSet = onRegisterSet)
        }

        // ── Volume panel ──
        item(key = "volume_panel") {
            VolumePanelSection(
                volumeByMuscleGroup = volumeByMuscleGroup,
                modifier = Modifier.padding(top = AiFitSpacing.sm),
            )
        }
    }
}

@Composable
private fun ExerciseHeader(
    exercise: SessionExercise,
    onRequestSubstitution: () -> Unit,
) {
    var menuExpanded by remember { mutableStateOf(false) }

    AiFitCard {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AiFitSpacing.md),
            verticalArrangement = Arrangement.spacedBy(AiFitSpacing.xs),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = exercise.name,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                )
                Box {
                    IconButton(
                        onClick = { menuExpanded = true },
                        modifier = Modifier.size(32.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.MoreVert,
                            contentDescription = "Options",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false },
                    ) {
                        DropdownMenuItem(
                            text = { Text("Substitute exercise") },
                            onClick = {
                                menuExpanded = false
                                onRequestSubstitution()
                            },
                        )
                    }
                }
            }
            Text(
                text = exercise.primaryMuscle.name
                    .replace("_", " ")
                    .lowercase()
                    .replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = "${exercise.completedSets}/${exercise.targetSets} sets",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primaryContainer,
            )
        }
    }
}

@Composable
private fun GhostSetRow(set: WorkoutSetLog) {
    val mutedColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = AiFitSpacing.xs),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = "Set ${set.exerciseSetNumber}",
            style = MaterialTheme.typography.bodySmall,
            color = mutedColor,
        )
        Text(
            text = "${set.weightUsed ?: "-"} kg × ${set.repsCompleted ?: "-"} reps",
            style = MaterialTheme.typography.bodySmall,
            color = mutedColor,
        )
        Text(
            text = "RPE ${set.rpe ?: "-"}",
            style = MaterialTheme.typography.bodySmall,
            color = mutedColor,
        )
    }
}

@Composable
private fun RegisteredSetRow(set: WorkoutSetLog) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = AiFitSpacing.xs),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = "Set ${set.exerciseSetNumber}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = "${set.weightUsed ?: "-"} kg × ${set.repsCompleted ?: "-"} reps",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = "RPE ${set.rpe ?: "-"}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = "1RM: ${"%.1f".format(set.estimatedOneRepMax ?: 0.0)}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun AutoregulationChip(suggestion: Double) {
    var visible by remember { mutableStateOf(true) }
    if (!visible) return

    AssistChip(
        onClick = { },
        label = {
            Text(
                text = "Suggestion: ${"%.1f".format(suggestion)} kg for next set",
                style = MaterialTheme.typography.labelMedium,
            )
        },
        trailingIcon = {
            IconButton(
                onClick = { visible = false },
                modifier = Modifier.size(18.dp),
            ) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = "Dismiss",
                    modifier = Modifier.size(14.dp),
                )
            }
        },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            labelColor = MaterialTheme.colorScheme.onSecondaryContainer,
        ),
        modifier = Modifier.padding(top = AiFitSpacing.xs),
    )
}

@Composable
private fun SetRegistrationForm(
    onRegisterSet: (weightKg: Double, reps: Int, rpe: Int) -> Unit,
) {
    var weight by remember { mutableStateOf("") }
    var reps by remember { mutableStateOf("") }
    var rpe by remember { mutableStateOf("") }

    var weightError by remember { mutableStateOf<String?>(null) }
    var repsError by remember { mutableStateOf<String?>(null) }
    var rpeError by remember { mutableStateOf<String?>(null) }

    Column(
        verticalArrangement = Arrangement.spacedBy(AiFitSpacing.sm),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AiFitSpacing.sm),
        ) {
            AiFitNumberField(
                value = weight,
                onValueChange = {
                    weight = it
                    weightError = null
                },
                label = "Weight",
                suffix = "kg",
                error = weightError,
                modifier = Modifier.weight(1f),
            )
            AiFitNumberField(
                value = reps,
                onValueChange = {
                    reps = it
                    repsError = null
                },
                label = "Reps",
                error = repsError,
                modifier = Modifier.weight(1f),
            )
            AiFitNumberField(
                value = rpe,
                onValueChange = {
                    rpe = it
                    rpeError = null
                },
                label = "RPE",
                error = rpeError,
                modifier = Modifier.weight(1f),
            )
        }

        Spacer(modifier = Modifier.height(AiFitSpacing.xs))

        PrimaryButton(
            text = "Add Set",
            onClick = {
                val weightVal = weight.toDoubleOrNull()
                val repsVal = reps.toIntOrNull()
                val rpeVal = rpe.toIntOrNull()

                var hasError = false

                if (weightVal == null || weightVal <= 0) {
                    weightError = "Weight > 0"
                    hasError = true
                }
                if (repsVal == null || repsVal <= 0) {
                    repsError = "Reps > 0"
                    hasError = true
                }
                if (rpeVal == null || rpeVal < 1 || rpeVal > 10) {
                    rpeError = "1–10"
                    hasError = true
                }

                if (!hasError) {
                    onRegisterSet(weightVal!!, repsVal!!, rpeVal!!)
                    weight = ""
                    reps = ""
                    rpe = ""
                }
            },
        )
    }
}

// ── Preview ──────────────────────────────────────────────────────────────

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "WorkoutSessionScreen Dark",
)
@Composable
private fun WorkoutSessionScreenPreview() {
    AIFitTheme(darkTheme = true) {
        val fakeExercises = listOf(
            SessionExercise(
                exerciseId = "e1",
                name = "Bench Press",
                primaryMuscle = MuscleGroup.CHEST,
                targetSets = 5,
                targetReps = 5,
                targetRpe = 8,
                restSeconds = 180,
                completedSets = 2,
            ),
            SessionExercise(
                exerciseId = "e2",
                name = "Overhead Press",
                primaryMuscle = MuscleGroup.SHOULDERS,
                targetSets = 3,
                targetReps = 10,
                targetRpe = 7,
                restSeconds = 120,
                completedSets = 0,
            ),
        )

        val fakeGhostSets = listOf(
            WorkoutSetLog(
                id = "g1", trainingExerciseId = "e1", exerciseName = "Bench Press",
                exerciseSetNumber = 1, repsCompleted = 5, weightUsed = 80.0,
                durationSeconds = null, completed = true, rpe = 7,
            ),
            WorkoutSetLog(
                id = "g2", trainingExerciseId = "e1", exerciseName = "Bench Press",
                exerciseSetNumber = 2, repsCompleted = 5, weightUsed = 82.5,
                durationSeconds = null, completed = true, rpe = 8,
            ),
        )

        val fakeRegisteredSets = listOf(
            WorkoutSetLog(
                id = "r1", trainingExerciseId = "e1", exerciseName = "Bench Press",
                exerciseSetNumber = 1, repsCompleted = 5, weightUsed = 82.5,
                durationSeconds = null, completed = true, estimatedOneRepMax = 96.3, rpe = 8,
            ),
            WorkoutSetLog(
                id = "r2", trainingExerciseId = "e1", exerciseName = "Bench Press",
                exerciseSetNumber = 2, repsCompleted = 5, weightUsed = 85.0,
                durationSeconds = null, completed = true, estimatedOneRepMax = 99.2, rpe = 9,
            ),
        )

        val fakeSessionData = WorkoutSessionData(
            exercises = fakeExercises,
            currentExerciseIndex = 0,
            registeredSets = fakeRegisteredSets,
            autoregulationSuggestion = 80.0,
            restTimerSeconds = null,
            volumeByMuscleGroup = mapOf(
                MuscleGroup.CHEST to 825.0,
                MuscleGroup.SHOULDERS to 0.0,
            ),
            ghostSets = fakeGhostSets,
            substitutions = null,
        )

        WorkoutSessionScreen(
            sessionData = fakeSessionData,
            onRegisterSet = { _, _, _, _ -> },
            onFinalize = {},
            restTimerSeconds = 92,
            onDismissTimer = {},
            onRequestSubstitution = {},
        )
    }
}



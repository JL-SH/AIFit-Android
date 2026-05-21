package com.jlsh.aifit.feature.workout.ui

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jlsh.aifit.R
import com.jlsh.aifit.core.ui.components.buttons.PrimaryButton
import com.jlsh.aifit.core.ui.components.buttons.SecondaryButton
import com.jlsh.aifit.core.ui.components.display.AiFitCard
import com.jlsh.aifit.core.ui.components.feedback.ConfirmationDialog
import com.jlsh.aifit.core.ui.components.feedback.ErrorScreen
import com.jlsh.aifit.core.ui.components.feedback.LoadingScreen
import com.jlsh.aifit.core.ui.components.inputs.AiFitNumberField
import com.jlsh.aifit.core.ui.components.layout.AiFitTopBar
import com.jlsh.aifit.core.ui.theme.AIFitTheme
import com.jlsh.aifit.core.ui.theme.AiFitSpacing
import com.jlsh.aifit.feature.training.domain.model.MuscleGroup
import com.jlsh.aifit.feature.workout.domain.model.WorkoutSetLog
import com.jlsh.aifit.feature.workout.ui.components.FinalizeSessionSheet
import com.jlsh.aifit.feature.workout.ui.components.SubstitutionSheet
import com.jlsh.aifit.feature.workout.ui.components.VolumePanelSection
import com.jlsh.aifit.feature.workout.ui.components.WarmUpSheet
import com.jlsh.aifit.feature.workout.ui.state.SessionExercise
import com.jlsh.aifit.feature.workout.ui.state.WorkoutSessionData
import com.jlsh.aifit.feature.workout.ui.state.WorkoutSessionUiEvent
import com.jlsh.aifit.feature.workout.ui.state.WorkoutSessionUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutSessionScreen(
    onNavigateBack: () -> Unit,
    onSessionFinalized: (workoutLogId: String) -> Unit,
    viewModel: WorkoutSessionViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val restTimerSeconds by viewModel.restTimerSeconds.collectAsStateWithLifecycle()
    val substitutionsState by viewModel.substitutionsState.collectAsStateWithLifecycle()

    var showFinalizeSheet by remember { mutableStateOf(false) }
    var showAbandonDialog by remember { mutableStateOf(false) }
    var showSubstitutionExerciseId by remember { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState) {
        val state = uiState
        if (state is WorkoutSessionUiState.SessionFinalized) {
            onSessionFinalized(state.summary.id)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is WorkoutSessionUiEvent.NavigateBack -> {
                    // onNavigateBack must clear the entire Training sub-graph so the user
                    // lands on HomeScreen, not on a stale TrainingDetail or TrainingHub.
                    // The call site in MainNavGraph.kt is expected to popUpTo(0) inclusive
                    // and navigate to HomeRoutes.GRAPH (same pattern as onSessionFinalized).
                    onNavigateBack()
                }
                is WorkoutSessionUiEvent.ShowSubstitutionSheet -> {
                    showSubstitutionExerciseId = event.exerciseId
                    viewModel.loadSubstitutions(event.exerciseId)
                }
                is WorkoutSessionUiEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(event.message)
                }
                is WorkoutSessionUiEvent.SessionAlreadyLocked -> onNavigateBack()
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when (val state = uiState) {
            is WorkoutSessionUiState.Idle,
            is WorkoutSessionUiState.LoadingWarmUp -> LoadingScreen()

            is WorkoutSessionUiState.WarmUpReady -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background),
                )
                WarmUpSheet(
                    protocol = state.protocol,
                    onSkip = { viewModel.startWorkout(warmupCompleted = false) },
                    onReady = { viewModel.startWorkout(warmupCompleted = true) },
                )
            }

            is WorkoutSessionUiState.SessionActive -> {
                WorkoutSessionContent(
                    sessionData = state.sessionData,
                    onRegisterSet = viewModel::registerSet,
                    onFinalize = { showFinalizeSheet = true },
                    onAbandon = { showAbandonDialog = true },
                    restTimerSeconds = restTimerSeconds,
                    onDismissTimer = viewModel::cancelRestTimer,
                    onRequestSubstitution = { exerciseId ->
                        showSubstitutionExerciseId = exerciseId
                        viewModel.loadSubstitutions(exerciseId)
                    },
                )

                if (showAbandonDialog) {
                    ConfirmationDialog(
                        title = stringResource(R.string.abandon_session_title),
                        message = stringResource(R.string.abandon_session_message),
                        confirmText = stringResource(R.string.abandon_session_dismiss),
                        dismissText = stringResource(R.string.abandon_session_confirm),
                        onConfirm = {
                            showAbandonDialog = false
                        },
                        onDismiss = {
                            showAbandonDialog = false
                            viewModel.abandonSession()
                        },
                    )
                }

                if (showSubstitutionExerciseId != null) {
                    SubstitutionSheet(
                        state = substitutionsState,
                        onSelect = { sub ->
                            viewModel.applySubstitution(showSubstitutionExerciseId!!, sub)
                            showSubstitutionExerciseId = null
                        },
                        onDismiss = { showSubstitutionExerciseId = null },
                    )
                }

                if (showFinalizeSheet) {
                    FinalizeSessionSheet(
                        onConfirm = { fatigue, jointPain ->
                            viewModel.finalizeSession(fatigue, jointPain)
                            showFinalizeSheet = false
                        },
                        onDismiss = { showFinalizeSheet = false },
                    )
                }
            }

            is WorkoutSessionUiState.Finalizing -> LoadingScreen()
            is WorkoutSessionUiState.SessionFinalized -> { /* handled by LaunchedEffect */ }
            is WorkoutSessionUiState.Error -> ErrorScreen(
                message = state.message,
                onRetry = onNavigateBack,
            )
        }
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WorkoutSessionContent(
    sessionData: WorkoutSessionData,
    onRegisterSet: (exerciseId: String, weightKg: Double, reps: Int, rpe: Int?) -> Unit,
    onFinalize: () -> Unit,
    onAbandon: () -> Unit,
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

    LaunchedEffect(sessionData.currentExerciseIndex) {
        if (pagerState.currentPage != sessionData.currentExerciseIndex) {
            pagerState.animateScrollToPage(sessionData.currentExerciseIndex)
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                AiFitTopBar(
                    title = stringResource(R.string.workout_session_exercise_counter, pagerState.currentPage + 1, exercises.size),
                    onBack = onAbandon,
                    actions = {
                        if (hasRegisteredSets) {
                            TextButton(onClick = onFinalize) {
                                Text(
                                    text = stringResource(R.string.workout_session_finalize),
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                )
                            }
                        }
                    },
                )
            },
            containerColor = MaterialTheme.colorScheme.background,
        ) { paddingValues ->
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
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
        }

        RestTimerOverlay(
            seconds = restTimerSeconds,
            exerciseName = exercises.getOrNull(pagerState.currentPage)?.name ?: "",
            onDismiss = onDismissTimer,
        )
    }
}

@Composable
private fun RestTimerOverlay(
    seconds: Int?,
    exerciseName: String,
    onDismiss: () -> Unit,
) {
    val haptic = LocalHapticFeedback.current

    LaunchedEffect(seconds) {
        if (seconds == 0) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    AnimatedVisibility(
        visible = seconds != null,
        enter = fadeIn(),
        exit = fadeOut(),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
        ) {
            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = AiFitSpacing.xxl),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = exerciseName,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(AiFitSpacing.xs))
                Text(
                    text = stringResource(R.string.workout_session_rest_label),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            val displaySeconds = seconds ?: 0
            val minutes = displaySeconds / 60
            val secs = displaySeconds % 60
            Text(
                text = "$minutes:${"%02d".format(secs)}",
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.align(Alignment.Center),
            )

            SecondaryButton(
                text = stringResource(R.string.workout_session_skip_rest),
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(
                        start = AiFitSpacing.md,
                        end = AiFitSpacing.md,
                        bottom = AiFitSpacing.xxl,
                    ),
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
    onRegisterSet: (weightKg: Double, reps: Int, rpe: Int?) -> Unit,
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
        item(key = "header") {
            ExerciseHeader(
                exercise = exercise,
                onRequestSubstitution = onRequestSubstitution,
            )
        }

        if (ghostSets.isNotEmpty()) {
            item(key = "ghost_header") {
                Text(
                    text = stringResource(R.string.workout_session_previous_session),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                    modifier = Modifier.padding(top = AiFitSpacing.xs),
                )
            }
            itemsIndexed(ghostSets, key = { _, set -> "ghost_${set.id}" }) { _, set ->
                GhostSetRow(set = set)
            }
        }

        if (registeredSets.isNotEmpty()) {
            item(key = "registered_header") {
                Text(
                    text = stringResource(R.string.workout_session_current_session),
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

        item(key = "divider") {
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant,
                thickness = 0.5.dp,
                modifier = Modifier.padding(vertical = AiFitSpacing.xs),
            )
        }

        if (exercise.isComplete) {
            item(key = "completed") {
                Text(
                    text = stringResource(R.string.workout_session_exercise_complete),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(vertical = AiFitSpacing.sm),
                )
            }
        } else {
            item(key = "form") {
                SetRegistrationForm(onRegisterSet = onRegisterSet)
            }
        }

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
                            contentDescription = stringResource(R.string.workout_session_options_cd),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false },
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.workout_session_substitute_exercise)) },
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
                text = stringResource(R.string.workout_session_sets_progress, exercise.completedSets, exercise.targetSets),
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
            text = stringResource(R.string.workout_session_set_number, set.exerciseSetNumber),
            style = MaterialTheme.typography.bodySmall,
            color = mutedColor,
        )
        Text(
            text = "${set.weightUsed ?: "-"} kg × ${set.repsCompleted ?: "-"} reps",
            style = MaterialTheme.typography.bodySmall,
            color = mutedColor,
        )
        Text(
            text = if (set.rpe != null) "RPE ${set.rpe}" else "RPE -",
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
            text = stringResource(R.string.workout_session_set_number, set.exerciseSetNumber),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = "${set.weightUsed ?: "-"} kg × ${set.repsCompleted ?: "-"} reps",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = if (set.rpe != null) "RPE ${set.rpe}" else "RPE -",
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
                text = stringResource(R.string.workout_session_suggestion, "%.1f".format(suggestion)),
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
                    contentDescription = stringResource(R.string.workout_session_dismiss_cd),
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
    onRegisterSet: (weightKg: Double, reps: Int, rpe: Int?) -> Unit,
) {
    var weight by remember { mutableStateOf("") }
    var reps by remember { mutableStateOf("") }
    var rpe by remember { mutableStateOf("") }

    var weightError by remember { mutableStateOf<String?>(null) }
    var repsError by remember { mutableStateOf<String?>(null) }
    var rpeError by remember { mutableStateOf<String?>(null) }

    val weightErrorStr = stringResource(R.string.workout_session_weight_error)
    val repsErrorStr = stringResource(R.string.workout_session_reps_error)
    val rpeErrorStr = stringResource(R.string.workout_session_rpe_error)

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
                label = stringResource(R.string.workout_session_weight_label),
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
                label = stringResource(R.string.workout_session_reps_label),
                error = repsError,
                modifier = Modifier.weight(1f),
            )
            AiFitNumberField(
                value = rpe,
                onValueChange = {
                    rpe = it
                    rpeError = null
                },
                label = stringResource(R.string.workout_session_rpe_label),
                error = rpeError,
                modifier = Modifier.weight(1f),
            )
        }

        Spacer(modifier = Modifier.height(AiFitSpacing.xs))

        PrimaryButton(
            text = stringResource(R.string.workout_session_add_set),
            onClick = {
                val weightVal = weight.toDoubleOrNull()
                val repsVal = reps.toIntOrNull()
                val rpeVal: Int? = rpe.toIntOrNull()

                var hasError = false

                if (weightVal == null || weightVal <= 0) {
                    weightError = weightErrorStr
                    hasError = true
                }
                if (repsVal == null || repsVal <= 0) {
                    repsError = repsErrorStr
                    hasError = true
                }
                if (rpeVal != null && (rpeVal < 1 || rpeVal > 10)) {
                    rpeError = rpeErrorStr
                    hasError = true
                }

                if (!hasError) {
                    onRegisterSet(weightVal!!, repsVal!!, rpeVal)
                    weight = ""
                    reps = ""
                    rpe = ""
                }
            },
        )
    }
}

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
                name = "Press de banca",
                primaryMuscle = MuscleGroup.CHEST,
                targetSets = 5,
                targetReps = 5,
                targetRpe = 8,
                restSeconds = 180,
                completedSets = 2,
            ),
            SessionExercise(
                exerciseId = "e2",
                name = "Press militar",
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
                id = "g1", trainingExerciseId = "e1", exerciseName = "Press de banca",
                exerciseSetNumber = 1, repsCompleted = 5, weightUsed = 80.0,
                durationSeconds = null, completed = true, rpe = 7,
            ),
            WorkoutSetLog(
                id = "g2", trainingExerciseId = "e1", exerciseName = "Press de banca",
                exerciseSetNumber = 2, repsCompleted = 5, weightUsed = 82.5,
                durationSeconds = null, completed = true, rpe = 8,
            ),
        )

        val fakeRegisteredSets = listOf(
            WorkoutSetLog(
                id = "r1", trainingExerciseId = "e1", exerciseName = "Press de banca",
                exerciseSetNumber = 1, repsCompleted = 5, weightUsed = 82.5,
                durationSeconds = null, completed = true, estimatedOneRepMax = 96.3, rpe = 8,
            ),
            WorkoutSetLog(
                id = "r2", trainingExerciseId = "e1", exerciseName = "Press de banca",
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

        WorkoutSessionContent(
            sessionData = fakeSessionData,
            onRegisterSet = { _, _, _, _ -> },
            onFinalize = {},
            onAbandon = {},
            restTimerSeconds = 92,
            onDismissTimer = {},
            onRequestSubstitution = {},
        )
    }
}

@Preview(
    showBackground = true,
    name = "WorkoutSessionScreen Light",
)
@Composable
private fun WorkoutSessionScreenLightPreview() {
    AIFitTheme(darkTheme = false) {
        val fakeExercises = listOf(
            SessionExercise(
                exerciseId = "e1",
                name = "Press de banca",
                primaryMuscle = MuscleGroup.CHEST,
                targetSets = 5,
                targetReps = 5,
                targetRpe = 8,
                restSeconds = 180,
                completedSets = 1,
            ),
        )

        val fakeSessionData = WorkoutSessionData(
            exercises = fakeExercises,
            currentExerciseIndex = 0,
            registeredSets = emptyList(),
            autoregulationSuggestion = null,
            restTimerSeconds = null,
            volumeByMuscleGroup = mapOf(MuscleGroup.CHEST to 400.0),
            ghostSets = emptyList(),
            substitutions = null,
        )

        WorkoutSessionContent(
            sessionData = fakeSessionData,
            onRegisterSet = { _, _, _, _ -> },
            onFinalize = {},
            onAbandon = {},
        )
    }
}
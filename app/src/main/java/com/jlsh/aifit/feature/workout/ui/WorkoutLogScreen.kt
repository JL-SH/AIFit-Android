package com.jlsh.aifit.feature.workout.ui

import android.content.res.Configuration
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jlsh.aifit.R
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jlsh.aifit.core.ui.components.buttons.PrimaryButton
import com.jlsh.aifit.core.ui.components.feedback.AchievementUnlockedDialog
import com.jlsh.aifit.feature.gamification.ui.localizedAchievementDescription
import com.jlsh.aifit.feature.gamification.ui.localizedAchievementName
import com.jlsh.aifit.core.ui.components.feedback.ConfirmationDialog
import com.jlsh.aifit.core.ui.components.feedback.ErrorScreen
import com.jlsh.aifit.core.ui.components.feedback.LoadingScreen
import com.jlsh.aifit.core.ui.components.inputs.AiFitNumberField
import com.jlsh.aifit.core.ui.components.layout.AiFitTopBar
import com.jlsh.aifit.core.ui.components.layout.LocalBottomBarVisibility
import com.jlsh.aifit.core.ui.theme.AIFitTheme
import com.jlsh.aifit.core.ui.theme.AiFitSpacing
import com.jlsh.aifit.feature.workout.ui.state.LoggingUiState
import com.jlsh.aifit.feature.workout.ui.state.SetEntryState
import com.jlsh.aifit.feature.workout.ui.state.WorkoutUiEvent

@Composable
fun WorkoutLogScreen(
    planId: String,
    onNavigateBack: () -> Unit,
    onNavigateToDetail: (logId: String) -> Unit,
    viewModel: WorkoutViewModel = hiltViewModel(),
) {
    val loggingState by viewModel.loggingState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    var showDiscardDialog by remember { mutableStateOf(false) }
    var achievementDialog by remember { mutableStateOf<Pair<String, String>?>(null) }
    var pendingLogId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(planId) {
        viewModel.loadPlanDay(planId)
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is WorkoutUiEvent.NavigateBack -> onNavigateBack()
                is WorkoutUiEvent.NavigateToDetail -> {
                    pendingLogId = event.logId
                    if (achievementDialog == null) {
                        onNavigateToDetail(event.logId)
                    }
                }
                is WorkoutUiEvent.ShowAchievementDialog -> {
                    achievementDialog = context.localizedAchievementName(
                        event.code,
                        event.fallbackName,
                    ) to context.localizedAchievementDescription(
                        event.code,
                        event.fallbackDescription,
                    )
                }
                is WorkoutUiEvent.ShowSnackbar -> snackbarHostState.showSnackbar(event.message)
                is WorkoutUiEvent.DiscardConfirmation -> showDiscardDialog = true
                is WorkoutUiEvent.SessionSaved -> { /* handled via NavigateToDetail */ }
            }
        }
    }

    BackHandler {
        viewModel.onBackPressed()
    }

    val timerText = when (val state = loggingState) {
        is LoggingUiState.Ready -> {
            val mins = state.timerSeconds / 60
            val secs = state.timerSeconds % 60
            "%02d:%02d".format(mins, secs)
        }
        else -> "00:00"
    }

    CompositionLocalProvider(LocalBottomBarVisibility provides false) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
        ) {
            Scaffold(
                containerColor = Color.Transparent,
                topBar = {
                    AiFitTopBar(
                        title = timerText,
                        onBack = { viewModel.onBackPressed() },
                        background = MaterialTheme.colorScheme.background,
                    )
                },
                snackbarHost = { SnackbarHost(snackbarHostState) },
                bottomBar = {
                    val state = loggingState
                    if (state is LoggingUiState.Ready) {
                        Surface(
                            color = MaterialTheme.colorScheme.background,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            PrimaryButton(
                                text = stringResource(R.string.workout_finish_session_btn),
                                onClick = { viewModel.onFinishSession() },
                                isLoading = state.isSaving,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(
                                        horizontal = AiFitSpacing.md,
                                        vertical = AiFitSpacing.sm,
                                    ),
                            )
                        }
                    }
                },
            ) { paddingValues ->
                when (val state = loggingState) {
                    is LoggingUiState.Loading -> LoadingScreen()
                    is LoggingUiState.Error -> ErrorScreen(
                        message = state.message,
                        onRetry = { viewModel.loadPlanDay(planId) },
                    )
                    is LoggingUiState.Ready -> {
                        WorkoutLogContent(
                            state = state,
                            onRepsChanged = viewModel::onSetRepsChanged,
                            onWeightChanged = viewModel::onSetWeightChanged,
                            onCompletedToggled = viewModel::onSetCompletedToggled,
                            modifier = Modifier.padding(paddingValues),
                        )
                    }
                }
            }
        }
    }

    if (showDiscardDialog) {
        ConfirmationDialog(
            title = stringResource(R.string.workout_discard_title),
            message = stringResource(R.string.workout_discard_message),
            onConfirm = {
                showDiscardDialog = false
                viewModel.onConfirmDiscard()
            },
            onDismiss = { showDiscardDialog = false },
        )
    }

    achievementDialog?.let { (name, description) ->
        AchievementUnlockedDialog(
            achievementName = name,
            achievementDescription = description,
            onDismiss = {
                achievementDialog = null
                pendingLogId?.let { onNavigateToDetail(it) }
            },
        )
    }
}

@Composable
private fun WorkoutLogContent(
    state: LoggingUiState.Ready,
    onRepsChanged: (Int, String) -> Unit,
    onWeightChanged: (Int, String) -> Unit,
    onCompletedToggled: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val exercises = state.planDay.exercises
    var globalIndex = 0

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(
            start = AiFitSpacing.md,
            top = AiFitSpacing.sm,
            end = AiFitSpacing.md,
            bottom = 88.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        exercises.forEach { exercise ->
            val startIndex = globalIndex

            // Exercise header
            item(key = "header_${exercise.id}") {
                Column {
                    if (startIndex > 0) {
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant,
                            thickness = 0.5.dp,
                            modifier = Modifier.padding(vertical = AiFitSpacing.md),
                        )
                    }

                    Text(
                        text = exercise.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = exercise.primaryMuscle.name.replace("_", " "),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 1.sp,
                    )

                    Spacer(modifier = Modifier.height(AiFitSpacing.md))

                    // Column headers row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = AiFitSpacing.sm),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = stringResource(R.string.workout_set_header),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 1.5.sp,
                            modifier = Modifier.width(40.dp),
                            textAlign = TextAlign.Center,
                        )
                        Spacer(modifier = Modifier.width(AiFitSpacing.sm))
                        Text(
                            text = stringResource(R.string.workout_kg_header),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 1.5.sp,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center,
                        )
                        Spacer(modifier = Modifier.width(AiFitSpacing.sm))
                        Text(
                            text = stringResource(R.string.workout_reps_header),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 1.5.sp,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center,
                        )
                        Spacer(modifier = Modifier.width(AiFitSpacing.sm))
                        Box(modifier = Modifier.size(40.dp))
                    }
                }
            }

            // Set rows
            val setCount = exercise.sets
            val indices = (startIndex until startIndex + setCount)
            itemsIndexed(
                items = indices.toList(),
                key = { _, idx -> "set_${exercise.id}_$idx" },
            ) { _, setIndex ->
                if (setIndex < state.setStates.size) {
                    val entry = state.setStates[setIndex]
                    SetEntryRow(
                        setNumber = entry.exerciseSetNumber,
                        weightValue = entry.weightUsed,
                        repsValue = entry.repsCompleted,
                        completed = entry.completed,
                        onWeightChanged = { onWeightChanged(setIndex, it) },
                        onRepsChanged = { onRepsChanged(setIndex, it) },
                        onCompletedToggled = { onCompletedToggled(setIndex) },
                    )
                    Spacer(modifier = Modifier.height(AiFitSpacing.sm))
                }
            }
            globalIndex += setCount
        }
    }
}

@Composable
private fun SetEntryRow(
    setNumber: Int,
    weightValue: String,
    repsValue: String,
    completed: Boolean,
    onWeightChanged: (String) -> Unit,
    onRepsChanged: (String) -> Unit,
    onCompletedToggled: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Set number — lime when completed
        Text(
            text = "$setNumber",
            style = MaterialTheme.typography.titleMedium,
            color = if (completed) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            modifier = Modifier.width(40.dp),
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.width(AiFitSpacing.sm))

        // Weight field
        AiFitNumberField(
            value = weightValue,
            onValueChange = onWeightChanged,
            label = "",
            modifier = Modifier.weight(1f),
        )

        Spacer(modifier = Modifier.width(AiFitSpacing.sm))

        // Reps field
        AiFitNumberField(
            value = repsValue,
            onValueChange = onRepsChanged,
            label = "",
            modifier = Modifier.weight(1f),
        )

        Spacer(modifier = Modifier.width(AiFitSpacing.sm))

        // Check button — lime filled when completed, subtle when not
        IconButton(
            onClick = onCompletedToggled,
            modifier = Modifier.size(40.dp),
            colors = IconButtonDefaults.iconButtonColors(
                containerColor = if (completed) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surfaceContainerHigh
                },
                contentColor = if (completed) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
            ),
        ) {
            Icon(
                imageVector = Icons.Rounded.Check,
                contentDescription = if (completed) "Completed" else "Mark as done",
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

// ═══════════════════════════════════════
// PREVIEW
// ═══════════════════════════════════════

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "WorkoutLogScreen Dark",
)
@Composable
private fun WorkoutLogScreenPreview() {
    AIFitTheme(darkTheme = true) {
        val fakeEntries = listOf(
            SetEntryState("e1", "Bench Press", 1, "80", "10", true),
            SetEntryState("e1", "Bench Press", 2, "80", "8", true),
            SetEntryState("e1", "Bench Press", 3, "", "", false),
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
                        title = "03:24",
                        onBack = {},
                        background = MaterialTheme.colorScheme.background,
                    )
                },
                bottomBar = {
                    Surface(
                        color = MaterialTheme.colorScheme.background,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        PrimaryButton(
                            text = "FINALIZAR SESIÓN",
                            onClick = {},
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    horizontal = AiFitSpacing.md,
                                    vertical = AiFitSpacing.sm,
                                ),
                        )
                    }
                },
            ) { paddingValues ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = AiFitSpacing.md),
                ) {
                    Spacer(modifier = Modifier.height(AiFitSpacing.sm))

                    Text(
                        text = "Bench Press",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "CHEST",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 1.sp,
                    )

                    Spacer(modifier = Modifier.height(AiFitSpacing.md))

                    // Column headers
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = AiFitSpacing.sm),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("SET", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 1.5.sp, modifier = Modifier.width(40.dp), textAlign = TextAlign.Center)
                        Spacer(modifier = Modifier.width(AiFitSpacing.sm))
                        Text("KG", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 1.5.sp, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                        Spacer(modifier = Modifier.width(AiFitSpacing.sm))
                        Text("REPS", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 1.5.sp, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                        Spacer(modifier = Modifier.width(AiFitSpacing.sm))
                        Box(modifier = Modifier.size(40.dp))
                    }

                    fakeEntries.forEach { entry ->
                        SetEntryRow(
                            setNumber = entry.exerciseSetNumber,
                            weightValue = entry.weightUsed,
                            repsValue = entry.repsCompleted,
                            completed = entry.completed,
                            onWeightChanged = {},
                            onRepsChanged = {},
                            onCompletedToggled = {},
                        )
                        Spacer(modifier = Modifier.height(AiFitSpacing.sm))
                    }

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant,
                        thickness = 0.5.dp,
                        modifier = Modifier.padding(vertical = AiFitSpacing.md),
                    )

                    Text(
                        text = "Overhead Press",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "SHOULDERS",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 1.sp,
                    )
                }
            }
        }
    }
}

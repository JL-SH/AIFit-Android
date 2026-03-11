package com.jlsh.aifit.feature.workout.ui

import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jlsh.aifit.core.ui.components.buttons.PrimaryButton
import com.jlsh.aifit.core.ui.components.feedback.AchievementUnlockedDialog
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
                    achievementDialog = event.name to event.description
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
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                AiFitTopBar(
                    title = timerText,
                    onBack = { viewModel.onBackPressed() },
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
                            text = "FINISH SESSION",
                            onClick = { viewModel.onFinishSession() },
                            isLoading = state.isSaving,
                            modifier = Modifier.padding(AiFitSpacing.md),
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

    if (showDiscardDialog) {
        ConfirmationDialog(
            title = "Descartar sesión?",
            message = "Perderás los datos registrados",
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
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = AiFitSpacing.md,
            top = AiFitSpacing.sm,
            end = AiFitSpacing.md,
            bottom = 88.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(AiFitSpacing.xs),
    ) {
        exercises.forEach { exercise ->
            val startIndex = globalIndex
            item(key = "header_${exercise.id}") {
                Column {
                    if (startIndex > 0) {
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant,
                            thickness = 0.5.dp,
                            modifier = Modifier.padding(vertical = AiFitSpacing.sm),
                        )
                    }
                    Text(
                        text = exercise.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = exercise.primaryMuscle.name.replace("_", " "),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(AiFitSpacing.sm))
                    // Column headers
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text("SET", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.width(36.dp))
                        Text("KG", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
                        Text("REPS", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
                        Text("", modifier = Modifier.width(40.dp))
                    }
                }
            }

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
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = "$setNumber",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(36.dp),
        )
        AiFitNumberField(
            value = weightValue,
            onValueChange = onWeightChanged,
            label = "",
            modifier = Modifier.weight(1f),
        )
        Spacer(modifier = Modifier.width(AiFitSpacing.sm))
        AiFitNumberField(
            value = repsValue,
            onValueChange = onRepsChanged,
            label = "",
            modifier = Modifier.weight(1f),
        )
        Checkbox(
            checked = completed,
            onCheckedChange = { onCompletedToggled() },
            colors = CheckboxDefaults.colors(
                checkedColor = MaterialTheme.colorScheme.primaryContainer,
                uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant,
                checkmarkColor = MaterialTheme.colorScheme.onPrimaryContainer,
            ),
        )
    }
}

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "WorkoutLogScreen Dark",
)
@Composable
private fun WorkoutLogScreenPreview() {
    AIFitTheme(darkTheme = true) {
        val fakeEntries = listOf(
            SetEntryState("e1", "Bench Press", 1, "80", "10", false),
            SetEntryState("e1", "Bench Press", 2, "80", "8", true),
            SetEntryState("e1", "Bench Press", 3, "", "", false),
            SetEntryState("e2", "Overhead Press", 1, "40", "12", false),
            SetEntryState("e2", "Overhead Press", 2, "", "", false),
        )
        Column(modifier = Modifier.fillMaxSize()) {
            fakeEntries.forEachIndexed { idx, entry ->
                SetEntryRow(
                    setNumber = entry.exerciseSetNumber,
                    weightValue = entry.weightUsed,
                    repsValue = entry.repsCompleted,
                    completed = entry.completed,
                    onWeightChanged = {},
                    onRepsChanged = {},
                    onCompletedToggled = {},
                )
            }
        }
    }
}




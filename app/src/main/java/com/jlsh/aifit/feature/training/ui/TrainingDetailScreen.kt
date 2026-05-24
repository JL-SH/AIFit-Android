package com.jlsh.aifit.feature.training.ui

import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.*
import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import com.jlsh.aifit.R
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jlsh.aifit.core.ui.components.buttons.PrimaryButton
import com.jlsh.aifit.core.ui.components.display.AiFitCard
import com.jlsh.aifit.core.ui.components.feedback.EmptyStateKind
import com.jlsh.aifit.core.ui.components.feedback.TrainingDetailSkeleton
import com.jlsh.aifit.core.ui.components.feedback.EmptyStateView
import com.jlsh.aifit.core.ui.components.layout.AiFitTopBar
import com.jlsh.aifit.core.ui.components.layout.ScreenScaffold
import com.jlsh.aifit.core.ui.theme.AIFitTheme
import com.jlsh.aifit.core.ui.theme.AiFitSpacing
import com.jlsh.aifit.feature.education.ui.EducationViewModel
import com.jlsh.aifit.feature.education.ui.components.EducationConfirmSheet
import com.jlsh.aifit.feature.education.ui.components.ExerciseExplanationSheet
import com.jlsh.aifit.feature.progression.ui.ProgressionViewModel
import com.jlsh.aifit.feature.progression.ui.components.ProgressionIntroSheet
import com.jlsh.aifit.feature.progression.ui.components.ProgressionRecommendationSheet
import com.jlsh.aifit.feature.training.domain.model.MuscleGroup
import com.jlsh.aifit.feature.training.domain.model.TrainingDay
import com.jlsh.aifit.feature.training.domain.model.TrainingDayType
import com.jlsh.aifit.feature.training.domain.model.TrainingExercise
import com.jlsh.aifit.feature.training.ui.state.TrainingDayItem
import com.jlsh.aifit.feature.training.ui.state.TrainingDetailUiState
import com.jlsh.aifit.feature.training.ui.state.TrainingUiEvent
import com.jlsh.aifit.feature.user.ui.toStringRes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrainingDetailScreen(
    planId: String,
    onNavigateBack: () -> Unit,
    onNavigateToGenerate: (adaptive: Boolean, basePlanId: String?) -> Unit,
    onNavigateToWorkoutLog: (planId: String) -> Unit,
    onNavigateToSession: (planId: String, dayId: String) -> Unit = { _, _ -> },
    viewModel: TrainingViewModel = hiltViewModel(),
    educationViewModel: EducationViewModel = hiltViewModel(),
    progressionViewModel: ProgressionViewModel = hiltViewModel(),
) {
    val detailState by viewModel.detailUiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val explanationState by educationViewModel.explanationState.collectAsStateWithLifecycle()
    val recommendationState by progressionViewModel.recommendationState.collectAsStateWithLifecycle()
    val sessionCount by progressionViewModel.sessionCount.collectAsStateWithLifecycle()

    var showExplanationForExerciseId by remember { mutableStateOf<String?>(null) }
    var showExplanationConfirmForExerciseId by remember { mutableStateOf<String?>(null) }
    var showProgressionIntroForExerciseId by remember { mutableStateOf<String?>(null) }
    var showProgressionForExerciseId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(planId) {
        viewModel.loadPlanDetail(planId)
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is TrainingUiEvent.NavigateBack -> onNavigateBack()
                is TrainingUiEvent.NavigateToGenerate -> onNavigateToGenerate(event.adaptive, event.basePlanId)
                is TrainingUiEvent.NavigateToWorkoutLog -> onNavigateToWorkoutLog(event.planId)
                is TrainingUiEvent.ShowSnackbar -> snackbarHostState.showSnackbar(event.message)
                else -> {}
            }
        }
    }

    // ── Sheets ──
    if (showExplanationConfirmForExerciseId != null) {
        EducationConfirmSheet(
            title = stringResource(R.string.training_detail_exercise_explanation_title),
            description = stringResource(R.string.training_detail_exercise_explanation_desc_plan),
            confirmText = stringResource(R.string.training_detail_generate_explanation),
            onDismiss = { showExplanationConfirmForExerciseId = null },
            onConfirm = {
                val exerciseId = showExplanationConfirmForExerciseId
                showExplanationConfirmForExerciseId = null
                if (exerciseId != null) {
                    showExplanationForExerciseId = exerciseId
                    educationViewModel.loadExerciseExplanation(exerciseId)
                }
            },
        )
    }

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

    if (showProgressionIntroForExerciseId != null) {
        ProgressionIntroSheet(
            sessionCount = sessionCount,
            onDismiss = { showProgressionIntroForExerciseId = null },
            onConfirm = {
                val exerciseId = showProgressionIntroForExerciseId
                showProgressionIntroForExerciseId = null
                if (exerciseId != null) {
                    showProgressionForExerciseId = exerciseId
                    progressionViewModel.loadExerciseRecommendation(exerciseId)
                }
            },
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

    val topBarTitle = when (val state = detailState) {
        is TrainingDetailUiState.Ready -> state.planName
        else -> stringResource(R.string.training_detail_title)
    }

    ScreenScaffold<TrainingDetailUiState.Ready>(
        uiState = detailState,
        loadingContent = { TrainingDetailSkeleton() },
        snackbarHostState = snackbarHostState,
        topBar = {
            AiFitTopBar(
                title = topBarTitle,
                onBack = onNavigateBack,
                actions = {
                    IconButton(onClick = {
                        viewModel.onNavigateToGenerate(adaptive = true, basePlanId = planId)
                    }) {
                        Icon(
                            imageVector = PhosphorIcons.Regular.Sparkle,
                            contentDescription = stringResource(R.string.training_detail_adaptive_plan_cd),
                            tint = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(24.dp),
                        )
                    }
                },
            )
        },
        onRetry = { viewModel.loadPlanDetail(planId) },
    ) { paddingValues, readyState ->
        if (readyState.days.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center,
            ) {
                EmptyStateView(
                    icon = PhosphorIcons.Regular.Barbell,
                    kind = EmptyStateKind.TrainingDays,
                    title = stringResource(R.string.training_detail_no_days_title),
                    subtitle = stringResource(R.string.training_detail_no_days_subtitle),
                )
            }
        } else {
            TrainingDetailContent(
                planId = planId,
                days = readyState.days,
                sessionCount = sessionCount,
                onExerciseInfoClick = { exerciseId ->
                    showExplanationConfirmForExerciseId = exerciseId
                },
                onExerciseProgressionClick = { exerciseId ->
                    showProgressionIntroForExerciseId = exerciseId
                },
                onNavigateToSession = onNavigateToSession,
                modifier = Modifier.padding(paddingValues),
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TrainingDetailContent(
    planId: String,
    days: List<TrainingDayItem>,
    sessionCount: Int? = null,
    onExerciseInfoClick: (exerciseId: String) -> Unit,
    onExerciseProgressionClick: (exerciseId: String) -> Unit,
    onNavigateToSession: (planId: String, dayId: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(
            start = AiFitSpacing.md,
            top = AiFitSpacing.md,
            end = AiFitSpacing.md,
            bottom = AiFitSpacing.xxl,
        ),
        verticalArrangement = Arrangement.spacedBy(AiFitSpacing.sm),
    ) {
        items(days, key = { item ->
            when (item) {
                is TrainingDayItem.Training -> item.day.id
                is TrainingDayItem.Rest -> item.day.id
            }
        }) { item ->
            when (item) {
                is TrainingDayItem.Rest -> RestDayCard(day = item.day)
                is TrainingDayItem.Training -> TrainingDayCard(
                    day = item.day,
                    sessionCount = sessionCount,
                    onExerciseInfoClick = onExerciseInfoClick,
                    onExerciseProgressionClick = onExerciseProgressionClick,
                    onStartSession = { onNavigateToSession(planId, item.day.id) },
                )
            }
        }
    }
}

@Composable
private fun RestDayCard(day: TrainingDay) {
    AiFitCard {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AiFitSpacing.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AiFitSpacing.sm),
        ) {
            Icon(
                imageVector = PhosphorIcons.Regular.PersonSimpleTaiChi,
                contentDescription = stringResource(R.string.training_detail_rest_day),
                tint = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(32.dp),
            )
            Column(verticalArrangement = Arrangement.spacedBy(AiFitSpacing.xs)) {
                Text(
                    text = stringResource(R.string.training_detail_day_label, day.dayNumber, day.name),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = stringResource(R.string.training_detail_rest_day),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TrainingDayCard(
    day: TrainingDay,
    sessionCount: Int? = null,
    onExerciseInfoClick: (exerciseId: String) -> Unit,
    onExerciseProgressionClick: (exerciseId: String) -> Unit,
    onStartSession: () -> Unit,
) {
    val muscleGroups = day.exercises
        .map { it.primaryMuscle }
        .distinct()

    AiFitCard {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AiFitSpacing.md),
            verticalArrangement = Arrangement.spacedBy(AiFitSpacing.sm),
        ) {
            // Day name
            Text(
                text = stringResource(R.string.training_detail_day_label, day.dayNumber, day.name),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )

            // Muscle group badges
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(AiFitSpacing.xs),
                verticalArrangement = Arrangement.spacedBy(AiFitSpacing.xs),
            ) {
                muscleGroups.forEach { muscle ->
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        shape = RoundedCornerShape(6.dp),
                    ) {
                        Text(
                            text = stringResource(muscle.toStringRes()),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = AiFitSpacing.sm, vertical = AiFitSpacing.xs),
                        )
                    }
                }
            }

            // Exercise list
            day.exercises.forEachIndexed { index, exercise ->
                ExerciseRow(
                    exercise = exercise,
                    sessionCount = sessionCount,
                    onInfoClick = { onExerciseInfoClick(exercise.id) },
                    onProgressionClick = { onExerciseProgressionClick(exercise.id) },
                )
                if (index < day.exercises.lastIndex) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant,
                        thickness = 0.5.dp,
                    )
                }
            }

            Spacer(modifier = Modifier.height(AiFitSpacing.xs))

            // Start Session button
            PrimaryButton(
                text = stringResource(R.string.training_detail_start_session),
                onClick = onStartSession,
            )
        }
    }
}

@Composable
private fun ExerciseRow(
    exercise: TrainingExercise,
    sessionCount: Int? = null,
    onInfoClick: () -> Unit = {},
    onProgressionClick: () -> Unit = {},
) {
    val hasEnoughSessions = (sessionCount ?: 0) >= 3
    val progressionAlpha = if (hasEnoughSessions) 1f else 0.4f
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = AiFitSpacing.xs),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(AiFitSpacing.xs),
        ) {
            Text(
                text = exercise.name,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(AiFitSpacing.sm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "${exercise.sets}×${exercise.repsMin}–${exercise.repsMax}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primaryContainer,
                )
                if (exercise.targetRpe != null) {
                    Text(
                        text = stringResource(R.string.common_rpe_format, exercise.targetRpe.toString()),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Text(
                    text = stringResource(R.string.training_detail_rest_seconds, exercise.restSeconds),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Row {
            IconButton(
                onClick = onInfoClick,
                modifier = Modifier.size(32.dp),
            ) {
                Icon(
                    imageVector = PhosphorIcons.Regular.Info,
                    contentDescription = stringResource(R.string.training_detail_exercise_explanation_title),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp),
                )
            }
            IconButton(
                onClick = onProgressionClick,
                modifier = Modifier
                    .size(32.dp)
                    .alpha(progressionAlpha),
            ) {
                Icon(
                    imageVector = PhosphorIcons.Regular.TrendUp,
                    contentDescription = stringResource(R.string.training_detail_progression_cd),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "TrainingDetailScreen Dark",
)
@Composable
private fun TrainingDetailScreenPreview() {
    AIFitTheme(darkTheme = true) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
        ) {
            val fakeDays = listOf(
                TrainingDayItem.Training(
                    day = TrainingDay(
                        id = "d1",
                        dayNumber = 1,
                        name = "Push Day",
                        estimatedDurationMinutes = 60,
                        exercises = listOf(
                            TrainingExercise(
                                id = "e1", name = "Bench Press", description = null,
                                primaryMuscle = MuscleGroup.CHEST,
                                secondaryMuscle = MuscleGroup.TRICEPS,
                                sets = 5, repsMin = 5, repsMax = 5, restSeconds = 180,
                                notes = null, order = 1, targetRpe = 8,
                            ),
                            TrainingExercise(
                                id = "e2", name = "Overhead Press", description = null,
                                primaryMuscle = MuscleGroup.SHOULDERS,
                                secondaryMuscle = MuscleGroup.TRICEPS,
                                sets = 3, repsMin = 8, repsMax = 12, restSeconds = 120,
                                notes = null, order = 2, targetRpe = 7,
                            ),
                            TrainingExercise(
                                id = "e3", name = "Incline Dumbbell Press", description = null,
                                primaryMuscle = MuscleGroup.CHEST,
                                secondaryMuscle = MuscleGroup.SHOULDERS,
                                sets = 3, repsMin = 10, repsMax = 15, restSeconds = 90,
                                notes = null, order = 3,
                            ),
                        ),
                    ),
                ),
                TrainingDayItem.Rest(
                    day = TrainingDay(
                        id = "d2",
                        dayNumber = 2,
                        name = "Descanso",
                        estimatedDurationMinutes = 0,
                        exercises = emptyList(),
                        dayType = TrainingDayType.REST,
                    ),
                ),
                TrainingDayItem.Training(
                    day = TrainingDay(
                        id = "d3",
                        dayNumber = 3,
                        name = "Pull Day",
                        estimatedDurationMinutes = 55,
                        exercises = listOf(
                            TrainingExercise(
                                id = "e4", name = "Barbell Row", description = null,
                                primaryMuscle = MuscleGroup.BACK,
                                secondaryMuscle = MuscleGroup.BICEPS,
                                sets = 5, repsMin = 5, repsMax = 5, restSeconds = 180,
                                notes = null, order = 1, targetRpe = 8,
                            ),
                            TrainingExercise(
                                id = "e5", name = "Pull-ups", description = null,
                                primaryMuscle = MuscleGroup.BACK,
                                secondaryMuscle = MuscleGroup.BICEPS,
                                sets = 3, repsMin = 6, repsMax = 10, restSeconds = 120,
                                notes = null, order = 2,
                            ),
                        ),
                    ),
                ),
            )

            TrainingDetailContent(
                planId = "plan-1",
                days = fakeDays,
                onExerciseInfoClick = {},
                onExerciseProgressionClick = {},
                onNavigateToSession = { _, _ -> },
            )
        }
    }
}

// U-12/U-13 compliance fix — light mode preview
@Preview(
    showBackground = true,
    name = "TrainingDetailScreen Light",
)
@Composable
private fun TrainingDetailScreenLightPreview() {
    AIFitTheme(darkTheme = false) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
        ) {
            val fakeDays = listOf(
                TrainingDayItem.Training(
                    day = TrainingDay(
                        id = "d1",
                        dayNumber = 1,
                        name = "Push Day",
                        estimatedDurationMinutes = 60,
                        exercises = listOf(
                            TrainingExercise(
                                id = "e1", name = "Bench Press", description = null,
                                primaryMuscle = MuscleGroup.CHEST,
                                secondaryMuscle = MuscleGroup.TRICEPS,
                                sets = 5, repsMin = 5, repsMax = 5, restSeconds = 180,
                                notes = null, order = 1, targetRpe = 8,
                            ),
                        ),
                    ),
                ),
            )

            TrainingDetailContent(
                planId = "plan-1",
                days = fakeDays,
                onExerciseInfoClick = {},
                onExerciseProgressionClick = {},
                onNavigateToSession = { _, _ -> },
            )
        }
    }
}

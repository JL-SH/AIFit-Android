package com.jlsh.aifit.feature.training.ui

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
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
import com.jlsh.aifit.core.ui.components.buttons.PrimaryButton
import com.jlsh.aifit.core.ui.components.display.PlanStatusBadge
import com.jlsh.aifit.core.ui.components.layout.AiFitTopBar
import com.jlsh.aifit.core.ui.components.layout.ExpandableSection
import com.jlsh.aifit.core.ui.components.layout.ScreenScaffold
import com.jlsh.aifit.core.ui.theme.AIFitTheme
import com.jlsh.aifit.core.ui.theme.AiFitSpacing
import com.jlsh.aifit.feature.training.domain.model.MuscleGroup
import com.jlsh.aifit.feature.training.domain.model.PlanStatus
import com.jlsh.aifit.feature.training.domain.model.TrainingDay
import com.jlsh.aifit.feature.training.domain.model.TrainingExercise
import com.jlsh.aifit.feature.training.domain.model.TrainingPlan
import com.jlsh.aifit.feature.training.ui.state.TrainingDetailUiState
import com.jlsh.aifit.feature.training.ui.state.TrainingUiEvent
import com.jlsh.aifit.feature.user.domain.model.FitnessLevel
import com.jlsh.aifit.feature.user.domain.model.GoalType
import com.jlsh.aifit.feature.user.domain.model.PreferredLocation
import java.time.LocalDateTime

@Composable
fun TrainingDetailScreen(
    planId: String,
    onNavigateBack: () -> Unit,
    onNavigateToGenerate: (adaptive: Boolean, basePlanId: String?) -> Unit,
    onNavigateToWorkoutLog: (planId: String) -> Unit,
    viewModel: TrainingViewModel = hiltViewModel(),
) {
    val detailState by viewModel.detailUiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

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

    val topBarTitle = when (val state = detailState) {
        is TrainingDetailUiState.Success -> state.plan.name
        else -> "Training Plan"
    }

    ScreenScaffold<TrainingDetailUiState.Success>(
        uiState = detailState,
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
                            imageVector = Icons.Rounded.AutoAwesome,
                            contentDescription = "Adaptive plan",
                            tint = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(24.dp),
                        )
                    }
                },
            )
        },
        onRetry = { viewModel.loadPlanDetail(planId) },
        bottomBar = {
            val state = detailState
            if (state is TrainingDetailUiState.Success && state.plan.status == PlanStatus.ACTIVE) {
                Surface(
                    color = MaterialTheme.colorScheme.background,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    PrimaryButton(
                        text = "START SESSION",
                        onClick = { viewModel.onStartSession(planId) },
                        modifier = Modifier.padding(AiFitSpacing.md),
                    )
                }
            }
        },
    ) { paddingValues, successState ->
        TrainingDetailContent(
            plan = successState.plan,
            modifier = Modifier.padding(paddingValues),
        )
    }
}

@Composable
private fun TrainingDetailContent(
    plan: TrainingPlan,
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
        // Metadata
        item(key = "metadata") {
            Column(
                verticalArrangement = Arrangement.spacedBy(AiFitSpacing.xs),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = plan.name,
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f),
                    )
                    PlanStatusBadge(status = plan.status.name)
                }

                Text(
                    text = "${plan.frequencyDaysPerWeek} days/week • ${plan.durationWeeks} weeks • ${plan.fitnessLevel.name} • ${plan.goalType.name.replace("_", " ")}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                if (!plan.description.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(AiFitSpacing.xs))
                    Text(
                        text = plan.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                Spacer(modifier = Modifier.height(AiFitSpacing.sm))
            }
        }

        // Days with exercises
        items(plan.days, key = { it.id }) { day ->
            ExpandableSection(
                title = "Day ${day.dayNumber} — ${day.name}",
                initiallyExpanded = false,
            ) {
                Column(
                    modifier = Modifier.padding(
                        start = AiFitSpacing.md,
                        end = AiFitSpacing.md,
                        bottom = AiFitSpacing.sm,
                    ),
                    verticalArrangement = Arrangement.spacedBy(AiFitSpacing.sm),
                ) {
                    Text(
                        text = "~${day.estimatedDurationMinutes} min",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    day.exercises.forEach { exercise ->
                        ExerciseRow(exercise = exercise)
                    }
                }
            }
        }
    }
}

@Composable
private fun ExerciseRow(
    exercise: TrainingExercise,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
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
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(6.dp),
                ) {
                    Text(
                        text = exercise.primaryMuscle.name.replace("_", " "),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    )
                }
            }
        }

        Row {
            IconButton(
                onClick = { /* Stub — Sprint 10: ExerciseExplanationSheet */ },
                modifier = Modifier.size(32.dp),
            ) {
                Icon(
                    imageVector = Icons.Rounded.Info,
                    contentDescription = "Info",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp),
                )
            }
            IconButton(
                onClick = { /* Stub — Sprint 10: ProgressionRecommendationSheet */ },
                modifier = Modifier.size(32.dp),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.TrendingUp,
                    contentDescription = "Progression",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp),
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
        val fakePlan = TrainingPlan(
            id = "1",
            name = "Plan de Fuerza 5x5",
            description = "Un plan de fuerza clásico para ganar músculo",
            frequencyDaysPerWeek = 3,
            durationWeeks = 8,
            goalType = GoalType.GAIN_MUSCLE,
            fitnessLevel = FitnessLevel.INTERMEDIATE,
            location = PreferredLocation.GYM,
            status = PlanStatus.ACTIVE,
            totalDays = 24,
            createdAt = LocalDateTime.now(),
            days = listOf(
                TrainingDay(
                    id = "d1",
                    dayNumber = 1,
                    name = "Push Day",
                    estimatedDurationMinutes = 60,
                    exercises = listOf(
                        TrainingExercise(
                            id = "e1", name = "Bench Press", description = null,
                            primaryMuscle = MuscleGroup.CHEST, secondaryMuscle = MuscleGroup.TRICEPS,
                            sets = 5, repsMin = 5, repsMax = 5, restSeconds = 180,
                            notes = null, order = 1,
                        ),
                        TrainingExercise(
                            id = "e2", name = "Overhead Press", description = null,
                            primaryMuscle = MuscleGroup.SHOULDERS, secondaryMuscle = MuscleGroup.TRICEPS,
                            sets = 3, repsMin = 8, repsMax = 12, restSeconds = 120,
                            notes = null, order = 2,
                        ),
                    ),
                ),
                TrainingDay(
                    id = "d2",
                    dayNumber = 2,
                    name = "Pull Day",
                    estimatedDurationMinutes = 55,
                    exercises = listOf(
                        TrainingExercise(
                            id = "e3", name = "Barbell Row", description = null,
                            primaryMuscle = MuscleGroup.BACK, secondaryMuscle = MuscleGroup.BICEPS,
                            sets = 5, repsMin = 5, repsMax = 5, restSeconds = 180,
                            notes = null, order = 1,
                        ),
                    ),
                ),
            ),
        )

        TrainingDetailContent(
            plan = fakePlan,
        )
    }
}










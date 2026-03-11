package com.jlsh.aifit.feature.training.ui

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
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Info
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
        else -> "Plan de entrenamiento"
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
                            contentDescription = "Plan adaptativo",
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
                        text = "INICIAR SESIÓN",
                        onClick = { viewModel.onStartSession(planId) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = AiFitSpacing.md, vertical = AiFitSpacing.sm),
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
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(
            bottom = 88.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(0.dp),
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
                // Badge
                PlanStatusBadge(status = plan.status.name)

                Spacer(Modifier.height(AiFitSpacing.xs))

                // Título dominante
                Text(
                    text = plan.name,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                // Metadata subordinada
                Text(
                    text = buildString {
                        append("${plan.frequencyDaysPerWeek} días/semana")
                        append("  ·  ${plan.durationWeeks} semanas")
                        append("  ·  ${plan.fitnessLevel.name.lowercase().replaceFirstChar { it.uppercase() }}")
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                if (!plan.description.isNullOrBlank()) {
                    Spacer(Modifier.height(AiFitSpacing.xs))
                    Text(
                        text = plan.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                Spacer(Modifier.height(AiFitSpacing.sm))

                // Stats row — 3 números destacados
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(0.dp),
                ) {
                    StatCell(
                        value = "${plan.frequencyDaysPerWeek}",
                        label = "DÍAS/SEM",
                        modifier = Modifier.weight(1f),
                    )
                    StatDivider()
                    StatCell(
                        value = "${plan.durationWeeks}",
                        label = "SEMANAS",
                        modifier = Modifier.weight(1f),
                    )
                    StatDivider()
                    StatCell(
                        value = "${plan.totalDays}",
                        label = "SESIONES",
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }

        // Divisor entre hero y lista de días
        item(key = "divider_top") {
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant,
                thickness = 0.5.dp,
            )
            Spacer(Modifier.height(AiFitSpacing.md))
        }

        // Section header
        item(key = "days_header") {
            Text(
                text = "PROGRAMA",
                style = MaterialTheme.typography.labelSmall,
                letterSpacing = 1.5.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AiFitSpacing.md)
                    .padding(bottom = AiFitSpacing.sm),
            )
        }

        // ── Días expandibles ─────────────────────────────────────────
        items(plan.days, key = { it.id }) { day ->
            Column(
                modifier = Modifier.padding(horizontal = AiFitSpacing.md),
            ) {
                ExpandableSection(
                    title = "Día ${day.dayNumber} — ${day.name}",
                    initiallyExpanded = false,
                ) {
                    Column(
                        modifier = Modifier.padding(
                            start = AiFitSpacing.sm,
                            end = AiFitSpacing.sm,
                            bottom = AiFitSpacing.sm,
                        ),
                        verticalArrangement = Arrangement.spacedBy(0.dp),
                    ) {
                        Text(
                            text = "~${day.estimatedDurationMinutes} min",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = AiFitSpacing.sm),
                        )
                        day.exercises.forEachIndexed { index, exercise ->
                            ExerciseRow(exercise = exercise)
                            if (index < day.exercises.lastIndex) {
                                HorizontalDivider(
                                    color = MaterialTheme.colorScheme.outlineVariant,
                                    thickness = 0.5.dp,
                                    modifier = Modifier.padding(vertical = AiFitSpacing.xs),
                                )
                            }
                        }
                    }
                }
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant,
                    thickness = 0.5.dp,
                )
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
            color = MaterialTheme.colorScheme.primaryContainer, // lime — números destacados
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
        modifier = Modifier
            .height(32.dp)
            .padding(horizontal = 0.dp),
        contentAlignment = Alignment.Center,
    ) {
        HorizontalDivider(
            modifier = Modifier
                .size(width = 0.5.dp, height = 32.dp),
            color = MaterialTheme.colorScheme.outlineVariant,
            thickness = 0.5.dp,
        )
    }
}

@Composable
private fun ExerciseRow(
    exercise: TrainingExercise,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = AiFitSpacing.xs),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
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
                // Series × Reps — en lime para destacar
                Text(
                    text = "${exercise.sets}×${exercise.repsMin}–${exercise.repsMax}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primaryContainer,
                )
                // Músculo — badge discreto
                Surface(
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    shape = RoundedCornerShape(6.dp),
                ) {
                    Text(
                        text = exercise.primaryMuscle.name
                            .replace("_", " ")
                            .lowercase()
                            .replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                    )
                }
            }
        }

        Row {
            IconButton(
                onClick = { /* Sprint 10: ExerciseExplanationSheet */ },
                modifier = Modifier.size(32.dp),
            ) {
                Icon(
                    imageVector = Icons.Rounded.Info,
                    contentDescription = "Info",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp),
                )
            }
            IconButton(
                onClick = { /* Sprint 10: ProgressionRecommendationSheet */ },
                modifier = Modifier.size(32.dp),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.TrendingUp,
                    contentDescription = "Progresión",
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
            val fakePlan = TrainingPlan(
                id = "1",
                name = "Plan de Fuerza 5x5",
                description = "Un plan de fuerza clásico para desarrollar fuerza máxima y masa muscular.",
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
                                primaryMuscle = MuscleGroup.CHEST,
                                secondaryMuscle = MuscleGroup.TRICEPS,
                                sets = 5, repsMin = 5, repsMax = 5, restSeconds = 180,
                                notes = null, order = 1,
                            ),
                            TrainingExercise(
                                id = "e2", name = "Overhead Press", description = null,
                                primaryMuscle = MuscleGroup.SHOULDERS,
                                secondaryMuscle = MuscleGroup.TRICEPS,
                                sets = 3, repsMin = 8, repsMax = 12, restSeconds = 120,
                                notes = null, order = 2,
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
                    TrainingDay(
                        id = "d2",
                        dayNumber = 2,
                        name = "Pull Day",
                        estimatedDurationMinutes = 55,
                        exercises = listOf(
                            TrainingExercise(
                                id = "e4", name = "Barbell Row", description = null,
                                primaryMuscle = MuscleGroup.BACK,
                                secondaryMuscle = MuscleGroup.BICEPS,
                                sets = 5, repsMin = 5, repsMax = 5, restSeconds = 180,
                                notes = null, order = 1,
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
                    TrainingDay(
                        id = "d3",
                        dayNumber = 3,
                        name = "Leg Day",
                        estimatedDurationMinutes = 65,
                        exercises = listOf(
                            TrainingExercise(
                                id = "e6", name = "Back Squat", description = null,
                                primaryMuscle = MuscleGroup.QUADS,
                                secondaryMuscle = MuscleGroup.GLUTES,
                                sets = 5, repsMin = 5, repsMax = 5, restSeconds = 240,
                                notes = null, order = 1,
                            ),
                        ),
                    ),
                ),
            )

            TrainingDetailContent(plan = fakePlan)
        }
    }
}
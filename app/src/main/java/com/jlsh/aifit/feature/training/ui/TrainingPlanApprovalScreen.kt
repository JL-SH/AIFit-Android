package com.jlsh.aifit.feature.training.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jlsh.aifit.core.ui.components.buttons.PrimaryButton
import com.jlsh.aifit.core.ui.components.buttons.SecondaryButton
import com.jlsh.aifit.core.ui.components.display.PlanStatusBadge
import com.jlsh.aifit.core.ui.components.layout.AiFitTopBar
import com.jlsh.aifit.core.ui.components.layout.ExpandableSection
import com.jlsh.aifit.core.ui.components.layout.ScreenScaffold
import com.jlsh.aifit.core.ui.theme.AiFitSpacing
import com.jlsh.aifit.feature.training.domain.model.TrainingExercise
import com.jlsh.aifit.feature.training.ui.state.TrainingDayItem
import com.jlsh.aifit.feature.training.ui.state.TrainingDetailUiState
import com.jlsh.aifit.feature.user.ui.displayName

@Composable
fun TrainingPlanApprovalScreen(
    planId: String,
    onAccept: () -> Unit,
    onReject: () -> Unit,
    viewModel: TrainingViewModel = hiltViewModel(),
) {
    val detailUiState by viewModel.detailUiState.collectAsStateWithLifecycle()

    LaunchedEffect(planId) { viewModel.loadPlanDetail(planId) }

    ScreenScaffold<TrainingDetailUiState.Ready>(
        uiState = detailUiState,
        topBar = { AiFitTopBar(title = "Tu Nuevo Plan") },
        onRetry = { viewModel.loadPlanDetail(planId) },
    ) { paddingValues, state ->
        Box(modifier = Modifier.fillMaxSize()) {
            val sortedDays = state.days.sortedBy { item ->
                when (item) {
                    is TrainingDayItem.Training -> item.day.dayNumber
                    is TrainingDayItem.Rest -> item.day.dayNumber
                }
            }
            val trainingDays = state.days.filterIsInstance<TrainingDayItem.Training>()
            val exerciseCount = trainingDays.sumOf { it.day.exercises.size }

            LazyColumn(
                contentPadding = PaddingValues(
                    start = AiFitSpacing.md,
                    end = AiFitSpacing.md,
                    top = paddingValues.calculateTopPadding() + AiFitSpacing.md,
                    bottom = paddingValues.calculateBottomPadding() + 88.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(AiFitSpacing.md),
            ) {
                // Plan name header + status badge
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(AiFitSpacing.sm),
                    ) {
                        Text(
                            text = state.planName,
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f),
                        )
                        PlanStatusBadge(status = "ACTIVE")
                    }
                }

                // Metadata chips row
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(AiFitSpacing.sm),
                    ) {
                        ApprovalSummaryChip(
                            text = "${trainingDays.size} días de entrenamiento",
                            modifier = Modifier.weight(1f),
                        )
                        ApprovalSummaryChip(
                            text = "$exerciseCount ejercicios",
                            modifier = Modifier.weight(1f),
                        )
                    }
                }

                // Days label
                item {
                    Text(
                        text = "PLAN DE ENTRENAMIENTO",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 1.5.sp,
                        modifier = Modifier.padding(top = AiFitSpacing.sm),
                    )
                }

                // Days
                items(sortedDays) { dayItem ->
                    when (dayItem) {
                        is TrainingDayItem.Training -> {
                            val day = dayItem.day
                            ExpandableSection(
                                title = "Día ${day.dayNumber} · ${day.name}",
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = AiFitSpacing.sm),
                                    verticalArrangement = Arrangement.spacedBy(AiFitSpacing.xs),
                                ) {
                                    day.exercises.sortedBy { it.order }.forEach { exercise ->
                                        ApprovalExerciseRow(exercise = exercise)
                                    }
                                }
                            }
                        }
                        is TrainingDayItem.Rest -> {
                            val day = dayItem.day
                            ElevatedCard(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.elevatedCardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                                ),
                                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 0.dp),
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(AiFitSpacing.md),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(
                                        text = "Día ${day.dayNumber} · ${day.name}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                    Text(
                                        text = "Descanso",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Sticky bottom action bar
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp,
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = AiFitSpacing.md, vertical = AiFitSpacing.sm)
                        .padding(bottom = paddingValues.calculateBottomPadding()),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    SecondaryButton(
                        text = "Cambiar Plan",
                        onClick = {
                            viewModel.onRejectPlan(planId)
                            onReject()
                        },
                        modifier = Modifier.weight(1f),
                    )
                    PrimaryButton(
                        text = "Aceptar Plan",
                        onClick = {
                            viewModel.onApprovePlan()
                            onAccept()
                        },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun ApprovalSummaryChip(
    text: String,
    modifier: Modifier = Modifier,
) {
    ElevatedCard(
        modifier = modifier,
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 0.dp),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(AiFitSpacing.sm),
        )
    }
}

@Composable
private fun ApprovalExerciseRow(
    exercise: TrainingExercise,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = AiFitSpacing.xs),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            text = exercise.name,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = exercise.primaryMuscle.displayName(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primaryContainer,
        )
        Text(
            text = "${exercise.sets} series · ${exercise.repsMin}–${exercise.repsMax} reps · ${exercise.restSeconds}s descanso",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}



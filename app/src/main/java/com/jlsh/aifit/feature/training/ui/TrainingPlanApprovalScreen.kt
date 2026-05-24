package com.jlsh.aifit.feature.training.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jlsh.aifit.R
import com.jlsh.aifit.core.ui.components.buttons.PrimaryButton
import com.jlsh.aifit.core.ui.components.buttons.SecondaryButton
import com.jlsh.aifit.core.ui.components.display.PlanStatusBadge
import com.jlsh.aifit.core.ui.components.inputs.AiFitTextField
import com.jlsh.aifit.core.ui.components.layout.AiFitTopBar
import com.jlsh.aifit.core.ui.components.layout.ExpandableSection
import com.jlsh.aifit.core.ui.components.feedback.SuccessCheckOverlay
import com.jlsh.aifit.core.ui.components.layout.ScreenScaffold
import com.jlsh.aifit.core.ui.theme.AiFitSpacing
import com.jlsh.aifit.feature.training.domain.model.TrainingExercise
import com.jlsh.aifit.feature.training.ui.state.TrainingDayItem
import com.jlsh.aifit.feature.training.ui.state.TrainingDetailUiState
import com.jlsh.aifit.feature.training.ui.state.TrainingUiEvent
import com.jlsh.aifit.feature.user.ui.displayName
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrainingPlanApprovalScreen(
    planId: String,
    onAccept: () -> Unit,
    onNavigateToApproval: (String) -> Unit,
    viewModel: TrainingViewModel = hiltViewModel(),
) {
    val detailUiState by viewModel.detailUiState.collectAsStateWithLifecycle()
    var showSuccessOverlay by remember { mutableStateOf(false) }
    var showFeedbackSheet by remember { mutableStateOf(false) }
    var feedbackText by remember { mutableStateOf("") }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    LaunchedEffect(planId) { viewModel.loadPlanDetail(planId) }

    // Collect navigation events for regeneration and approval
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is TrainingUiEvent.NavigateToApproval -> onNavigateToApproval(event.planId)
                is TrainingUiEvent.PlanApproved -> showSuccessOverlay = true
                is TrainingUiEvent.NavigateBack -> onAccept()
                else -> Unit
            }
        }
    }

    // Handle Regenerating state — fullscreen loading (mirrors onboarding pattern)
    if (detailUiState is TrainingDetailUiState.Regenerating) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(AiFitSpacing.md),
            ) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primaryContainer,
                )
                Text(
                    text = stringResource(R.string.onboarding_regenerating_training),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        return
    }

    Box(modifier = Modifier.fillMaxSize()) {
        SuccessCheckOverlay(
            visible = showSuccessOverlay,
            onAnimationComplete = {
                showSuccessOverlay = false
                onAccept()
            },
        )

    ScreenScaffold<TrainingDetailUiState.Ready>(
        uiState = detailUiState,
        topBar = { AiFitTopBar(title = stringResource(R.string.training_approval_title)) },
        onRetry = { viewModel.loadPlanDetail(planId) },
    ) { paddingValues, state ->
        Box(modifier = Modifier.fillMaxSize()) {
            val sortedDays = state.days.sortedBy { item ->
                when (item) {
                    is TrainingDayItem.Training -> item.day.dayOfWeek?.ordinal ?: item.day.dayNumber
                    is TrainingDayItem.Rest -> item.day.dayOfWeek?.ordinal ?: item.day.dayNumber
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
                        PlanStatusBadge(status = state.planStatus)
                    }
                }

                // Metadata chips row
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(AiFitSpacing.sm),
                    ) {
                        ApprovalSummaryChip(
                            text = stringResource(R.string.training_approval_training_days, trainingDays.size),
                            modifier = Modifier.weight(1f),
                        )
                        ApprovalSummaryChip(
                            text = stringResource(R.string.training_approval_exercises, exerciseCount),
                            modifier = Modifier.weight(1f),
                        )
                    }
                }

                // Days label
                item {
                    Text(
                        text = stringResource(R.string.training_approval_plan_label),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 1.5.sp,
                        modifier = Modifier.padding(top = AiFitSpacing.sm),
                    )
                }

                // Days
                itemsIndexed(sortedDays) { index, dayItem ->
                    val dayLabel = stringResource(R.string.training_approval_day_label, index + 1)
                    when (dayItem) {
                        is TrainingDayItem.Training -> {
                            val day = dayItem.day
                            ExpandableSection(
                                title = "$dayLabel · ${day.name}",
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
                                        text = "$dayLabel · ${day.name}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                    Text(
                                        text = stringResource(R.string.training_approval_rest),
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
                        text = stringResource(R.string.training_approval_change_plan),
                        onClick = { showFeedbackSheet = true },
                        modifier = Modifier.weight(1f),
                    )
                    PrimaryButton(
                        text = stringResource(R.string.training_approval_accept_plan),
                        onClick = { viewModel.onApprovePlan(planId) },
                        enabled = detailUiState !is TrainingDetailUiState.Loading,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }

    // Feedback ModalBottomSheet (mirrors OnboardingTrainingApprovalScreen)
    if (showFeedbackSheet) {
        ModalBottomSheet(
            onDismissRequest = { showFeedbackSheet = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AiFitSpacing.md)
                    .padding(bottom = AiFitSpacing.xxl),
                verticalArrangement = Arrangement.spacedBy(AiFitSpacing.md),
            ) {
                Text(
                    text = stringResource(R.string.onboarding_adjust_training),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                AiFitTextField(
                    value = feedbackText,
                    onValueChange = { feedbackText = it },
                    label = stringResource(R.string.onboarding_what_to_change),
                    singleLine = false,
                    modifier = Modifier.fillMaxWidth(),
                )
                PrimaryButton(
                    text = stringResource(R.string.onboarding_regenerate),
                    onClick = {
                        scope.launch {
                            sheetState.hide()
                            showFeedbackSheet = false
                            val fb = feedbackText.takeIf { it.isNotBlank() }
                            feedbackText = ""
                            viewModel.onRegenerateApprovalPlan(planId, fb)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
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

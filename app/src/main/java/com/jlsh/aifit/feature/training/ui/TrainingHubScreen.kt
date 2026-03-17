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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jlsh.aifit.core.ui.components.buttons.PrimaryButton
import com.jlsh.aifit.core.ui.components.display.AiFitCard
import com.jlsh.aifit.core.ui.components.display.PlanStatusBadge
import com.jlsh.aifit.core.ui.components.feedback.EmptyStateView
import com.jlsh.aifit.core.ui.components.feedback.ErrorScreen
import com.jlsh.aifit.core.ui.components.feedback.LoadingScreen
import com.jlsh.aifit.core.ui.components.inputs.AiFitChipGroup
import com.jlsh.aifit.core.ui.components.layout.AiFitTopBar
import com.jlsh.aifit.core.ui.theme.AIFitTheme
import com.jlsh.aifit.core.ui.theme.AiFitSpacing
import com.jlsh.aifit.feature.training.domain.model.PlanStatus
import com.jlsh.aifit.feature.training.domain.model.TrainingDay
import com.jlsh.aifit.feature.training.domain.model.TrainingPlan
import com.jlsh.aifit.feature.training.ui.state.TrainingHubUiState
import com.jlsh.aifit.feature.training.ui.state.TrainingUiEvent
import com.jlsh.aifit.feature.user.domain.model.FitnessLevel
import com.jlsh.aifit.feature.user.domain.model.GoalType
import com.jlsh.aifit.feature.user.domain.model.WorkoutLocation
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

private val FILTER_CHIPS = listOf("All", "Active", "Completed", "Paused")

private fun chipToStatus(chip: String): PlanStatus? = when (chip) {
    "Active" -> PlanStatus.ACTIVE
    "Completed" -> PlanStatus.COMPLETED
    "Paused" -> PlanStatus.PAUSED
    else -> null
}

private fun statusToChip(status: PlanStatus?): String = when (status) {
    PlanStatus.ACTIVE -> "Active"
    PlanStatus.COMPLETED -> "Completed"
    PlanStatus.PAUSED -> "Paused"
    else -> "All"
}

@Composable
fun TrainingHubScreen(
    onNavigateToDetail: (planId: String) -> Unit,
    onNavigateToGenerate: (adaptive: Boolean, basePlanId: String?) -> Unit,
    onNavigateToWorkoutLog: (planId: String) -> Unit,
    onNavigateToWorkoutDetail: (logId: String) -> Unit,
    viewModel: TrainingViewModel = hiltViewModel(),
) {
    val hubState by viewModel.hubUiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is TrainingUiEvent.NavigateToDetail -> onNavigateToDetail(event.planId)
                is TrainingUiEvent.NavigateToGenerate -> onNavigateToGenerate(event.adaptive, event.basePlanId)
                is TrainingUiEvent.NavigateToWorkoutLog -> onNavigateToWorkoutLog(event.planId)
                is TrainingUiEvent.ShowSnackbar -> snackbarHostState.showSnackbar(event.message)
                is TrainingUiEvent.PlanDeleted -> { /* refresh handled in VM */ }
                is TrainingUiEvent.NavigateBack -> { /* not used in hub */ }
            }
        }
    }

    Scaffold(
        topBar = {
            AiFitTopBar(
                title = "Training",
                background = MaterialTheme.colorScheme.secondaryContainer,
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            if (hubState is TrainingHubUiState.ActivePlan || hubState is TrainingHubUiState.NoActivePlan) {
                FloatingActionButton(
                    onClick = { viewModel.onNavigateToGenerate() },
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = "New Plan",
                    )
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { paddingValues ->
        when (val state = hubState) {
            is TrainingHubUiState.Loading -> LoadingScreen(
                modifier = Modifier.padding(paddingValues),
            )

            is TrainingHubUiState.Error -> ErrorScreen(
                message = state.message,
                onRetry = viewModel::onRefresh,
                modifier = Modifier.padding(paddingValues),
            )

            is TrainingHubUiState.NoActivePlan -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(top = AiFitSpacing.xxl),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    EmptyStateView(
                        icon = Icons.Rounded.FitnessCenter,
                        title = "Sin planes activos",
                        subtitle = "Genera tu primer plan de entrenamiento con IA",
                        action = {
                            PrimaryButton(
                                text = "CREAR PLAN",
                                onClick = { viewModel.onNavigateToGenerate() },
                                modifier = Modifier.padding(horizontal = AiFitSpacing.xl),
                            )
                        },
                    )
                }
            }

            is TrainingHubUiState.ActivePlan -> {
                ActivePlanContent(
                    state = state,
                    onActivePlanClicked = { viewModel.onPlanClicked(state.plan.id) },
                    onPlanClicked = viewModel::onPlanClicked,
                    onFilterChanged = viewModel::filterPlans,
                    modifier = Modifier.padding(paddingValues),
                )
            }
        }
    }
}

@Composable
private fun ActivePlanContent(
    state: TrainingHubUiState.ActivePlan,
    onActivePlanClicked: () -> Unit,
    onPlanClicked: (String) -> Unit,
    onFilterChanged: (PlanStatus?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val filteredPlans = if (state.selectedFilter == null) {
        state.allPlans
    } else {
        state.allPlans.filter { it.status == state.selectedFilter }
    }

    val selectedChip = statusToChip(state.selectedFilter)

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = AiFitSpacing.md,
            top = AiFitSpacing.sm,
            end = AiFitSpacing.md,
            bottom = AiFitSpacing.xxl + AiFitSpacing.xxl,
        ),
        verticalArrangement = Arrangement.spacedBy(AiFitSpacing.sm),
    ) {
        // ── Top section: Active plan card ──
        item(key = "active_plan_card") {
            AiFitCard(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                onClick = onActivePlanClicked,
            ) {
                Column(
                    modifier = Modifier.padding(AiFitSpacing.md),
                    verticalArrangement = Arrangement.spacedBy(AiFitSpacing.sm),
                ) {
                    Text(
                        text = state.plan.name,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )

                    Text(
                        text = "Week ${state.currentWeek} of ${state.plan.durationWeeks}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    if (state.nextDay != null) {
                        Text(
                            text = "Next: ${state.nextDay.name}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primaryContainer,
                        )
                    }
                }
            }
        }

        // ── Bottom section: Filter chips ──
        item(key = "filter_chips") {
            Spacer(modifier = Modifier.height(AiFitSpacing.sm))
            AiFitChipGroup(
                options = FILTER_CHIPS,
                selected = setOf(selectedChip),
                onSelectionChanged = { selection ->
                    val chip = selection.firstOrNull() ?: "All"
                    onFilterChanged(chipToStatus(chip))
                },
                multiSelect = false,
            )
            Spacer(modifier = Modifier.height(AiFitSpacing.xs))
        }

        // ── Bottom section: Plan list ──
        items(filteredPlans, key = { it.id }) { plan ->
            PlanSummaryItem(
                plan = plan,
                onClick = { onPlanClicked(plan.id) },
            )
        }
    }
}

@Composable
private fun PlanSummaryItem(
    plan: TrainingPlan,
    onClick: () -> Unit,
) {
    AiFitCard(onClick = onClick) {
        Column(
            modifier = Modifier.padding(AiFitSpacing.md),
            verticalArrangement = Arrangement.spacedBy(AiFitSpacing.xs),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = plan.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                )
                PlanStatusBadge(status = plan.status.name)
            }

            Text(
                text = plan.createdAt.format(DateTimeFormatter.ofPattern("dd MMM yyyy")),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}


@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "TrainingHubScreen Active Dark",
)
@Composable
private fun TrainingHubScreenActivePreview() {
    AIFitTheme(darkTheme = true) {
        val fakePlans = listOf(
            TrainingPlan(
                id = "1",
                name = "Plan de Fuerza 5x5",
                description = "Plan de fuerza básico",
                frequencyDaysPerWeek = 3,
                durationWeeks = 8,
                goalType = GoalType.GAIN_MUSCLE,
                fitnessLevel = FitnessLevel.INTERMEDIATE,
                location = WorkoutLocation.GYM,
                status = PlanStatus.ACTIVE,
                totalDays = 24,
                createdAt = LocalDateTime.now().minusWeeks(2),
                days = listOf(
                    TrainingDay(
                        id = "d1",
                        dayNumber = 1,
                        name = "Push Day",
                        estimatedDurationMinutes = 60,
                        exercises = emptyList(),
                    ),
                ),
            ),
            TrainingPlan(
                id = "2",
                name = "Cardio HIIT",
                description = null,
                frequencyDaysPerWeek = 4,
                durationWeeks = 6,
                goalType = GoalType.LOSE_WEIGHT,
                fitnessLevel = FitnessLevel.BEGINNER,
                location = WorkoutLocation.HOME,
                status = PlanStatus.COMPLETED,
                totalDays = 24,
                createdAt = LocalDateTime.now().minusMonths(2),
            ),
        )

        val fakeState = TrainingHubUiState.ActivePlan(
            plan = fakePlans.first(),
            currentWeek = 3,
            nextDay = fakePlans.first().days.firstOrNull(),
            allPlans = fakePlans,
            selectedFilter = null,
        )

        ActivePlanContent(
            state = fakeState,
            onActivePlanClicked = {},
            onPlanClicked = {},
            onFilterChanged = {},
        )
    }
}

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "TrainingHubScreen Empty Dark",
)
@Composable
private fun TrainingHubScreenEmptyPreview() {
    AIFitTheme(darkTheme = true) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = AiFitSpacing.xxl),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            EmptyStateView(
                icon = Icons.Rounded.FitnessCenter,
                title = "Sin planes activos",
                subtitle = "Genera tu primer plan de entrenamiento con IA",
                action = {
                    PrimaryButton(
                        text = "CREAR PLAN",
                        onClick = {},
                        modifier = Modifier.padding(horizontal = AiFitSpacing.xl),
                    )
                },
            )
        }
    }
}


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
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jlsh.aifit.R
import com.jlsh.aifit.core.ui.components.buttons.PrimaryButton
import com.jlsh.aifit.core.ui.components.display.AiFitCard
import com.jlsh.aifit.core.ui.components.display.PlanStatusBadge
import com.jlsh.aifit.core.ui.components.feedback.ConfirmationDialog
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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle

private val FILTER_CHIPS = listOf("Todos", "Activo", "Completado", "Pausado")

private fun chipToStatus(chip: String): PlanStatus? = when (chip) {
    "Activo" -> PlanStatus.ACTIVE
    "Completado" -> PlanStatus.COMPLETED
    "Pausado" -> PlanStatus.PAUSED
    else -> null
}

private fun statusToChip(status: PlanStatus?): String = when (status) {
    PlanStatus.ACTIVE -> "Activo"
    PlanStatus.COMPLETED -> "Completado"
    PlanStatus.PAUSED -> "Pausado"
    else -> "Todos"
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
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            viewModel.onRefresh()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is TrainingUiEvent.NavigateToDetail -> onNavigateToDetail(event.planId)
                is TrainingUiEvent.NavigateToGenerate -> onNavigateToGenerate(event.adaptive, event.basePlanId)
                is TrainingUiEvent.NavigateToApproval -> { /* not applicable from hub screen */ }
                is TrainingUiEvent.NavigateToWorkoutLog -> onNavigateToWorkoutLog(event.planId)
                is TrainingUiEvent.ShowSnackbar -> snackbarHostState.showSnackbar(event.message)
                is TrainingUiEvent.PlanDeleted -> { }
                is TrainingUiEvent.NavigateBack -> { }
            }
        }
    }

    Scaffold(
        topBar = {
            AiFitTopBar(
                title = "Entrenamiento",
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
                        contentDescription = "Nuevo plan",
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
                    onActivatePlan = viewModel::onActivatePlan,
                    onDeletePlan = viewModel::onDeletePlan,
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
    onActivatePlan: (String) -> Unit,
    onDeletePlan: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var planToDelete by remember { mutableStateOf<String?>(null) }
    var planToActivate by remember { mutableStateOf<String?>(null) }

    if (planToDelete != null) {
        ConfirmationDialog(
            title = stringResource(R.string.training_delete_plan_title),
            message = stringResource(R.string.training_delete_plan_message),
            onConfirm = {
                planToDelete?.let { onDeletePlan(it) }
            },
            onDismiss = { planToDelete = null },
        )
    }

    if (planToActivate != null) {
        ConfirmationDialog(
            title = "Activar plan",
            message = "Solo puede haber un plan activo. El plan actual pasará a pausado.",
            onConfirm = {
                planToActivate?.let { onActivatePlan(it) }
                planToActivate = null
            },
            onDismiss = { planToActivate = null },
        )
    }

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
                onActivate = { planToActivate = plan.id },
                onDelete = { planToDelete = plan.id },
            )
        }
    }
}

@Composable
private fun PlanSummaryItem(
    plan: TrainingPlan,
    onClick: () -> Unit,
    onActivate: () -> Unit,
    onDelete: () -> Unit,
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
                if (plan.status != PlanStatus.ACTIVE) {
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Rounded.DeleteOutline,
                            contentDescription = stringResource(R.string.common_delete),
                            tint = MaterialTheme.colorScheme.error,
                        )
                    }
                }
                PlanStatusBadge(status = plan.status.name)
            }

            Text(
                text = plan.createdAt.format(DateTimeFormatter.ofPattern("dd MMM yyyy")),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            if (plan.status != PlanStatus.ACTIVE && plan.status != PlanStatus.COMPLETED) {
                TextButton(onClick = onActivate) {
                    Text(
                        text = "Activar",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primaryContainer,
                    )
                }
            }
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
            onActivatePlan = {},
            onDeletePlan = {},
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

// U-12/U-13 compliance fix — light mode preview
@Preview(
    showBackground = true,
    name = "TrainingHubScreen Active Light",
)
@Composable
private fun TrainingHubScreenActiveLightPreview() {
    AIFitTheme(darkTheme = false) {
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
            onActivatePlan = {},
            onDeletePlan = {},
        )
    }
}




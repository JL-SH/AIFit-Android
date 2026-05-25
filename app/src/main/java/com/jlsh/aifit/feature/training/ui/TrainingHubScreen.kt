package com.jlsh.aifit.feature.training.ui

import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.*
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
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
import com.jlsh.aifit.core.ui.components.feedback.ConfirmationDialog
import com.jlsh.aifit.core.ui.components.plans.PlanFilterChipGroup
import com.jlsh.aifit.core.ui.components.plans.PlanHubActiveCard
import com.jlsh.aifit.core.ui.components.plans.PlanSummaryCard
import com.jlsh.aifit.core.ui.components.feedback.EmptyStateKind
import com.jlsh.aifit.core.ui.components.feedback.EmptyStateView
import com.jlsh.aifit.core.ui.components.feedback.ErrorScreen
import com.jlsh.aifit.core.ui.components.feedback.LoadingScreen
import com.jlsh.aifit.core.ui.components.layout.AiFitTopBar
import com.jlsh.aifit.core.ui.theme.AIFitTheme
import com.jlsh.aifit.core.ui.theme.AiFitSpacing
import com.jlsh.aifit.feature.training.domain.model.PlanStatus
import com.jlsh.aifit.feature.training.domain.model.TrainingDay
import com.jlsh.aifit.feature.training.domain.model.TrainingPlan
import com.jlsh.aifit.feature.training.ui.state.TrainingHubUiState
import com.jlsh.aifit.feature.training.ui.state.TrainingUiState
import com.jlsh.aifit.feature.training.ui.state.TrainingUiEvent
import com.jlsh.aifit.feature.user.domain.model.FitnessLevel
import com.jlsh.aifit.feature.user.domain.model.GoalType
import com.jlsh.aifit.feature.user.domain.model.WorkoutLocation
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle

/**
 * Training hub screen: highlighted active plan, filterable list and access to history.
 *
 * Shows statuses of loading, error, empty (no active plan), or content with plan card
 * active, filter chips and summary cards. Includes FAB to create plan and history icon.
 *
 * @param onNavigateToDetail Detail navigation of a plan.
 * @param onNavigateToGenerate Navigate to the generation flow (`adaptive`, `basePlanId` optional).
 * @param onNavigateToWorkoutLog Navigating to a plan's session log.
 * @param onNavigateToWorkoutDetail Detail navigation of a log (not used in this screen).
 * @param onNavigateToWorkoutHistory Navigation to workout history.
 * @param viewModel ViewModel that provides [TrainingHubUiState] and navigation events.
 */
@Composable
fun TrainingHubScreen(
    onNavigateToDetail: (planId: String) -> Unit,
    onNavigateToGenerate: (adaptive: Boolean, basePlanId: String?) -> Unit,
    onNavigateToWorkoutLog: (planId: String) -> Unit,
    onNavigateToWorkoutDetail: (logId: String) -> Unit,
    onNavigateToWorkoutHistory: () -> Unit = {},
    viewModel: TrainingViewModel = hiltViewModel(),
) {
    val hubState by viewModel.hubUiState.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
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
                is TrainingUiEvent.NavigateToWorkoutHistory -> onNavigateToWorkoutHistory()
                is TrainingUiEvent.ShowSnackbar -> snackbarHostState.showSnackbar(event.message)
                is TrainingUiEvent.PlanDeleted -> { }
                is TrainingUiEvent.PlanApproved -> { }
                is TrainingUiEvent.NavigateBack -> { }
            }
        }
    }

    Scaffold(
        topBar = {
            AiFitTopBar(
                title = stringResource(R.string.training_hub_title),
                background = MaterialTheme.colorScheme.secondaryContainer,
                actions = {
                    IconButton(onClick = { viewModel.onNavigateToWorkoutHistory() }) {
                        Icon(
                            imageVector = PhosphorIcons.Regular.ClockCounterClockwise,
                            contentDescription = stringResource(R.string.training_hub_history_cd),
                        )
                    }
                },
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
                        imageVector = PhosphorIcons.Regular.Plus,
                        contentDescription = stringResource(R.string.training_hub_new_plan_cd),
                    )
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { paddingValues ->
        val isActivating = (uiState as? TrainingUiState.Success)?.isActivatingPlan == true

        if (isActivating) {
            LoadingScreen(modifier = Modifier.padding(paddingValues))
        } else when (val state = hubState) {
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
                        icon = PhosphorIcons.Regular.Barbell,
                        kind = EmptyStateKind.TrainingPlans,
                        title = stringResource(R.string.training_hub_no_active_title),
                        subtitle = stringResource(R.string.training_hub_no_active_subtitle),
                        action = {
                            PrimaryButton(
                                text = stringResource(R.string.training_hub_create_plan),
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
            title = stringResource(R.string.training_hub_activate_plan_title),
            message = stringResource(R.string.training_hub_activate_plan_message),
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
        // ── Top section: Activate plan card ──
        item(key = "active_plan_card") {
            PlanHubActiveCard(
                planName = state.plan.name,
                status = state.plan.status,
                primarySubtitle = stringResource(
                    R.string.training_hub_week_of,
                    state.currentWeek,
                    state.plan.durationWeeks,
                ),
                secondarySubtitle = state.nextDay?.let {
                    stringResource(R.string.training_hub_next_day, it.name)
                },
                onClick = onActivePlanClicked,
            )
        }

        item(key = "filter_chips") {
            Spacer(modifier = Modifier.height(AiFitSpacing.sm))
            PlanFilterChipGroup(
                selectedFilter = state.selectedFilter,
                onFilterChanged = onFilterChanged,
            )
            Spacer(modifier = Modifier.height(AiFitSpacing.xs))
        }

        items(filteredPlans, key = { it.id }) { plan ->
            PlanSummaryCard(
                name = plan.name,
                status = plan.status,
                subtitle = plan.createdAt.format(DateTimeFormatter.ofPattern("dd MMM yyyy")),
                onClick = { onPlanClicked(plan.id) },
                onActivate = { planToActivate = plan.id },
                onDelete = { planToDelete = plan.id },
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
                icon = PhosphorIcons.Regular.Barbell,
                kind = EmptyStateKind.TrainingPlans,
                title = stringResource(R.string.training_empty_title),
                subtitle = stringResource(R.string.training_empty_subtitle),
                action = {
                    PrimaryButton(
                        text = stringResource(R.string.home_create_plan_btn),
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




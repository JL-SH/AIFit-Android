package com.jlsh.aifit.feature.training.ui

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.jlsh.aifit.core.ui.components.display.AdherenceBar
import com.jlsh.aifit.core.ui.components.display.AiFitCard
import com.jlsh.aifit.core.ui.components.display.PlanStatusBadge
import com.jlsh.aifit.core.ui.components.feedback.ConfirmationDialog
import com.jlsh.aifit.core.ui.components.feedback.EmptyStateView
import com.jlsh.aifit.core.ui.components.layout.AiFitTabRow
import com.jlsh.aifit.core.ui.components.layout.AiFitTopBar
import com.jlsh.aifit.core.ui.components.layout.ScreenScaffold
import com.jlsh.aifit.core.ui.theme.AIFitTheme
import com.jlsh.aifit.core.ui.theme.AiFitSpacing
import com.jlsh.aifit.feature.training.domain.model.PlanStatus
import com.jlsh.aifit.feature.training.domain.model.TrainingPlan
import com.jlsh.aifit.feature.training.ui.state.TrainingUiEvent
import com.jlsh.aifit.feature.training.ui.state.TrainingUiState
import com.jlsh.aifit.feature.user.domain.model.FitnessLevel
import com.jlsh.aifit.feature.user.domain.model.GoalType
import com.jlsh.aifit.feature.user.domain.model.PreferredLocation
import java.time.LocalDateTime

private val HUB_TABS = listOf("MY PLANS", "WORKOUT LOG")

@Composable
fun TrainingHubScreen(
    onNavigateToDetail: (planId: String) -> Unit,
    onNavigateToGenerate: (adaptive: Boolean, basePlanId: String?) -> Unit,
    onNavigateToWorkoutLog: (planId: String) -> Unit,
    viewModel: TrainingViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val selectedTabIndex by viewModel.selectedTabIndex.collectAsStateWithLifecycle()

    var showDeleteDialog by remember { mutableStateOf(false) }
    var planToDeleteId by remember { mutableStateOf<String?>(null) }
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

    ScreenScaffold<TrainingUiState.Success>(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        topBar = {
            AiFitTopBar(
                title = "Training",
                background = MaterialTheme.colorScheme.secondaryContainer,
            )
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = selectedTabIndex == 0,
                enter = scaleIn() + fadeIn(),
                exit = scaleOut() + fadeOut(),
            ) {
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
        onRetry = viewModel::onRefresh,
    ) { _, successState ->
        Column(modifier = Modifier.fillMaxSize()) {
            AiFitTabRow(
                tabs = HUB_TABS,
                selectedIndex = selectedTabIndex,
                onTabSelected = viewModel::onTabSelected,
            )

            when (selectedTabIndex) {
                0 -> MyPlansTab(
                    state = successState,
                    onPlanClicked = viewModel::onPlanClicked,
                    onStartSession = viewModel::onStartSession,
                    onDeletePlan = { planId ->
                        planToDeleteId = planId
                        showDeleteDialog = true
                    },
                    onCreatePlan = { viewModel.onNavigateToGenerate() },
                )
                1 -> WorkoutHistoryPlaceholder()
            }
        }
    }

    if (showDeleteDialog && planToDeleteId != null) {
        ConfirmationDialog(
            title = "Eliminar plan",
            message = "Esta acción no se puede deshacer.",
            onConfirm = {
                planToDeleteId?.let { viewModel.onDeletePlan(it) }
                showDeleteDialog = false
                planToDeleteId = null
            },
            onDismiss = {
                showDeleteDialog = false
                planToDeleteId = null
            },
        )
    }
}

@Composable
private fun MyPlansTab(
    state: TrainingUiState.Success,
    onPlanClicked: (String) -> Unit,
    onStartSession: (String) -> Unit,
    onDeletePlan: (String) -> Unit,
    onCreatePlan: () -> Unit,
) {
    if (state.plans.isEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = AiFitSpacing.xxl),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            EmptyStateView(
                icon = Icons.Rounded.FitnessCenter,
                title = "Sin planes",
                subtitle = "Genera tu primer plan de entrenamiento con IA",
                action = {
                    PrimaryButton(
                        text = "CREAR PLAN",
                        onClick = onCreatePlan,
                        modifier = Modifier.padding(horizontal = AiFitSpacing.xl),
                    )
                },
            )
        }
        return
    }

    LazyColumn(
        contentPadding = PaddingValues(
            start = AiFitSpacing.md,
            top = AiFitSpacing.sm,
            end = AiFitSpacing.md,
            bottom = 88.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(AiFitSpacing.sm),
    ) {
        // Active plan card — prominent
        state.activePlan?.let { activePlan ->
            item(key = "active_${activePlan.id}") {
                ActivePlanCard(
                    plan = activePlan,
                    onStartSession = { onStartSession(activePlan.id) },
                    onDeletePlan = { onDeletePlan(activePlan.id) },
                    onClick = { onPlanClicked(activePlan.id) },
                )
            }
        }

        // Other plans
        val otherPlans = state.plans.filter { it.id != state.activePlan?.id }
        items(otherPlans, key = { it.id }) { plan ->
            PlanCard(
                plan = plan,
                onClick = { onPlanClicked(plan.id) },
            )
        }
    }
}

@Composable
private fun ActivePlanCard(
    plan: TrainingPlan,
    onStartSession: () -> Unit,
    onDeletePlan: () -> Unit,
    onClick: () -> Unit,
) {
    AiFitCard(
        containerColor = MaterialTheme.colorScheme.secondaryContainer,
        onClick = onClick,
    ) {
        Column(
            modifier = Modifier.padding(AiFitSpacing.md),
            verticalArrangement = Arrangement.spacedBy(AiFitSpacing.sm),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                PlanStatusBadge(status = plan.status.name)
                IconButton(onClick = onDeletePlan, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Rounded.MoreVert,
                        contentDescription = "Options",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }

            Text(
                text = plan.name,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Text(
                text = "${plan.frequencyDaysPerWeek} days/week • ${plan.durationWeeks} weeks",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            AdherenceBar(percentage = 0f)

            Spacer(modifier = Modifier.height(AiFitSpacing.xs))

            PrimaryButton(
                text = "CONTINUE SESSION",
                onClick = onStartSession,
            )
        }
    }
}

@Composable
private fun PlanCard(
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
                text = "${plan.frequencyDaysPerWeek} days/week • ${plan.goalType.name.replace("_", " ")} • ${plan.fitnessLevel.name}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
fun WorkoutHistoryPlaceholder() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = AiFitSpacing.xxl),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        EmptyStateView(
            icon = Icons.Rounded.FitnessCenter,
            title = "Historial de sesiones",
            subtitle = "El historial de sesiones aparecerá aquí",
        )
    }
}

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "TrainingHubScreen Dark",
)
@Composable
private fun TrainingHubScreenPreview() {
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
                location = PreferredLocation.GYM,
                status = PlanStatus.ACTIVE,
                totalDays = 24,
                createdAt = LocalDateTime.now(),
            ),
            TrainingPlan(
                id = "2",
                name = "Cardio HIIT",
                description = null,
                frequencyDaysPerWeek = 4,
                durationWeeks = 6,
                goalType = GoalType.LOSE_WEIGHT,
                fitnessLevel = FitnessLevel.BEGINNER,
                location = PreferredLocation.HOME,
                status = PlanStatus.COMPLETED,
                totalDays = 24,
                createdAt = LocalDateTime.now(),
            ),
        )

        val fakeState = TrainingUiState.Success(
            plans = fakePlans,
            activePlan = fakePlans.first(),
        )

        Column(modifier = Modifier.fillMaxSize()) {
            AiFitTabRow(
                tabs = HUB_TABS,
                selectedIndex = 0,
                onTabSelected = {},
            )
            MyPlansTab(
                state = fakeState,
                onPlanClicked = {},
                onStartSession = {},
                onDeletePlan = {},
                onCreatePlan = {},
            )
        }
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
        val fakeState = TrainingUiState.Success(plans = emptyList())
        Column(modifier = Modifier.fillMaxSize()) {
            AiFitTabRow(
                tabs = HUB_TABS,
                selectedIndex = 0,
                onTabSelected = {},
            )
            MyPlansTab(
                state = fakeState,
                onPlanClicked = {},
                onStartSession = {},
                onDeletePlan = {},
                onCreatePlan = {},
            )
        }
    }
}





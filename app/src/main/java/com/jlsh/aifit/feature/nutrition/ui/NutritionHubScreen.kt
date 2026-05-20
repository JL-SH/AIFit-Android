package com.jlsh.aifit.feature.nutrition.ui

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.ShoppingCart
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.jlsh.aifit.R
import com.jlsh.aifit.core.ui.components.buttons.PrimaryButton
import com.jlsh.aifit.core.ui.components.display.AiFitCard
import com.jlsh.aifit.core.ui.components.display.MacroRingChart
import com.jlsh.aifit.core.ui.components.display.MacroRingData
import com.jlsh.aifit.core.ui.components.display.PlanStatusBadge
import com.jlsh.aifit.core.ui.components.feedback.ConfirmationDialog
import com.jlsh.aifit.core.ui.components.feedback.EmptyStateView
import com.jlsh.aifit.core.ui.components.inputs.AiFitChipGroup
import com.jlsh.aifit.core.ui.components.list.SwipeableListItem
import com.jlsh.aifit.core.ui.components.layout.AiFitTabRow
import com.jlsh.aifit.core.ui.components.layout.AiFitTopBar
import com.jlsh.aifit.core.ui.components.layout.ScreenScaffold
import com.jlsh.aifit.core.ui.components.layout.SectionHeader
import com.jlsh.aifit.core.ui.theme.AIFitTheme
import com.jlsh.aifit.core.ui.theme.AiFitSpacing
import com.jlsh.aifit.feature.diet.domain.model.DietPlan
import com.jlsh.aifit.feature.diet.domain.model.MealType
import com.jlsh.aifit.feature.nutrition.domain.model.MealLog
import com.jlsh.aifit.feature.nutrition.domain.model.NutritionLog
import com.jlsh.aifit.feature.nutrition.domain.model.NutritionTarget
import com.jlsh.aifit.feature.nutrition.domain.model.TargetSource
import com.jlsh.aifit.feature.nutrition.ui.components.TrackMealSheet
import com.jlsh.aifit.feature.nutrition.ui.state.NutritionHubUiState
import com.jlsh.aifit.feature.nutrition.ui.state.NutritionUiEvent
import com.jlsh.aifit.feature.nutrition.ui.state.TodayState
import com.jlsh.aifit.feature.shopping.ui.ShoppingViewModel
import com.jlsh.aifit.feature.shopping.ui.components.GenerateShoppingListSheet
import com.jlsh.aifit.feature.shopping.ui.state.ShoppingListUiState
import com.jlsh.aifit.feature.shopping.ui.state.ShoppingUiEvent
import com.jlsh.aifit.feature.training.domain.model.PlanStatus
import com.jlsh.aifit.feature.user.ui.toStringRes
import kotlinx.coroutines.launch
import java.time.LocalDate

private val DIET_STATUS_KEYS = listOf("all", "active", "completed", "paused")

private fun dietKeyToStatus(key: String): PlanStatus? = when (key) {
    "active" -> PlanStatus.ACTIVE
    "paused" -> PlanStatus.PAUSED
    "completed" -> PlanStatus.COMPLETED
    else -> null
}

private fun dietStatusToKey(status: PlanStatus?): String = when (status) {
    PlanStatus.ACTIVE -> "active"
    PlanStatus.PAUSED -> "paused"
    PlanStatus.COMPLETED -> "completed"
    else -> "all"
}

@Composable
private fun dietKeyDisplayName(key: String): String = stringResource(
    when (key) {
        "active" -> R.string.plan_status_active
        "paused" -> R.string.plan_status_paused
        "completed" -> R.string.plan_status_completed
        else -> R.string.plan_status_all
    }
)

@Composable
private fun mealTypeDisplay(mealType: MealType): String = stringResource(
        when (mealType) {
            MealType.BREAKFAST -> R.string.meal_type_breakfast
            MealType.MID_MORNING -> R.string.meal_type_morning_snack
            MealType.LUNCH -> R.string.meal_type_lunch
            MealType.AFTERNOON_SNACK -> R.string.meal_type_snack
            MealType.DINNER -> R.string.meal_type_dinner
            MealType.PRE_WORKOUT -> R.string.meal_type_pre_workout
            MealType.POST_WORKOUT -> R.string.meal_type_post_workout
            else -> R.string.meal_type_unknown
        }
    )

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NutritionHubScreen(
    onNavigateToTrackMeal: (mode: String) -> Unit,
    onNavigateToFoodVision: () -> Unit,
    onNavigateToNutritionTarget: () -> Unit,
    onNavigateToDietDetail: (planId: String) -> Unit,
    onNavigateToGenerateDiet: () -> Unit,
    onNavigateToShoppingDetail: (listId: String) -> Unit,
    viewModel: NutritionViewModel = hiltViewModel(),
) {
    val hubState by viewModel.hubState.collectAsStateWithLifecycle()
    val selectedTabIndex by viewModel.selectedTabIndex.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val lifecycleOwner = LocalLifecycleOwner.current
    var showSheet by remember { mutableStateOf(false) }
    var mealToDeleteId by remember { mutableStateOf<String?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            viewModel.onRefresh()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is NutritionUiEvent.ShowTrackMealSheet -> showSheet = true
                is NutritionUiEvent.NavigateToTrackMeal -> onNavigateToTrackMeal(event.mode)
                is NutritionUiEvent.NavigateToNutritionTarget -> onNavigateToNutritionTarget()
                is NutritionUiEvent.NavigateToDietDetail -> onNavigateToDietDetail(event.planId)
                is NutritionUiEvent.NavigateToGenerateDiet -> onNavigateToGenerateDiet()
                is NutritionUiEvent.ShowSnackbar -> snackbarHostState.showSnackbar(event.message)
                is NutritionUiEvent.MealDeleted -> { /* hub refreshes via loadHubData */ }
                else -> {}
            }
        }
    }

    val hubTabs = listOf(
        stringResource(R.string.nutrition_hub_tab_today),
        stringResource(R.string.nutrition_hub_tab_diet_plan),
        stringResource(R.string.nutrition_hub_tab_shopping),
    )

    ScreenScaffold<NutritionHubUiState.Success>(
        uiState = hubState,
        snackbarHostState = snackbarHostState,
        topBar = {
            AiFitTopBar(
                title = stringResource(R.string.nutrition_hub_title),
                background = MaterialTheme.colorScheme.secondaryContainer,
            )
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = selectedTabIndex == 0 || selectedTabIndex == 1,
                enter = scaleIn() + fadeIn(),
                exit = scaleOut() + fadeOut(),
            ) {
                FloatingActionButton(
                    onClick = {
                        when (selectedTabIndex) {
                            0 -> showSheet = true
                            1 -> viewModel.onGenerateDietClicked()
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = when (selectedTabIndex) {
                            0 -> stringResource(R.string.nutrition_hub_fab_add_meal)
                            1 -> stringResource(R.string.nutrition_hub_fab_new_plan)
                            else -> stringResource(R.string.nutrition_hub_generate_list)
                        },
                    )
                }
            }
        },
        onRetry = viewModel::onRefresh,
    ) { paddingValues, successState ->
        Column(
            modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
        ) {
            AiFitTabRow(
                tabs = hubTabs,
                selectedIndex = selectedTabIndex,
                onTabSelected = viewModel::onTabSelected,
            )

            when (selectedTabIndex) {
                0 -> TodayTab(
                    todayState = successState.todayState,
                    onRingClicked = { viewModel.onNavigateToTarget() },
                    onDeleteMeal = { mealId ->
                        mealToDeleteId = mealId
                        showDeleteDialog = true
                    },
                    onAddMeal = { showSheet = true },
                )
                1 -> DietPlanTab(
                    plans = successState.dietPlans,
                    selectedFilter = successState.selectedDietPlanFilter,
                    isActivatingPlan = successState.isActivatingPlan,
                    onFilterChanged = viewModel::onDietPlanFilterChanged,
                    onPlanClicked = viewModel::onDietPlanClicked,
                    onActivatePlan = viewModel::onActivateDietPlan,
                    onDeletePlan = viewModel::onDeleteDietPlan,
                    onCreatePlan = { viewModel.onGenerateDietClicked() },
                )
                2 -> ShoppingTab(
                    dietPlans = successState.dietPlans,
                    onNavigateToDetail = onNavigateToShoppingDetail,
                )
            }
        }
    }

    if (showDeleteDialog && mealToDeleteId != null) {
        ConfirmationDialog(
            title = stringResource(R.string.nutrition_hub_delete_meal_title),
            message = stringResource(R.string.common_irreversible_action),
            onConfirm = {
                mealToDeleteId?.let { viewModel.onDeleteMeal(it) }
                showDeleteDialog = false
                mealToDeleteId = null
            },
            onDismiss = {
                showDeleteDialog = false
                mealToDeleteId = null
            },
        )
    }

    if (showSheet) {
        TrackMealSheet(
            sheetState = sheetState,
            onDismiss = { showSheet = false },
            onManual = {
                scope.launch { sheetState.hide() }
                showSheet = false
                onNavigateToTrackMeal("manual")
            },
            onScanPhoto = {
                scope.launch { sheetState.hide() }
                showSheet = false
                onNavigateToFoodVision()
            },
            onAnalyzeText = {
                scope.launch { sheetState.hide() }
                showSheet = false
                onNavigateToTrackMeal("text_analysis")
            },
        )
    }
}

@Composable
private fun TodayTab(
    todayState: TodayState,
    onRingClicked: () -> Unit,
    onDeleteMeal: (String) -> Unit,
    onAddMeal: () -> Unit,
) {
    val log = todayState.nutritionLog
    val target = todayState.target

    // Only show the full "configure" empty state when there is no target at all.
    // A null log (no meals logged today) is perfectly normal and should NOT block
    // the tab — we just show the ring chart with 0 consumed.
    if (target == null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = AiFitSpacing.xxl),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            EmptyStateView(
                icon = Icons.Rounded.Restaurant,
                title = stringResource(R.string.nutrition_hub_no_target_title),
                subtitle = stringResource(R.string.nutrition_hub_no_target_subtitle),
                action = {
                    PrimaryButton(
                        text = stringResource(R.string.nutrition_hub_configure),
                        onClick = onRingClicked,
                        modifier = Modifier.padding(horizontal = AiFitSpacing.xl),
                    )
                },
            )
        }
        return
    }

    val meals = log?.meals ?: emptyList()

    LazyColumn(
        contentPadding = PaddingValues(
            start = AiFitSpacing.md,
            top = AiFitSpacing.md,
            end = AiFitSpacing.md,
            bottom = 88.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(AiFitSpacing.md),
    ) {
        item(key = "ring") {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onRingClicked),
                contentAlignment = Alignment.Center,
            ) {
                MacroRingChart(
                    data = MacroRingData(
                        currentCalories = (log?.totalCalories ?: 0).toFloat(),
                        targetCalories = target.calorieTarget.toFloat(),
                        currentProtein = (log?.totalProteinGrams ?: 0.0).toFloat(),
                        targetProtein = target.proteinTarget.toFloat(),
                        currentCarbs = (log?.totalCarbsGrams ?: 0.0).toFloat(),
                        targetCarbs = target.carbsTarget.toFloat(),
                        currentFat = (log?.totalFatGrams ?: 0.0).toFloat(),
                        targetFat = target.fatTarget.toFloat(),
                    ),
                    size = 180.dp,
                )
            }
        }

        item(key = "meals_header") {
            SectionHeader(title = stringResource(R.string.nutrition_hub_meals_header))
        }

        if (meals.isEmpty()) {
            item(key = "empty") {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = AiFitSpacing.lg),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    EmptyStateView(
                        icon = Icons.Rounded.Restaurant,
                        title = stringResource(R.string.nutrition_hub_no_meals_title),
                        subtitle = stringResource(R.string.nutrition_hub_no_meals_subtitle),
                        action = {
                            PrimaryButton(
                                text = stringResource(R.string.nutrition_hub_add_meal_btn),
                                onClick = onAddMeal,
                                modifier = Modifier.padding(horizontal = AiFitSpacing.xl),
                            )
                        },
                    )
                }
            }
        } else {
            items(meals, key = { it.id }) { meal ->
                SwipeableListItem(
                    onDelete = { onDeleteMeal(meal.id) },
                ) {
                    MealRow(meal = meal)
                }
            }
        }
    }
}

@Composable
private fun MealRow(
    meal: MealLog,
) {
    val mealTypeLabel = mealTypeDisplay(meal.mealType)
    AiFitCard {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AiFitSpacing.md),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(AiFitSpacing.sm),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        shape = RoundedCornerShape(6.dp),
                    ) {
                        Text(
                            text = mealTypeLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        )
                    }
                    if (meal.aiGenerated) {
                        Icon(
                            imageVector = Icons.Rounded.AutoAwesome,
                            contentDescription = stringResource(R.string.nutrition_hub_ai_generated_cd),
                            tint = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(14.dp),
                        )
                    }
                }
                Spacer(modifier = Modifier.height(AiFitSpacing.xs))
                Text(
                    text = meal.name ?: mealTypeLabel,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = meal.time,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                text = "${meal.calories} kcal",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primaryContainer,
            )
        }
    }
}

@Composable
private fun DietPlanTab(
    plans: List<DietPlan>,
    selectedFilter: PlanStatus?,
    isActivatingPlan: Boolean,
    onFilterChanged: (PlanStatus?) -> Unit,
    onPlanClicked: (String) -> Unit,
    onActivatePlan: (String) -> Unit,
    onDeletePlan: (String) -> Unit,
    onCreatePlan: () -> Unit,
) {
    var planToDelete by remember { mutableStateOf<String?>(null) }
    var planToActivate by remember { mutableStateOf<String?>(null) }
    val activePlanName = plans.firstOrNull {
        it.status == PlanStatus.ACTIVE
    }?.name

    // Build display map for filter chips (must be Composable context)
    val dietKeyDisplayMap = mapOf(
        "all" to stringResource(R.string.plan_status_all),
        "active" to stringResource(R.string.plan_status_active),
        "draft" to stringResource(R.string.plan_status_draft),
        "paused" to stringResource(R.string.plan_status_paused),
        "completed" to stringResource(R.string.plan_status_completed),
    )

    // Delete confirmation dialog
    if (planToDelete != null) {
        ConfirmationDialog(
            title = stringResource(R.string.nutrition_hub_delete_diet_plan_title),
            message = stringResource(R.string.common_irreversible_action),
            onConfirm = {
                planToDelete?.let { onDeletePlan(it) }
                planToDelete = null
            },
            onDismiss = { planToDelete = null },
        )
    }

    // Activate confirmation dialog (only when there is already an active plan)
    if (planToActivate != null) {
        val message = if (activePlanName != null) {
            stringResource(R.string.nutrition_hub_activate_plan_pause_message, activePlanName)
        } else {
            stringResource(R.string.nutrition_hub_activate_plan_message)
        }
        ConfirmationDialog(
            title = stringResource(R.string.nutrition_hub_activate_plan_title),
            message = message,
            onConfirm = {
                planToActivate?.let { onActivatePlan(it) }
                planToActivate = null
            },
            onDismiss = { planToActivate = null },
        )
    }

    if (plans.isEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = AiFitSpacing.xxl),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            EmptyStateView(
                icon = Icons.Rounded.Restaurant,
                title = stringResource(R.string.nutrition_hub_no_diet_plans_title),
                subtitle = stringResource(R.string.nutrition_hub_no_diet_plans_subtitle),
                action = {
                    PrimaryButton(
                        text = stringResource(R.string.nutrition_hub_create_plan),
                        onClick = onCreatePlan,
                        modifier = Modifier.padding(horizontal = AiFitSpacing.xl),
                    )
                },
            )
        }
        return
    }

    val selectedKey = dietStatusToKey(selectedFilter)

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            contentPadding = PaddingValues(
                start = AiFitSpacing.md,
                top = AiFitSpacing.sm,
                end = AiFitSpacing.md,
                bottom = 88.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(AiFitSpacing.sm),
        ) {
            // ── Active plan highlight ──
            if (selectedFilter == null || selectedFilter == PlanStatus.ACTIVE) {
                val activePlan = plans.firstOrNull { it.status == PlanStatus.ACTIVE }
                activePlan?.let { plan ->
                    item(key = "active_${plan.id}") {
                        AiFitCard(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            onClick = { onPlanClicked(plan.id) },
                        ) {
                            Column(
                                modifier = Modifier.padding(AiFitSpacing.md),
                                verticalArrangement = Arrangement.spacedBy(AiFitSpacing.xs),
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    PlanStatusBadge(status = plan.status.name)
                                }
                                Text(
                                    text = plan.name,
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                                Text(
                                    text = stringResource(R.string.nutrition_hub_plan_summary, plan.durationWeeks, plan.dailyCalories),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
            }

            // ── Filter chips ──
            item(key = "diet_filter_chips") {
                Spacer(modifier = Modifier.height(AiFitSpacing.sm))
                AiFitChipGroup(
                    options = DIET_STATUS_KEYS,
                    selected = setOf(selectedKey),
                    onSelectionChanged = { selection ->
                        val key = selection.firstOrNull() ?: "all"
                        onFilterChanged(dietKeyToStatus(key))
                    },
                    multiSelect = false,
                    displayMapper = { key -> dietKeyDisplayName(key) },
                )
                Spacer(modifier = Modifier.height(AiFitSpacing.xs))
            }

            // ── Plan list (excluding active card already shown) ──
            val activePlanId = plans.firstOrNull { it.status == PlanStatus.ACTIVE }?.id
            val filteredPlans = if (selectedFilter == null) plans else plans.filter { it.status == selectedFilter }
            val listPlans = if (selectedFilter == null || selectedFilter == PlanStatus.ACTIVE) {
                filteredPlans.filter { it.id != activePlanId }
            } else {
                filteredPlans
            }

            items(listPlans, key = { it.id }) { plan ->
                AiFitCard(onClick = { onPlanClicked(plan.id) }) {
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
                            if (plan.status != PlanStatus.ACTIVE) {
                                IconButton(onClick = { planToDelete = plan.id }) {
                                    Icon(
                                        imageVector = Icons.Rounded.DeleteOutline,
                                        contentDescription = stringResource(R.string.nutrition_hub_delete_plan_cd),
                                        tint = MaterialTheme.colorScheme.error,
                                    )
                                }
                            }
                        }
                        Text(
                            text = stringResource(R.string.nutrition_hub_plan_summary, plan.durationWeeks, plan.dailyCalories),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        if (plan.status != PlanStatus.ACTIVE && plan.status != PlanStatus.COMPLETED) {
                            Spacer(modifier = Modifier.height(AiFitSpacing.xs))
                            PrimaryButton(
                                text = stringResource(R.string.nutrition_hub_activate_plan_btn),
                                onClick = { planToActivate = plan.id },
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                }
            }
        }

        // Activation overlay
        if (isActivatingPlan) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.45f)),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(AiFitSpacing.md),
                ) {
                    androidx.compose.material3.CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        text = stringResource(R.string.nutrition_hub_activating_plan),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.inverseOnSurface,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ShoppingTab(
    dietPlans: List<DietPlan>,
    onNavigateToDetail: (listId: String) -> Unit,
) {
    val shoppingViewModel: ShoppingViewModel = hiltViewModel()
    val listState by shoppingViewModel.listState.collectAsStateWithLifecycle()
    var showGenerateSheet by remember { mutableStateOf(false) }
    var isGenerating by remember { mutableStateOf(false) }
    var deleteDialogListId by remember { mutableStateOf<String?>(null) }
    val generateSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(Unit) {
        shoppingViewModel.loadLists()
    }

    LaunchedEffect(Unit) {
        shoppingViewModel.events.collect { event ->
            when (event) {
                is ShoppingUiEvent.ListGenerated -> {
                    isGenerating = false
                    showGenerateSheet = false
                    onNavigateToDetail(event.listId)
                }
                is ShoppingUiEvent.ShowSnackbar -> { /* handled by parent snackbar */ }
                else -> {}
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when (val state = listState) {
            is ShoppingListUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stringResource(R.string.common_loading),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            is ShoppingListUiState.Error -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = AiFitSpacing.xxl),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    EmptyStateView(
                        icon = Icons.Rounded.ShoppingCart,
                        title = stringResource(R.string.nutrition_hub_shopping_error_title),
                        subtitle = state.message,
                    )
                }
            }
            is ShoppingListUiState.Success -> {
                if (state.lists.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = AiFitSpacing.xxl),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        EmptyStateView(
                            icon = Icons.Rounded.ShoppingCart,
                            title = stringResource(R.string.nutrition_hub_shopping_empty_title),
                            subtitle = stringResource(R.string.nutrition_hub_shopping_empty_subtitle),
                        )
                        Spacer(modifier = Modifier.height(AiFitSpacing.md))
                        PrimaryButton(
                            text = stringResource(R.string.nutrition_hub_generate_list),
                            onClick = { showGenerateSheet = true },
                        )
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(
                            start = AiFitSpacing.md,
                            end = AiFitSpacing.md,
                            top = AiFitSpacing.md,
                            bottom = 88.dp,
                        ),
                        verticalArrangement = Arrangement.spacedBy(AiFitSpacing.sm),
                    ) {
                        items(state.lists, key = { it.id }) { shoppingList ->
                            SwipeableListItem(
                                onDelete = { deleteDialogListId = shoppingList.id },
                            ) {
                                AiFitCard(
                                    onClick = { onNavigateToDetail(shoppingList.id) },
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(
                                                start = AiFitSpacing.md,
                                                top = AiFitSpacing.sm,
                                                bottom = AiFitSpacing.sm,
                                                end = 4.dp,
                                            ),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Column {
                                            PlanStatusBadge(
                                                status = stringResource(shoppingList.period.toStringRes()),
                                            )
                                            Spacer(modifier = Modifier.height(AiFitSpacing.xs))
                                            Text(
                                                text = shoppingList.generatedAt.take(10),
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            )
                                        }
                                        IconButton(
                                            onClick = { deleteDialogListId = shoppingList.id },
                                        ) {
                                            Icon(
                                                imageVector = Icons.Rounded.DeleteOutline,
                                                contentDescription = stringResource(R.string.nutrition_hub_delete_list_cd),
                                                tint = MaterialTheme.colorScheme.error,
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // FAB for generate
        FloatingActionButton(
            onClick = { showGenerateSheet = true },
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(AiFitSpacing.md),
        ) {
            Icon(
                imageVector = Icons.Rounded.Add,
                contentDescription = stringResource(R.string.nutrition_hub_generate_list),
            )
        }
    }

    deleteDialogListId?.let { id ->
        ConfirmationDialog(
            title = stringResource(R.string.nutrition_hub_delete_list_title),
            message = stringResource(R.string.nutrition_hub_delete_list_message),
            confirmText = stringResource(R.string.common_delete),
            onConfirm = {
                shoppingViewModel.onDeleteList(id)
                deleteDialogListId = null
            },
            onDismiss = { deleteDialogListId = null },
        )
    }

    if (showGenerateSheet) {
        GenerateShoppingListSheet(
            sheetState = generateSheetState,
            dietPlans = dietPlans,
            isGenerating = isGenerating,
            onDismiss = { showGenerateSheet = false },
            onGenerate = { dietPlanId, period ->
                isGenerating = true
                shoppingViewModel.onGenerateList(dietPlanId, period)
            },
        )
    }
}

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "NutritionHubScreen Today Dark",
)
@Composable
private fun NutritionHubTodayPreview() {
    AIFitTheme(darkTheme = true) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
        ) {
            TodayTab(
                todayState = TodayState(
                    nutritionLog = NutritionLog(
                        id = "1",
                        date = LocalDate.now(),
                        totalCalories = 1450,
                        totalProteinGrams = 95.0,
                        totalCarbsGrams = 180.0,
                        totalFatGrams = 42.0,
                        meals = listOf(
                            MealLog(
                                id = "m1",
                                mealType = MealType.BREAKFAST,
                                name = "Oatmeal with berries",
                                time = "08:30",
                                calories = 380,
                                proteinGrams = 15.0,
                                carbsGrams = 55.0,
                                fatGrams = 10.0,
                                aiGenerated = false,
                                rawInputText = null,
                                items = emptyList(),
                            ),
                            MealLog(
                                id = "m2",
                                mealType = MealType.LUNCH,
                                name = "Grilled chicken salad",
                                time = "13:00",
                                calories = 520,
                                proteinGrams = 45.0,
                                carbsGrams = 30.0,
                                fatGrams = 18.0,
                                aiGenerated = true,
                                rawInputText = "chicken salad with veggies",
                                items = emptyList(),
                            ),
                        ),
                    ),
                    target = NutritionTarget(
                        id = "t1",
                        calorieTarget = 2200,
                        proteinTarget = 165.0,
                        carbsTarget = 250.0,
                        fatTarget = 73.0,
                        effectiveFrom = LocalDate.now(),
                        setBy = TargetSource.MANUAL,
                    ),
                ),
                onRingClicked = {},
                onDeleteMeal = {},
                onAddMeal = {},
            )
        }
    }
}

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "NutritionHubScreen Shopping Dark",
)
@Composable
private fun NutritionHubShoppingPreview() {
    AIFitTheme(darkTheme = true) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
        ) {
            ShoppingTab(
                dietPlans = emptyList(),
                onNavigateToDetail = {},
            )
        }
    }
}

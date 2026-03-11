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
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.ShoppingCart
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jlsh.aifit.core.ui.components.buttons.PrimaryButton
import com.jlsh.aifit.core.ui.components.display.AiFitCard
import com.jlsh.aifit.core.ui.components.display.MacroRingChart
import com.jlsh.aifit.core.ui.components.display.MacroRingData
import com.jlsh.aifit.core.ui.components.display.PlanStatusBadge
import com.jlsh.aifit.core.ui.components.feedback.ConfirmationDialog
import com.jlsh.aifit.core.ui.components.feedback.EmptyStateView
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
import kotlinx.coroutines.launch
import java.time.LocalDate

private val HUB_TABS = listOf("TODAY", "DIET PLAN", "SHOPPING")

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
    var showSheet by remember { mutableStateOf(false) }
    var mealToDeleteId by remember { mutableStateOf<String?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

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

    ScreenScaffold<NutritionHubUiState.Success>(
        uiState = hubState,
        snackbarHostState = snackbarHostState,
        topBar = {
            AiFitTopBar(
                title = "Nutrition",
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
                            0 -> "Add Meal"
                            1 -> "New Plan"
                            else -> "Generate"
                        },
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
                    onPlanClicked = viewModel::onDietPlanClicked,
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
            title = "Eliminar comida",
            message = "Esta acción no se puede deshacer.",
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
                        currentCalories = log.totalCalories.toFloat(),
                        targetCalories = target.calorieTarget.toFloat(),
                        currentProtein = log.totalProteinGrams.toFloat(),
                        targetProtein = target.proteinTarget.toFloat(),
                        currentCarbs = log.totalCarbsGrams.toFloat(),
                        targetCarbs = target.carbsTarget.toFloat(),
                        currentFat = log.totalFatGrams.toFloat(),
                        targetFat = target.fatTarget.toFloat(),
                    ),
                    size = 180.dp,
                )
            }
        }

        item(key = "meals_header") {
            SectionHeader(title = "MEALS")
        }

        if (log.meals.isEmpty()) {
            item(key = "empty") {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = AiFitSpacing.lg),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    EmptyStateView(
                        icon = Icons.Rounded.Restaurant,
                        title = "No has registrado comidas hoy",
                        subtitle = "Añade tu primera comida del día",
                        action = {
                            PrimaryButton(
                                text = "ADD MEAL",
                                onClick = onAddMeal,
                                modifier = Modifier.padding(horizontal = AiFitSpacing.xl),
                            )
                        },
                    )
                }
            }
        } else {
            items(log.meals, key = { it.id }) { meal ->
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
                            text = meal.mealType.name.replace("_", " "),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        )
                    }
                    if (meal.aiGenerated) {
                        Icon(
                            imageVector = Icons.Rounded.AutoAwesome,
                            contentDescription = "AI",
                            tint = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(14.dp),
                        )
                    }
                }
                Spacer(modifier = Modifier.height(AiFitSpacing.xs))
                Text(
                    text = meal.name ?: meal.mealType.name.replace("_", " "),
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
    onPlanClicked: (String) -> Unit,
    onCreatePlan: () -> Unit,
) {
    if (plans.isEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = AiFitSpacing.xxl),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            EmptyStateView(
                icon = Icons.Rounded.Restaurant,
                title = "Sin planes de dieta",
                subtitle = "Genera tu primer plan de dieta con IA",
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
                        PlanStatusBadge(status = plan.status.name)
                        Text(
                            text = plan.name,
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = "${plan.durationWeeks} weeks • ${plan.dailyCalories} kcal/day",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }

        val otherPlans = plans.filter { it.id != activePlan?.id }
        items(otherPlans, key = { it.id }) { plan ->
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
                    }
                    Text(
                        text = "${plan.durationWeeks} weeks • ${plan.dailyCalories} kcal/day",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                        text = "Cargando…",
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
                        title = "Error al cargar listas",
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
                            title = "Genera tu primera lista de compras",
                            subtitle = "Basada en tu plan de dieta activo",
                        )
                        Spacer(modifier = Modifier.height(AiFitSpacing.md))
                        PrimaryButton(
                            text = "Generar lista",
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
                                            .padding(AiFitSpacing.md),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Column {
                                            PlanStatusBadge(
                                                status = shoppingList.period.name.replace("_", " "),
                                            )
                                            Spacer(modifier = Modifier.height(AiFitSpacing.xs))
                                            Text(
                                                text = shoppingList.generatedAt.take(10),
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            )
                                        }
                                        Icon(
                                            imageVector = Icons.Rounded.ShoppingCart,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primaryContainer,
                                        )
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
                contentDescription = "Generar lista",
            )
        }
    }

    deleteDialogListId?.let { id ->
        ConfirmationDialog(
            title = "Eliminar lista",
            message = "¿Seguro que quieres eliminar esta lista de compras?",
            confirmText = "Eliminar",
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
                        setBy = TargetSource.USER,
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


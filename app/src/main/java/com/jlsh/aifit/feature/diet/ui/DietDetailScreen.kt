package com.jlsh.aifit.feature.diet.ui

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jlsh.aifit.R
import com.jlsh.aifit.core.ui.components.display.MacroProgressBar
import com.jlsh.aifit.core.ui.components.display.PlanStatusBadge
import com.jlsh.aifit.core.ui.components.layout.AiFitTopBar
import com.jlsh.aifit.core.ui.components.layout.ExpandableSection
import com.jlsh.aifit.core.ui.components.layout.ScreenScaffold
import com.jlsh.aifit.core.ui.theme.AIFitTheme
import com.jlsh.aifit.core.ui.theme.AiFitSpacing
import com.jlsh.aifit.core.ui.theme.MacroType
import com.jlsh.aifit.feature.education.ui.EducationViewModel
import com.jlsh.aifit.feature.education.ui.components.EducationConfirmSheet
import com.jlsh.aifit.feature.education.ui.components.MealExplanationSheet
import com.jlsh.aifit.feature.education.ui.components.WhyThisMealSheet
import com.jlsh.aifit.feature.diet.domain.model.DietDay
import com.jlsh.aifit.feature.diet.domain.model.DietPlan
import com.jlsh.aifit.feature.diet.domain.model.Meal
import com.jlsh.aifit.feature.diet.domain.model.MealItem
import com.jlsh.aifit.feature.diet.domain.model.MealType
import com.jlsh.aifit.feature.diet.ui.state.DietUiEvent
import com.jlsh.aifit.feature.diet.ui.state.DietUiState
import com.jlsh.aifit.feature.training.domain.model.PlanStatus
import com.jlsh.aifit.feature.user.domain.model.DietPreference
import java.time.LocalDateTime

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

/**
 * Diet plan detail screen: summary of macros, expandable days and actions per meal.
 *
 * Shows loading/error status using [ScreenScaffold] and educational sheets (explain/why this food).
 *
 * @param planId Identifier of the plan to load.
 * @param onNavigateBack Callback when clicking back or after navigation events.
 * @param onNavigateToGenerate Opens the adaptive generation flow from the top bar.
 * @param viewModel Diet ViewModel with [DietUiState] and [DietUiEvent].
 * @param educationViewModel ViewModel for meal explanations.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DietDetailScreen(
    planId: String,
    onNavigateBack: () -> Unit,
    onNavigateToGenerate: (adaptive: Boolean, basePlanId: String?) -> Unit,
    viewModel: DietViewModel = hiltViewModel(),
    educationViewModel: EducationViewModel = hiltViewModel(),
) {
    val detailState by viewModel.detailUiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val explanationState by educationViewModel.explanationState.collectAsStateWithLifecycle()
    val whyThisState by educationViewModel.whyThisState.collectAsStateWithLifecycle()

    var showMealExplanationForId by remember { mutableStateOf<String?>(null) }
    var showMealExplanationConfirmForId by remember { mutableStateOf<String?>(null) }
    var showWhyThisMealForId by remember { mutableStateOf<String?>(null) }
    var showWhyThisMealConfirmForId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(planId) {
        viewModel.loadPlanDetail(planId)
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is DietUiEvent.NavigateBack -> onNavigateBack()
                is DietUiEvent.NavigateToDetail -> { /* already on detail */ }
                is DietUiEvent.NavigateToDietApproval -> { /* not applicable from detail screen */ }
                is DietUiEvent.NavigateToDietGenerate -> { /* not applicable from detail screen */ }
                is DietUiEvent.ShowSnackbar -> snackbarHostState.showSnackbar(event.message)
                is DietUiEvent.PlanApproved -> Unit
            }
        }
    }

    // ── Sheets ──
    if (showMealExplanationConfirmForId != null) {
        EducationConfirmSheet(
            title = stringResource(R.string.diet_detail_explain_meal_title),
            description = stringResource(R.string.diet_detail_explain_meal_description),
            confirmText = stringResource(R.string.diet_detail_explain_meal_confirm),
            onDismiss = { showMealExplanationConfirmForId = null },
            onConfirm = {
                val mealId = showMealExplanationConfirmForId
                showMealExplanationConfirmForId = null
                if (mealId != null) {
                    showMealExplanationForId = mealId
                    educationViewModel.loadMealExplanation(mealId)
                }
            },
        )
    }

    if (showWhyThisMealConfirmForId != null) {
        EducationConfirmSheet(
            title = stringResource(R.string.diet_detail_why_meal_title),
            description = stringResource(R.string.diet_detail_why_meal_description),
            confirmText = stringResource(R.string.diet_detail_why_meal_confirm),
            onDismiss = { showWhyThisMealConfirmForId = null },
            onConfirm = {
                val mealId = showWhyThisMealConfirmForId
                showWhyThisMealConfirmForId = null
                if (mealId != null) {
                    showWhyThisMealForId = mealId
                    educationViewModel.loadWhyThisMeal(mealId)
                }
            },
        )
    }

    if (showMealExplanationForId != null) {
        MealExplanationSheet(
            state = explanationState,
            onDismiss = {
                showMealExplanationForId = null
                educationViewModel.resetExplanationState()
            },
            onRetry = { showMealExplanationForId?.let { educationViewModel.loadMealExplanation(it) } },
        )
    }

    if (showWhyThisMealForId != null) {
        WhyThisMealSheet(
            state = whyThisState,
            onDismiss = {
                showWhyThisMealForId = null
                educationViewModel.resetWhyThisState()
            },
            onRetry = { showWhyThisMealForId?.let { educationViewModel.loadWhyThisMeal(it) } },
        )
    }

    val topBarTitle = when (val state = detailState) {
        is DietUiState.Success -> state.plan.name
        else -> stringResource(R.string.diet_detail_topbar_fallback)
    }

    ScreenScaffold<DietUiState.Success>(
        uiState = detailState,
        snackbarHostState = snackbarHostState,
        topBar = {
            AiFitTopBar(
                title = topBarTitle,
                onBack = onNavigateBack,
                actions = {
                    IconButton(onClick = { onNavigateToGenerate(true, planId) }) {
                        Icon(
                            imageVector = PhosphorIcons.Regular.Sparkle,
                            contentDescription = stringResource(R.string.diet_detail_adaptive_plan_cd),
                            tint = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(24.dp),
                        )
                    }
                },
            )
        },
        onRetry = { viewModel.loadPlanDetail(planId) },
    ) { paddingValues, successState ->
        DietDetailContent(
            plan = successState.plan,
            onMealInfoClick = { mealId ->
                showMealExplanationConfirmForId = mealId
            },
            onMealWhyClick = { mealId ->
                showWhyThisMealConfirmForId = mealId
            },
            modifier = Modifier.padding(paddingValues),
        )
    }
}

@Composable
private fun DietDetailContent(
    plan: DietPlan,
    onMealInfoClick: (mealId: String) -> Unit = {},
    onMealWhyClick: (mealId: String) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 88.dp),
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

                // Dominant title
                Text(
                    text = plan.name,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                // Metadata subordinada
                Text(
                    text = buildString {
                        append("${plan.durationWeeks} semanas")
                        append("  ·  ${plan.preference.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }}")
                        append("  ·  ${plan.totalDays} días")
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

                // Stats row — calories + macros as highlighted numbers
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(0.dp),
                ) {
                    StatCell(
                        value = "${plan.dailyCalories}",
                        label = stringResource(R.string.diet_detail_kcal_label),
                        modifier = Modifier.weight(1f),
                    )
                    StatDivider()
                    StatCell(
                        value = "${plan.proteinGrams}g",
                        label = stringResource(R.string.diet_detail_protein_label),
                        modifier = Modifier.weight(1f),
                    )
                    StatDivider()
                    StatCell(
                        value = "${plan.carbsGrams}g",
                        label = stringResource(R.string.diet_detail_carbs_label),
                        modifier = Modifier.weight(1f),
                    )
                    StatDivider()
                    StatCell(
                        value = "${plan.fatGrams}g",
                        label = stringResource(R.string.diet_detail_fat_label),
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }

        // Divider + macro section
        item(key = "divider_macros") {
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant,
                thickness = 0.5.dp,
            )
            Spacer(Modifier.height(AiFitSpacing.md))
        }

        item(key = "macros_header") {
            Text(
                text = stringResource(R.string.diet_detail_macros_header),
                style = MaterialTheme.typography.labelSmall,
                letterSpacing = 1.5.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AiFitSpacing.md)
                    .padding(bottom = AiFitSpacing.sm),
            )
        }

        item(key = "macros") {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AiFitSpacing.md)
                    .padding(bottom = AiFitSpacing.lg),
                verticalArrangement = Arrangement.spacedBy(AiFitSpacing.sm),
            ) {
                MacroProgressBar(
                    name = stringResource(R.string.diet_detail_macro_protein),
                    current = plan.proteinGrams.toFloat(),
                    target = plan.proteinGrams.toFloat(),
                    macro = MacroType.Protein,
                )
                MacroProgressBar(
                    name = stringResource(R.string.diet_detail_macro_carbs),
                    current = plan.carbsGrams.toFloat(),
                    target = plan.carbsGrams.toFloat(),
                    macro = MacroType.Carbs,
                )
                MacroProgressBar(
                    name = stringResource(R.string.diet_detail_macro_fat),
                    current = plan.fatGrams.toFloat(),
                    target = plan.fatGrams.toFloat(),
                    macro = MacroType.Fat,
                )
            }
        }

        // Divider + days section
        item(key = "divider_days") {
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant,
                thickness = 0.5.dp,
            )
            Spacer(Modifier.height(AiFitSpacing.md))
        }

        item(key = "days_header") {
            Text(
                text = stringResource(R.string.diet_detail_weekly_plan_header),
                style = MaterialTheme.typography.labelSmall,
                letterSpacing = 1.5.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AiFitSpacing.md)
                    .padding(bottom = AiFitSpacing.sm),
            )
        }

        // ── Expandable days ──────────────────── ─────────────────────
        items(plan.days, key = { it.id }) { day ->
            Column(
                modifier = Modifier.padding(horizontal = AiFitSpacing.md),
            ) {
                ExpandableSection(
                    title = stringResource(R.string.diet_detail_day_section_title, day.dayNumber, day.name),
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
                        // Calories of the day in lime
                        Text(
                            text = "${day.totalCalories} kcal",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.padding(bottom = AiFitSpacing.sm),
                        )
                        day.meals.forEachIndexed { index, meal ->
                            MealRow(
                                meal = meal,
                                onInfoClick = { onMealInfoClick(meal.id) },
                                onWhyClick = { onMealWhyClick(meal.id) },
                            )
                            if (index < day.meals.lastIndex) {
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
            color = MaterialTheme.colorScheme.primaryContainer,
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
            .height(32.dp),
        contentAlignment = Alignment.Center,
    ) {
        HorizontalDivider(
            modifier = Modifier.size(width = 0.5.dp, height = 32.dp),
            color = MaterialTheme.colorScheme.outlineVariant,
            thickness = 0.5.dp,
        )
    }
}

@Composable
private fun MealRow(
    meal: Meal,
    onInfoClick: () -> Unit = {},
    onWhyClick: () -> Unit = {},
) {
    val mealTypeLabel = mealTypeDisplay(meal.mealType)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = AiFitSpacing.xs),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = meal.name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(AiFitSpacing.sm),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // Food type badge
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        shape = RoundedCornerShape(6.dp),
                    ) {
                        Text(
                            text = mealTypeLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        )
                    }
                    // Calories in lime + discrete time
                    Text(
                        text = "${meal.calories} kcal",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primaryContainer,
                    )
                    Text(
                        text = meal.time,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Row {
                IconButton(
                    onClick = onInfoClick,
                    modifier = Modifier.size(32.dp),
                ) {
                    Icon(
                        imageVector = PhosphorIcons.Regular.Info,
                        contentDescription = stringResource(R.string.diet_detail_info_cd),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp),
                    )
                }
                IconButton(
                    onClick = onWhyClick,
                    modifier = Modifier.size(32.dp),
                ) {
                    Icon(
                        imageVector = PhosphorIcons.Regular.Question,
                        contentDescription = stringResource(R.string.diet_detail_why_cd),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        }

        // Food items — indented and discreet
        if (meal.items.isNotEmpty()) {
            Column(
                modifier = Modifier.padding(start = AiFitSpacing.sm),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                meal.items.forEach { item ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = "· ${item.name}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f),
                        )
                        Text(
                            text = "${item.quantity} ${item.unit}  ·  ${item.calories} kcal",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "DietDetailScreen Dark",
)
@Composable
private fun DietDetailScreenPreview() {
    AIFitTheme(darkTheme = true) {
        val fakePlan = DietPlan(
            id = "1",
            name = "Dieta Mediterránea",
            description = "Plan equilibrado basado en la dieta mediterránea para mejorar la composición corporal.",
            dailyCalories = 2200,
            proteinGrams = 130,
            carbsGrams = 250,
            fatGrams = 80,
            durationWeeks = 4,
            preference = DietPreference.MEDITERRANEAN,
            status = PlanStatus.ACTIVE,
            totalDays = 28,
            createdAt = LocalDateTime.now(),
            days = listOf(
                DietDay(
                    id = "d1",
                    dayNumber = 1,
                    name = "Lunes",
                    totalCalories = 2200,
                    meals = listOf(
                        Meal(
                            id = "m1",
                            mealType = MealType.BREAKFAST,
                            name = "Tostadas de aguacate con huevos",
                            time = "08:00",
                            calories = 450,
                            proteinGrams = 22,
                            carbsGrams = 35,
                            fatGrams = 25,
                            items = listOf(
                                MealItem(
                                    id = "i1", name = "Pan integral",
                                    quantity = 2f, unit = "rebanadas",
                                    calories = 160, proteinGrams = 6f,
                                    carbsGrams = 28f, fatGrams = 2f,
                                ),
                                MealItem(
                                    id = "i2", name = "Aguacate",
                                    quantity = 0.5f, unit = "unidad",
                                    calories = 120, proteinGrams = 1.5f,
                                    carbsGrams = 6f, fatGrams = 11f,
                                ),
                            ),
                        ),
                        Meal(
                            id = "m2",
                            mealType = MealType.LUNCH,
                            name = "Ensalada de pollo a la plancha",
                            time = "13:00",
                            calories = 550,
                            proteinGrams = 40,
                            carbsGrams = 30,
                            fatGrams = 25,
                            items = emptyList(),
                        ),
                        Meal(
                            id = "m3",
                            mealType = MealType.DINNER,
                            name = "Salmón con verduras al horno",
                            time = "20:00",
                            calories = 600,
                            proteinGrams = 42,
                            carbsGrams = 28,
                            fatGrams = 30,
                            items = emptyList(),
                        ),
                    ),
                ),
                DietDay(
                    id = "d2",
                    dayNumber = 2,
                    name = "Martes",
                    totalCalories = 2150,
                    meals = listOf(
                        Meal(
                            id = "m4",
                            mealType = MealType.BREAKFAST,
                            name = "Yogur griego con frutos secos",
                            time = "08:00",
                            calories = 380,
                            proteinGrams = 18,
                            carbsGrams = 25,
                            fatGrams = 20,
                            items = emptyList(),
                        ),
                    ),
                ),
            ),
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
        ) {
            DietDetailContent(plan = fakePlan)
        }
    }
}
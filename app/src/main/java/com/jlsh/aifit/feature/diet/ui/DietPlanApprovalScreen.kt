package com.jlsh.aifit.feature.diet.ui

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
import com.jlsh.aifit.core.ui.components.layout.ScreenScaffold
import com.jlsh.aifit.core.ui.theme.AiFitSpacing
import com.jlsh.aifit.feature.diet.domain.model.DietDay
import com.jlsh.aifit.feature.diet.domain.model.Meal
import com.jlsh.aifit.feature.diet.domain.model.MealItem
import com.jlsh.aifit.feature.diet.ui.state.DietUiEvent
import com.jlsh.aifit.feature.diet.ui.state.DietUiState
import com.jlsh.aifit.feature.user.ui.displayName
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DietPlanApprovalScreen(
    planId: String,
    onAccept: () -> Unit,
    onNavigateToApproval: (String) -> Unit,
    viewModel: DietViewModel = hiltViewModel(),
) {
    val detailUiState by viewModel.detailUiState.collectAsStateWithLifecycle()
    var showFeedbackSheet by remember { mutableStateOf(false) }
    var feedbackText by remember { mutableStateOf("") }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    LaunchedEffect(planId) { viewModel.loadPlanDetail(planId) }

    // Collect navigation events for regeneration
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is DietUiEvent.NavigateToDietApproval -> onNavigateToApproval(event.planId)
                else -> Unit
            }
        }
    }

    // Handle Regenerating state — fullscreen loading (mirrors onboarding pattern)
    if (detailUiState is DietUiState.Regenerating) {
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
                    text = stringResource(R.string.onboarding_regenerating_nutrition),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        return
    }

    ScreenScaffold<DietUiState.Success>(
        uiState = detailUiState,
        topBar = { AiFitTopBar(title = "Tu Nuevo Plan") },
        onRetry = { viewModel.loadPlanDetail(planId) },
    ) { paddingValues, state ->
        val plan = state.plan
        val sortedDays = plan.days.sortedBy { it.dayNumber }

        Box(modifier = Modifier.fillMaxSize()) {
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
                            text = plan.name,
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f),
                        )
                        PlanStatusBadge(status = "ACTIVE")
                    }
                }

                // Macros summary card
                item {
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        ),
                        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 0.dp),
                    ) {
                        Column(
                            modifier = Modifier.padding(AiFitSpacing.md),
                            verticalArrangement = Arrangement.spacedBy(AiFitSpacing.xs),
                        ) {
                            Text(
                                text = "${plan.dailyCalories} kcal / día",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Text(
                                text = "P: ${plan.proteinGrams}g · C: ${plan.carbsGrams}g · G: ${plan.fatGrams}g",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Text(
                                text = "${plan.durationWeeks} semanas · ${plan.preference.displayName()}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }

                // Description if present
                plan.description?.let { desc ->
                    item {
                        Text(
                            text = desc,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                // Weekly plan label
                item {
                    Text(
                        text = "PLAN SEMANAL",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 1.5.sp,
                        modifier = Modifier.padding(top = AiFitSpacing.sm),
                    )
                }

                // Days
                items(sortedDays) { day ->
                    DietApprovalDaySection(day = day)
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
                        onClick = { showFeedbackSheet = true },
                        modifier = Modifier.weight(1f),
                    )
                    PrimaryButton(
                        text = "Aceptar Plan",
                        onClick = {
                            viewModel.onApproveDietPlan()
                            onAccept()
                        },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }

    // Feedback ModalBottomSheet (mirrors OnboardingNutritionApprovalScreen)
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
                    text = stringResource(R.string.onboarding_adjust_nutrition),
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
                            viewModel.onRegenerateApprovalDietPlan(planId, fb)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun DietApprovalDaySection(day: DietDay) {
    val dayName = DAY_NAMES_ES[day.dayNumber] ?: day.name
    ExpandableSection(
        title = "$dayName · ${day.totalCalories} kcal",
    ) {
        Column(
            modifier = Modifier.padding(vertical = AiFitSpacing.sm),
            verticalArrangement = Arrangement.spacedBy(AiFitSpacing.sm),
        ) {
            day.meals.forEach { meal ->
                DietApprovalMealCard(meal = meal)
            }
        }
    }
}

@Composable
private fun DietApprovalMealCard(meal: Meal) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier.padding(AiFitSpacing.md),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = meal.mealType.displayName(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = meal.name,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = meal.time,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = "${meal.calories} kcal · P: ${meal.proteinGrams}g · C: ${meal.carbsGrams}g · G: ${meal.fatGrams}g",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
            if (meal.items.isNotEmpty()) {
                Spacer(Modifier.height(AiFitSpacing.xs))
                meal.items.forEach { item ->
                    DietApprovalMealItemRow(item = item)
                }
            }
        }
    }
}

@Composable
private fun DietApprovalMealItemRow(item: MealItem) {
    Text(
        text = "- ${item.name} — ${
            if (item.quantity == item.quantity.toLong().toFloat()) {
                item.quantity.toLong().toString()
            } else {
                item.quantity.toString()
            }
        } ${item.unit}",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

private val DAY_NAMES_ES = mapOf(
    1 to "Lunes",
    2 to "Martes",
    3 to "Miércoles",
    4 to "Jueves",
    5 to "Viernes",
    6 to "Sábado",
    7 to "Domingo",
)

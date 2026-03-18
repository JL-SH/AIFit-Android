package com.jlsh.aifit.feature.user.ui

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
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
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
import com.jlsh.aifit.core.ui.components.inputs.AiFitTextField
import com.jlsh.aifit.core.ui.theme.AiFitSpacing
import com.jlsh.aifit.feature.diet.domain.model.DietDay
import com.jlsh.aifit.feature.diet.domain.model.Meal
import com.jlsh.aifit.feature.diet.domain.model.MealItem
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingNutritionApprovalScreen(
    onApprove: () -> Unit,
    onRegenerate: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showFeedbackSheet by remember { mutableStateOf(false) }
    var feedbackText by remember { mutableStateOf("") }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    when (state) {
        is OnboardingState.RegeneratingDiet -> {
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

        !is OnboardingState.Ready -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primaryContainer,
                )
            }
            return
        }

        else -> Unit
    }

    val result = (state as OnboardingState.Ready).result
    val dietPlan = result.dietPlan
    val nutritionTarget = result.nutritionTarget

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        LazyColumn(
            contentPadding = PaddingValues(
                start = AiFitSpacing.md,
                end = AiFitSpacing.md,
                top = AiFitSpacing.xxl,
                bottom = AiFitSpacing.xxl,
            ),
            verticalArrangement = Arrangement.spacedBy(AiFitSpacing.md),
        ) {
            // Cabecera
            item {
                Text(
                    text = stringResource(R.string.onboarding_review_nutrition),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            item {
                Text(
                    text = dietPlan.name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            // Tarjeta resumen de macros
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
                            text = stringResource(
                                R.string.onboarding_kcal_per_day,
                                nutritionTarget.calorieTarget,
                            ),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = stringResource(
                                R.string.onboarding_macros_summary,
                                nutritionTarget.proteinTarget.toInt(),
                                nutritionTarget.carbsTarget.toInt(),
                                nutritionTarget.fatTarget.toInt(),
                            ),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = stringResource(
                                R.string.onboarding_duration_preference,
                                dietPlan.durationWeeks,
                                dietPlan.preference.displayName(),
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            // Sección plan semanal
            item {
                Text(
                    text = stringResource(R.string.onboarding_weekly_plan),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 1.5.sp,
                    modifier = Modifier.padding(top = AiFitSpacing.sm),
                )
            }

            items(dietPlan.days.size) { index ->
                DietDayCard(day = dietPlan.days[index])
            }

            // Botones
            item { Spacer(Modifier.height(AiFitSpacing.sm)) }
            item {
                PrimaryButton(
                    text = stringResource(R.string.onboarding_start_nutrition_plan),
                    onClick = {
                        viewModel.confirmOnboarding()
                        onApprove()
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            item {
                SecondaryButton(
                    text = stringResource(R.string.onboarding_adjust_something),
                    onClick = { showFeedbackSheet = true },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }

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
                            viewModel.regenerateDiet(fb)
                            onRegenerate()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun DietDayCard(day: DietDay) {
    var expanded by rememberSaveable { mutableStateOf(false) }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 0.dp),
    ) {
        Column(modifier = Modifier.padding(AiFitSpacing.md)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = day.name,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = stringResource(R.string.onboarding_kcal_short, day.totalCalories),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primaryContainer,
                )
            }
            Spacer(Modifier.height(AiFitSpacing.sm))
            SecondaryButton(
                text = if (expanded) {
                    stringResource(R.string.onboarding_hide_meals)
                } else {
                    stringResource(R.string.onboarding_show_meals)
                },
                onClick = { expanded = !expanded },
                modifier = Modifier.fillMaxWidth(),
            )
            if (expanded) {
                Spacer(Modifier.height(AiFitSpacing.sm))
                day.meals.forEach { meal ->
                    MealCard(meal = meal)
                    Spacer(Modifier.height(AiFitSpacing.sm))
                }
            }
        }
    }
}

@Composable
private fun MealCard(meal: Meal) {
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
                text = stringResource(
                    R.string.onboarding_meal_macros,
                    meal.calories,
                    meal.proteinGrams,
                    meal.carbsGrams,
                    meal.fatGrams,
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
            if (meal.items.isNotEmpty()) {
                Spacer(Modifier.height(AiFitSpacing.xs))
                meal.items.forEach { item ->
                    MealItemRow(item = item)
                }
            }
        }
    }
}

@Composable
private fun MealItemRow(item: MealItem) {
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

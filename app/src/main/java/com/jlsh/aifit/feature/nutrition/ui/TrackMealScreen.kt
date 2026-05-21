package com.jlsh.aifit.feature.nutrition.ui

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jlsh.aifit.R
import com.jlsh.aifit.core.ui.components.buttons.AiGenerateButton
import com.jlsh.aifit.core.ui.components.buttons.PrimaryButton
import com.jlsh.aifit.core.ui.components.inputs.AiFitDropdown
import com.jlsh.aifit.core.ui.components.inputs.AiFitNumberField
import com.jlsh.aifit.core.ui.components.inputs.AiFitTextField
import com.jlsh.aifit.core.ui.components.layout.AiFitTopBar
import com.jlsh.aifit.core.ui.theme.AIFitTheme
import com.jlsh.aifit.core.ui.theme.AiFitSpacing
import com.jlsh.aifit.feature.diet.domain.model.MealType
import com.jlsh.aifit.feature.nutrition.data.dto.AnalyzeMealFromTextRequestDto
import com.jlsh.aifit.feature.nutrition.data.dto.TrackFoodItemRequestDto
import com.jlsh.aifit.feature.nutrition.data.dto.TrackMealRequestDto
import com.jlsh.aifit.feature.nutrition.domain.util.scaleFoodItemMacros
import com.jlsh.aifit.feature.nutrition.domain.util.usesPer100gScaling
import com.jlsh.aifit.feature.nutrition.ui.state.NutritionUiEvent
import com.jlsh.aifit.feature.nutrition.ui.state.TrackMealUiState
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

private val MEAL_TYPE_OPTIONS = listOf(
    MealType.BREAKFAST.name,
    MealType.MID_MORNING.name,
    MealType.LUNCH.name,
    MealType.AFTERNOON_SNACK.name,
    MealType.DINNER.name,
    MealType.PRE_WORKOUT.name,
    MealType.POST_WORKOUT.name,
)

private val UNIT_OPTIONS = listOf("g", "ml", "unit", "slice", "cup", "tbsp", "tsp")

data class FoodItemEntry(
    val name: String = "",
    val quantity: String = "",
    val unit: String = "g",
    val calories: String = "",
    val protein: String = "",
    val carbs: String = "",
    val fat: String = "",
)

private fun FoodItemEntry.scaledMacros() = scaleFoodItemMacros(
    unit = unit,
    quantity = quantity.toDoubleOrNull(),
    caloriesPer100g = calories.toIntOrNull(),
    proteinPer100g = protein.toDoubleOrNull(),
    carbsPer100g = carbs.toDoubleOrNull(),
    fatPer100g = fat.toDoubleOrNull(),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackMealScreen(
    mode: String,
    onNavigateBack: () -> Unit,
    onNavigateToHome: () -> Unit = {},
    viewModel: NutritionViewModel = hiltViewModel(),
) {
    val trackState by viewModel.trackMealState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    var selectedMealType by remember { mutableStateOf(MEAL_TYPE_OPTIONS.first()) }
    var mealName by remember { mutableStateOf("") }
    var mealTime by remember {
        mutableStateOf(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")))
    }

    // TimePicker state (BUG-019)
    var showTimePicker by remember { mutableStateOf(false) }
    val timePickerState = rememberTimePickerState(
        initialHour = LocalTime.now().hour,
        initialMinute = LocalTime.now().minute,
        is24Hour = true,
    )

    // Manual mode
    val items = remember { mutableStateListOf(FoodItemEntry()) }

    // Text analysis mode
    var analysisText by remember { mutableStateOf("") }

    val isTextMode = mode == "text_analysis"

    // Build meal type display map using string resources
    val mealTypeDisplayMap = mapOf(
        MealType.BREAKFAST.name to stringResource(R.string.meal_type_breakfast),
        MealType.MID_MORNING.name to stringResource(R.string.meal_type_morning_snack),
        MealType.LUNCH.name to stringResource(R.string.meal_type_lunch),
        MealType.AFTERNOON_SNACK.name to stringResource(R.string.meal_type_snack),
        MealType.DINNER.name to stringResource(R.string.meal_type_dinner),
        MealType.PRE_WORKOUT.name to stringResource(R.string.meal_type_pre_workout),
        MealType.POST_WORKOUT.name to stringResource(R.string.meal_type_post_workout),
    )

    LaunchedEffect(Unit) {
        viewModel.resetTrackMealState()
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is NutritionUiEvent.NavigateToHome -> onNavigateToHome()
                is NutritionUiEvent.NavigateBack -> onNavigateBack()
                is NutritionUiEvent.ShowSnackbar -> snackbarHostState.showSnackbar(event.message)
                else -> {}
            }
        }
    }

    // TimePicker dialog (BUG-019)
    if (showTimePicker) {
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    mealTime = String.format("%02d:%02d", timePickerState.hour, timePickerState.minute)
                    showTimePicker = false
                }) {
                    Text(stringResource(R.string.common_accept))
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text(stringResource(R.string.common_cancel))
                }
            },
            text = {
                TimePicker(state = timePickerState)
            },
        )
    }

    val isSaving = trackState is TrackMealUiState.Saving
    val isAnalyzing = trackState is TrackMealUiState.Analyzing

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                AiFitTopBar(
                    title = if (isTextMode) stringResource(R.string.nutrition_track_title_analyze) else stringResource(R.string.nutrition_track_title_manual),
                    onBack = onNavigateBack,
                    background = MaterialTheme.colorScheme.background,
                )
            },
            snackbarHost = { SnackbarHost(snackbarHostState) },
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = AiFitSpacing.md)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(AiFitSpacing.md),
            ) {
                Spacer(modifier = Modifier.height(AiFitSpacing.sm))

                // BUG-018: Dropdown en lugar de ChipGroup
                AiFitDropdown(
                    selectedValue = selectedMealType,
                    options = MEAL_TYPE_OPTIONS,
                    onOptionSelected = { selectedMealType = it },
                    label = stringResource(R.string.nutrition_track_meal_type_label),
                    modifier = Modifier.fillMaxWidth(),
                    displayMapper = { mealTypeDisplayMap[it] ?: it },
                )

                // BUG-019: TimePicker en lugar de TextField
                Box(modifier = Modifier.fillMaxWidth()) {
                    AiFitTextField(
                        value = mealTime,
                        onValueChange = {},
                        label = stringResource(R.string.nutrition_track_time_label),
                        readOnly = true,
                        trailingIcon = Icons.Rounded.Schedule,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    // Overlay transparente para capturar clicks
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable { showTimePicker = true },
                    )
                }

                if (isTextMode) {
                    // Text analysis mode
                    AiFitTextField(
                        value = analysisText,
                        onValueChange = {
                            if (it.length <= 500) analysisText = it
                        },
                        label = stringResource(R.string.nutrition_track_describe_food),
                        singleLine = false,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp),
                    )

                    Text(
                        text = "${analysisText.length}/500",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.align(Alignment.End),
                    )

                    Spacer(modifier = Modifier.height(AiFitSpacing.sm))

                    AiGenerateButton(
                        text = stringResource(R.string.nutrition_track_analyze_btn),
                        loadingText = stringResource(R.string.nutrition_track_analyzing),
                        isLoading = isAnalyzing,
                        onClick = {
                            viewModel.onAnalyzeMealFromText(
                                AnalyzeMealFromTextRequestDto(
                                    date = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE),
                                    mealType = selectedMealType,
                                    time = mealTime,
                                    text = analysisText,
                                ),
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                    )
                } else {
                    // Manual mode
                    AiFitTextField(
                        value = mealName,
                        onValueChange = { mealName = it },
                        label = stringResource(R.string.nutrition_track_meal_name_label),
                        modifier = Modifier.fillMaxWidth(),
                    )

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant,
                        thickness = 0.5.dp,
                    )

                    Text(
                        text = stringResource(R.string.nutrition_track_food_items_header),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 1.5.sp,
                    )

                    items.forEachIndexed { index, item ->
                        FoodItemForm(
                            item = item,
                            onItemChanged = { items[index] = it },
                            index = index + 1,
                        )
                        if (index < items.lastIndex) {
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.outlineVariant,
                                thickness = 0.5.dp,
                            )
                        }
                    }

                    TextButton(
                        onClick = { items.add(FoodItemEntry()) },
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = stringResource(R.string.nutrition_track_add_food_item),
                            tint = MaterialTheme.colorScheme.primaryContainer,
                        )
                        Text(
                            text = stringResource(R.string.nutrition_track_add_food_item),
                            color = MaterialTheme.colorScheme.primaryContainer,
                        )
                    }

                    // Totals (scaled when unit is g/ml — values entered are per 100g)
                    val totalCal = items.sumOf { it.scaledMacros().calories ?: 0 }
                    val totalProt = items.sumOf { it.scaledMacros().proteinGrams ?: 0.0 }
                    val totalCarbs = items.sumOf { it.scaledMacros().carbsGrams ?: 0.0 }
                    val totalFat = items.sumOf { it.scaledMacros().fatGrams ?: 0.0 }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = stringResource(R.string.nutrition_track_total_calories, totalCal),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primaryContainer,
                        )
                        Text(
                            text = stringResource(R.string.nutrition_track_macros_summary, totalProt.toInt(), totalCarbs.toInt(), totalFat.toInt()),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    Spacer(modifier = Modifier.height(AiFitSpacing.sm))

                    PrimaryButton(
                        text = stringResource(R.string.common_save),
                        isLoading = isSaving,
                        onClick = {
                            val foodItems = items.map { entry ->
                                val quantity = entry.quantity.toDoubleOrNull()
                                val per100g = usesPer100gScaling(entry.unit)
                                TrackFoodItemRequestDto(
                                    name = entry.name,
                                    quantity = quantity,
                                    unit = entry.unit,
                                    calories = entry.calories.toIntOrNull(),
                                    proteinGrams = entry.protein.toDoubleOrNull(),
                                    carbsGrams = entry.carbs.toDoubleOrNull(),
                                    fatGrams = entry.fat.toDoubleOrNull(),
                                    macrosPer100g = per100g,
                                )
                            }
                            viewModel.onTrackMeal(
                                TrackMealRequestDto(
                                    date = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE),
                                    mealType = selectedMealType,
                                    name = mealName.ifBlank { null },
                                    time = mealTime,
                                    items = foodItems,
                                ),
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                Spacer(modifier = Modifier.height(AiFitSpacing.lg))
            }
        }
    }
}

@Composable
private fun FoodItemForm(
    item: FoodItemEntry,
    onItemChanged: (FoodItemEntry) -> Unit,
    index: Int,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(AiFitSpacing.sm),
    ) {
        Text(
            text = stringResource(R.string.nutrition_track_food_item_number, index),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        AiFitTextField(
            value = item.name,
            onValueChange = { onItemChanged(item.copy(name = it)) },
            label = stringResource(R.string.nutrition_track_food_name_label),
            modifier = Modifier.fillMaxWidth(),
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(AiFitSpacing.sm),
        ) {
            AiFitNumberField(
                value = item.quantity,
                onValueChange = { onItemChanged(item.copy(quantity = it)) },
                label = stringResource(R.string.nutrition_track_quantity_label),
                modifier = Modifier.weight(1f),
            )
            AiFitDropdown(
                selectedValue = item.unit,
                options = UNIT_OPTIONS,
                onOptionSelected = { onItemChanged(item.copy(unit = it)) },
                label = stringResource(R.string.nutrition_track_unit_label),
                modifier = Modifier.weight(1f),
            )
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(AiFitSpacing.sm),
        ) {
            AiFitNumberField(
                value = item.calories,
                onValueChange = { onItemChanged(item.copy(calories = it)) },
                label = if (usesPer100gScaling(item.unit)) {
                    stringResource(R.string.nutrition_track_kcal_per_100g_label)
                } else {
                    stringResource(R.string.nutrition_track_kcal_label)
                },
                modifier = Modifier.weight(1f),
            )
            AiFitNumberField(
                value = item.protein,
                onValueChange = { onItemChanged(item.copy(protein = it)) },
                label = if (usesPer100gScaling(item.unit)) {
                    stringResource(R.string.nutrition_track_protein_per_100g_label)
                } else {
                    stringResource(R.string.nutrition_track_protein_label)
                },
                modifier = Modifier.weight(1f),
            )
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(AiFitSpacing.sm),
        ) {
            AiFitNumberField(
                value = item.carbs,
                onValueChange = { onItemChanged(item.copy(carbs = it)) },
                label = if (usesPer100gScaling(item.unit)) {
                    stringResource(R.string.nutrition_track_carbs_per_100g_label)
                } else {
                    stringResource(R.string.nutrition_track_carbs_label)
                },
                modifier = Modifier.weight(1f),
            )
            AiFitNumberField(
                value = item.fat,
                onValueChange = { onItemChanged(item.copy(fat = it)) },
                label = if (usesPer100gScaling(item.unit)) {
                    stringResource(R.string.nutrition_track_fat_per_100g_label)
                } else {
                    stringResource(R.string.nutrition_track_fat_label)
                },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "TrackMealScreen Dark",
)
@Composable
private fun TrackMealScreenPreview() {
    AIFitTheme(darkTheme = true) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(AiFitSpacing.md),
            ) {
                Text(
                    text = "Track Meal Preview",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}

package com.jlsh.aifit.feature.nutrition.ui

import android.content.res.Configuration
import androidx.compose.foundation.background
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jlsh.aifit.core.ui.components.buttons.AiGenerateButton
import com.jlsh.aifit.core.ui.components.buttons.PrimaryButton
import com.jlsh.aifit.core.ui.components.inputs.AiFitChipGroup
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
import com.jlsh.aifit.feature.nutrition.ui.state.NutritionUiEvent
import com.jlsh.aifit.feature.nutrition.ui.state.TrackMealUiState
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

private val MEAL_TYPE_OPTIONS = MealType.entries
    .filter { it != MealType.UNKNOWN }
    .map { it.name }

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

@Composable
fun TrackMealScreen(
    mode: String,
    onNavigateBack: () -> Unit,
    viewModel: NutritionViewModel = hiltViewModel(),
) {
    val trackState by viewModel.trackMealState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    var selectedMealType by remember { mutableStateOf(setOf(MEAL_TYPE_OPTIONS.first())) }
    var mealName by remember { mutableStateOf("") }
    var mealTime by remember {
        mutableStateOf(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")))
    }

    // Manual mode
    val items = remember { mutableStateListOf(FoodItemEntry()) }

    // Text analysis mode
    var analysisText by remember { mutableStateOf("") }

    val isTextMode = mode == "text_analysis"

    LaunchedEffect(Unit) {
        viewModel.resetTrackMealState()
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is NutritionUiEvent.NavigateBack -> onNavigateBack()
                is NutritionUiEvent.ShowSnackbar -> snackbarHostState.showSnackbar(event.message)
                else -> {}
            }
        }
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
                    title = if (isTextMode) "Analyze Meal" else "Track Meal",
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

                AiFitChipGroup(
                    options = MEAL_TYPE_OPTIONS,
                    selected = selectedMealType,
                    onSelectionChanged = { selectedMealType = it },
                    multiSelect = false,
                    modifier = Modifier.fillMaxWidth(),
                    displayMapper = { it.replace("_", " ") },
                )

                AiFitTextField(
                    value = mealTime,
                    onValueChange = { mealTime = it },
                    label = "Time (HH:mm)",
                    modifier = Modifier.fillMaxWidth(),
                )

                if (isTextMode) {
                    // Text analysis mode
                    AiFitTextField(
                        value = analysisText,
                        onValueChange = {
                            if (it.length <= 500) analysisText = it
                        },
                        label = "Describe your meal",
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
                        text = "ANALYZE",
                        loadingText = "Analyzing meal...",
                        isLoading = isAnalyzing,
                        onClick = {
                            viewModel.onAnalyzeMealFromText(
                                AnalyzeMealFromTextRequestDto(
                                    date = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE),
                                    mealType = selectedMealType.first(),
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
                        label = "Meal name (optional)",
                        modifier = Modifier.fillMaxWidth(),
                    )

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant,
                        thickness = 0.5.dp,
                    )

                    Text(
                        text = "ITEMS",
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
                            contentDescription = "Add item",
                            tint = MaterialTheme.colorScheme.primaryContainer,
                        )
                        Text(
                            text = "Add item",
                            color = MaterialTheme.colorScheme.primaryContainer,
                        )
                    }

                    // Totals
                    val totalCal = items.sumOf { it.calories.toIntOrNull() ?: 0 }
                    val totalProt = items.sumOf { it.protein.toDoubleOrNull() ?: 0.0 }
                    val totalCarbs = items.sumOf { it.carbs.toDoubleOrNull() ?: 0.0 }
                    val totalFat = items.sumOf { it.fat.toDoubleOrNull() ?: 0.0 }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = "Total: ${totalCal} kcal",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primaryContainer,
                        )
                        Text(
                            text = "P:${totalProt.toInt()}g C:${totalCarbs.toInt()}g F:${totalFat.toInt()}g",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    Spacer(modifier = Modifier.height(AiFitSpacing.sm))

                    PrimaryButton(
                        text = "SAVE",
                        isLoading = isSaving,
                        onClick = {
                            val foodItems = items.map { entry ->
                                TrackFoodItemRequestDto(
                                    name = entry.name,
                                    quantity = entry.quantity.toDoubleOrNull(),
                                    unit = entry.unit,
                                    calories = entry.calories.toIntOrNull(),
                                    proteinGrams = entry.protein.toDoubleOrNull(),
                                    carbsGrams = entry.carbs.toDoubleOrNull(),
                                    fatGrams = entry.fat.toDoubleOrNull(),
                                )
                            }
                            viewModel.onTrackMeal(
                                TrackMealRequestDto(
                                    date = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE),
                                    mealType = selectedMealType.first(),
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
            text = "Item $index",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        AiFitTextField(
            value = item.name,
            onValueChange = { onItemChanged(item.copy(name = it)) },
            label = "Name",
            modifier = Modifier.fillMaxWidth(),
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(AiFitSpacing.sm),
        ) {
            AiFitNumberField(
                value = item.quantity,
                onValueChange = { onItemChanged(item.copy(quantity = it)) },
                label = "Qty",
                modifier = Modifier.weight(1f),
            )
            AiFitDropdown(
                selectedValue = item.unit,
                options = UNIT_OPTIONS,
                onOptionSelected = { onItemChanged(item.copy(unit = it)) },
                label = "Unit",
                modifier = Modifier.weight(1f),
            )
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(AiFitSpacing.sm),
        ) {
            AiFitNumberField(
                value = item.calories,
                onValueChange = { onItemChanged(item.copy(calories = it)) },
                label = "kcal",
                modifier = Modifier.weight(1f),
            )
            AiFitNumberField(
                value = item.protein,
                onValueChange = { onItemChanged(item.copy(protein = it)) },
                label = "Prot(g)",
                modifier = Modifier.weight(1f),
            )
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(AiFitSpacing.sm),
        ) {
            AiFitNumberField(
                value = item.carbs,
                onValueChange = { onItemChanged(item.copy(carbs = it)) },
                label = "Carbs(g)",
                modifier = Modifier.weight(1f),
            )
            AiFitNumberField(
                value = item.fat,
                onValueChange = { onItemChanged(item.copy(fat = it)) },
                label = "Fat(g)",
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



package com.jlsh.aifit.feature.diet.ui

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jlsh.aifit.core.ui.components.buttons.AiGenerateButton
import com.jlsh.aifit.core.ui.components.inputs.AiFitDropdown
import com.jlsh.aifit.core.ui.components.inputs.AiFitNumberField
import com.jlsh.aifit.core.ui.components.inputs.AiFitTextField
import com.jlsh.aifit.core.ui.components.layout.AiFitTopBar
import com.jlsh.aifit.core.ui.theme.AIFitTheme
import com.jlsh.aifit.core.ui.theme.AiFitSpacing
import com.jlsh.aifit.feature.diet.data.dto.GenerateAdaptiveDietPlanRequestDto
import com.jlsh.aifit.feature.diet.data.dto.GenerateDietPlanRequestDto
import com.jlsh.aifit.feature.diet.ui.state.DietUiEvent
import com.jlsh.aifit.feature.diet.ui.state.GenerateDietUiState
import com.jlsh.aifit.feature.user.domain.model.DietPreference
import com.jlsh.aifit.feature.user.domain.model.GoalType

@Composable
fun GenerateDietScreen(
    adaptive: Boolean,
    basePlanId: String?,
    onNavigateBack: () -> Unit,
    onNavigateToDetail: (planId: String) -> Unit,
    viewModel: DietViewModel = hiltViewModel(),
) {
    val generateState by viewModel.generateUiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Form state
    var durationWeeks by remember { mutableStateOf("4") }
    var mealsPerDay by remember { mutableStateOf("4") }
    var dietPreference by remember { mutableStateOf(DietPreference.STANDARD.name) }
    var goalType by remember { mutableStateOf("") }
    var budget by remember { mutableStateOf("") }
    var allergies by remember { mutableStateOf("") }
    var additionalNotes by remember { mutableStateOf("") }

    // Adaptive field
    var includeNutritionHistory by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is DietUiEvent.NavigateToDetail -> onNavigateToDetail(event.planId)
                is DietUiEvent.ShowSnackbar -> snackbarHostState.showSnackbar(event.message)
                is DietUiEvent.NavigateBack -> onNavigateBack()
            }
        }
    }

    val isLoading = generateState is GenerateDietUiState.Generating

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            AiFitTopBar(
                title = if (adaptive) "Adaptive Diet" else "Generate Diet",
                onBack = onNavigateBack,
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(paddingValues)
                .padding(horizontal = AiFitSpacing.md)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(AiFitSpacing.md),
        ) {
            Spacer(modifier = Modifier.height(AiFitSpacing.sm))

            AiFitNumberField(
                value = durationWeeks,
                onValueChange = { durationWeeks = it },
                label = "Duration (weeks)",
                suffix = "weeks",
                modifier = Modifier.fillMaxWidth(),
            )

            AiFitNumberField(
                value = mealsPerDay,
                onValueChange = { mealsPerDay = it },
                label = "Meals per day",
                modifier = Modifier.fillMaxWidth(),
            )

            AiFitDropdown(
                selectedValue = dietPreference,
                options = DietPreference.entries
                    .filter { it != DietPreference.UNKNOWN }
                    .map { it.name },
                onOptionSelected = { dietPreference = it },
                label = "Diet preference",
                displayMapper = { it.replace("_", " ") },
                modifier = Modifier.fillMaxWidth(),
            )

            AiFitDropdown(
                selectedValue = goalType,
                options = listOf("") + GoalType.entries
                    .filter { it != GoalType.UNKNOWN }
                    .map { it.name },
                onOptionSelected = { goalType = it },
                label = "Goal (optional)",
                displayMapper = { if (it.isBlank()) "None" else it.replace("_", " ") },
                modifier = Modifier.fillMaxWidth(),
            )

            AiFitTextField(
                value = budget,
                onValueChange = { budget = it },
                label = "Budget (optional)",
                modifier = Modifier.fillMaxWidth(),
            )

            AiFitTextField(
                value = allergies,
                onValueChange = { allergies = it },
                label = "Allergies (optional)",
                modifier = Modifier.fillMaxWidth(),
            )

            AiFitTextField(
                value = additionalNotes,
                onValueChange = { additionalNotes = it },
                label = "Additional notes (optional)",
                singleLine = false,
                modifier = Modifier.fillMaxWidth(),
            )

            if (adaptive) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Include nutrition history",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Switch(
                        checked = includeNutritionHistory,
                        onCheckedChange = { includeNutritionHistory = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.primaryContainer,
                            checkedTrackColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                        ),
                    )
                }
            }

            Spacer(modifier = Modifier.height(AiFitSpacing.sm))

            AiGenerateButton(
                text = "GENERATE PLAN",
                loadingText = "Generando tu plan...",
                isLoading = isLoading,
                onClick = {
                    val weeks = durationWeeks.toIntOrNull() ?: 4
                    val meals = mealsPerDay.toIntOrNull() ?: 4
                    val goal = goalType.ifBlank { null }
                    val budgetVal = budget.ifBlank { null }
                    val allergiesVal = allergies.ifBlank { null }
                    val notesVal = additionalNotes.ifBlank { null }

                    if (adaptive) {
                        viewModel.onGenerateAdaptivePlan(
                            GenerateAdaptiveDietPlanRequestDto(
                                durationWeeks = weeks,
                                mealsPerDay = meals,
                                dietPreference = dietPreference,
                                goalType = goal,
                                budget = budgetVal,
                                allergies = allergiesVal,
                                additionalNotes = notesVal,
                                includeNutritionHistory = includeNutritionHistory,
                            ),
                        )
                    } else {
                        viewModel.onGeneratePlan(
                            GenerateDietPlanRequestDto(
                                durationWeeks = weeks,
                                mealsPerDay = meals,
                                dietPreference = dietPreference,
                                goalType = goal,
                                budget = budgetVal,
                                allergies = allergiesVal,
                                additionalNotes = notesVal,
                            ),
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(AiFitSpacing.lg))
        }
    }
}

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "GenerateDietScreen Dark",
)
@Composable
private fun GenerateDietScreenPreview() {
    AIFitTheme(darkTheme = true) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            topBar = { AiFitTopBar(title = "Generate Diet", onBack = {}) },
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(paddingValues)
                    .padding(horizontal = AiFitSpacing.md)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(AiFitSpacing.md),
            ) {
                Spacer(modifier = Modifier.height(AiFitSpacing.sm))
                AiFitNumberField(value = "4", onValueChange = {}, label = "Duration (weeks)")
                AiFitNumberField(value = "4", onValueChange = {}, label = "Meals per day")
                AiFitDropdown(
                    selectedValue = "STANDARD",
                    options = listOf("STANDARD", "VEGETARIAN", "VEGAN"),
                    onOptionSelected = {},
                    label = "Diet preference",
                    displayMapper = { it.replace("_", " ") },
                )
                AiGenerateButton(
                    text = "GENERATE PLAN",
                    onClick = {},
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(AiFitSpacing.lg))
            }
        }
    }
}


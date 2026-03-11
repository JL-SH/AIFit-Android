package com.jlsh.aifit.feature.training.ui

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jlsh.aifit.core.ui.components.buttons.AiGenerateButton
import com.jlsh.aifit.core.ui.components.inputs.AiFitChipGroup
import com.jlsh.aifit.core.ui.components.inputs.AiFitDropdown
import com.jlsh.aifit.core.ui.components.inputs.AiFitNumberField
import com.jlsh.aifit.core.ui.components.inputs.AiFitTextField
import com.jlsh.aifit.core.ui.components.layout.AiFitTopBar
import com.jlsh.aifit.core.ui.theme.AIFitTheme
import com.jlsh.aifit.core.ui.theme.AiFitSpacing
import com.jlsh.aifit.feature.training.data.dto.GenerateAdaptiveTrainingPlanRequestDto
import com.jlsh.aifit.feature.training.data.dto.GenerateTrainingPlanRequestDto
import com.jlsh.aifit.feature.training.domain.model.MuscleGroup
import com.jlsh.aifit.feature.training.ui.state.GeneratePlanUiState
import com.jlsh.aifit.feature.training.ui.state.TrainingUiEvent
import com.jlsh.aifit.feature.user.domain.model.FitnessLevel
import com.jlsh.aifit.feature.user.domain.model.GoalType
import com.jlsh.aifit.feature.user.domain.model.PreferredLocation

@Composable
fun GeneratePlanScreen(
    adaptive: Boolean,
    basePlanId: String?,
    onNavigateBack: () -> Unit,
    onNavigateToDetail: (planId: String) -> Unit,
    viewModel: TrainingViewModel = hiltViewModel(),
) {
    val generateState by viewModel.generateUiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Form state
    var frequency by remember { mutableStateOf("3") }
    var sessionDuration by remember { mutableStateOf("60") }
    var durationWeeks by remember { mutableStateOf("8") }
    var goalType by remember { mutableStateOf(GoalType.GAIN_MUSCLE.name) }
    var fitnessLevel by remember { mutableStateOf(FitnessLevel.INTERMEDIATE.name) }
    var location by remember { mutableStateOf(PreferredLocation.GYM.name) }
    var injuries by remember { mutableStateOf("") }
    var additionalNotes by remember { mutableStateOf("") }

    // Adaptive fields
    var focusAreas by remember { mutableStateOf<Set<String>>(emptySet()) }
    var avoidExercises by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is TrainingUiEvent.NavigateToDetail -> onNavigateToDetail(event.planId)
                is TrainingUiEvent.ShowSnackbar -> snackbarHostState.showSnackbar(event.message)
                is TrainingUiEvent.NavigateBack -> onNavigateBack()
                else -> {}
            }
        }
    }

    val isLoading = generateState is GeneratePlanUiState.Loading

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            AiFitTopBar(
                title = if (adaptive) "Adaptive Plan" else "Generate Plan",
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
                value = frequency,
                onValueChange = { frequency = it },
                label = "Frequency (days/week)",
                suffix = "days",
                modifier = Modifier.fillMaxWidth(),
            )

            AiFitNumberField(
                value = sessionDuration,
                onValueChange = { sessionDuration = it },
                label = "Session duration",
                suffix = "min",
                modifier = Modifier.fillMaxWidth(),
            )

            AiFitNumberField(
                value = durationWeeks,
                onValueChange = { durationWeeks = it },
                label = "Duration (weeks)",
                suffix = "weeks",
                modifier = Modifier.fillMaxWidth(),
            )

            AiFitDropdown(
                selectedValue = goalType,
                options = GoalType.entries.filter { it != GoalType.UNKNOWN }.map { it.name },
                onOptionSelected = { goalType = it },
                label = "Goal",
                displayMapper = { it.replace("_", " ") },
                modifier = Modifier.fillMaxWidth(),
            )

            AiFitDropdown(
                selectedValue = fitnessLevel,
                options = FitnessLevel.entries.filter { it != FitnessLevel.UNKNOWN }.map { it.name },
                onOptionSelected = { fitnessLevel = it },
                label = "Fitness level",
                displayMapper = { it.replace("_", " ") },
                modifier = Modifier.fillMaxWidth(),
            )

            AiFitDropdown(
                selectedValue = location,
                options = PreferredLocation.entries.filter { it != PreferredLocation.UNKNOWN }.map { it.name },
                onOptionSelected = { location = it },
                label = "Location",
                displayMapper = { it.replace("_", " ") },
                modifier = Modifier.fillMaxWidth(),
            )

            AiFitTextField(
                value = injuries,
                onValueChange = { injuries = it },
                label = "Injuries (optional)",
                singleLine = false,
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
                Text(
                    text = "FOCUS AREAS",
                    style = MaterialTheme.typography.labelSmall,
                    letterSpacing = 1.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                AiFitChipGroup(
                    options = MuscleGroup.entries
                        .filter { it != MuscleGroup.UNKNOWN }
                        .map { it.name },
                    selected = focusAreas,
                    onSelectionChanged = { focusAreas = it },
                    displayMapper = { it.replace("_", " ") },
                    modifier = Modifier.fillMaxWidth(),
                )

                AiFitTextField(
                    value = avoidExercises,
                    onValueChange = { avoidExercises = it },
                    label = "Exercises to avoid (optional)",
                    singleLine = false,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            Spacer(modifier = Modifier.height(AiFitSpacing.sm))

            AiGenerateButton(
                text = "GENERATE PLAN",
                loadingText = "Generando tu plan...",
                isLoading = isLoading,
                onClick = {
                    val freq = frequency.toIntOrNull() ?: 3
                    val dur = sessionDuration.toIntOrNull() ?: 60
                    val weeks = durationWeeks.toIntOrNull() ?: 8
                    val injuriesVal = injuries.ifBlank { null }
                    val notesVal = additionalNotes.ifBlank { null }

                    if (adaptive) {
                        viewModel.onGenerateAdaptivePlan(
                            GenerateAdaptiveTrainingPlanRequestDto(
                                frequencyDaysPerWeek = freq,
                                sessionDurationMinutes = dur,
                                durationWeeks = weeks,
                                goalType = goalType,
                                fitnessLevel = fitnessLevel,
                                location = location,
                                injuries = injuriesVal,
                                additionalNotes = notesVal,
                                includeAthleteHistory = true,
                                focusAreas = focusAreas.toList().ifEmpty { null },
                                avoidExercises = avoidExercises.ifBlank { null }
                                    ?.split(",")
                                    ?.map { it.trim() }
                                    ?.filter { it.isNotBlank() },
                            ),
                        )
                    } else {
                        viewModel.onGeneratePlan(
                            GenerateTrainingPlanRequestDto(
                                frequencyDaysPerWeek = freq,
                                sessionDurationMinutes = dur,
                                durationWeeks = weeks,
                                goalType = goalType,
                                fitnessLevel = fitnessLevel,
                                location = location,
                                injuries = injuriesVal,
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
    name = "GeneratePlanScreen Dark",
)
@Composable
private fun GeneratePlanScreenPreview() {
    AIFitTheme(darkTheme = true) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                AiFitTopBar(title = "Generate Plan", onBack = {})
            },
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
                AiFitNumberField(value = "3", onValueChange = {}, label = "Frequency (days/week)")
                AiFitNumberField(value = "60", onValueChange = {}, label = "Session duration", suffix = "min")
                AiFitNumberField(value = "8", onValueChange = {}, label = "Duration (weeks)")
                AiFitDropdown(
                    selectedValue = "GAIN_MUSCLE",
                    options = listOf("GAIN_MUSCLE", "LOSE_WEIGHT"),
                    onOptionSelected = {},
                    label = "Goal",
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

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "GeneratePlanScreen Adaptive Dark",
)
@Composable
private fun GeneratePlanScreenAdaptivePreview() {
    AIFitTheme(darkTheme = true) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                AiFitTopBar(title = "Adaptive Plan", onBack = {})
            },
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
                AiFitNumberField(value = "4", onValueChange = {}, label = "Frequency (days/week)")
                Text(
                    text = "FOCUS AREAS",
                    style = MaterialTheme.typography.labelSmall,
                    letterSpacing = 1.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                AiFitChipGroup(
                    options = listOf("CHEST", "BACK", "SHOULDERS", "BICEPS"),
                    selected = setOf("CHEST", "BACK"),
                    onSelectionChanged = {},
                    displayMapper = { it.replace("_", " ") },
                )
                AiGenerateButton(
                    text = "GENERATE PLAN",
                    onClick = {},
                    isLoading = true,
                    loadingText = "Generando tu plan...",
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(AiFitSpacing.lg))
            }
        }
    }
}



package com.jlsh.aifit.feature.training.ui

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jlsh.aifit.R
import com.jlsh.aifit.core.ui.components.buttons.AiGenerateButton
import com.jlsh.aifit.core.ui.components.inputs.AiFitChipGroup
import com.jlsh.aifit.core.ui.components.inputs.AiFitDropdown
import com.jlsh.aifit.core.ui.components.inputs.AiFitTextField
import com.jlsh.aifit.core.ui.components.layout.AiFitTopBar
import com.jlsh.aifit.core.ui.theme.AIFitTheme
import com.jlsh.aifit.core.ui.theme.AiFitSpacing
import com.jlsh.aifit.feature.training.data.dto.GenerateAdaptiveTrainingPlanRequestDto
import com.jlsh.aifit.feature.training.data.dto.GenerateTrainingPlanRequestDto
import com.jlsh.aifit.feature.training.domain.model.MuscleGroup
import com.jlsh.aifit.feature.training.ui.state.GeneratePlanUiState
import com.jlsh.aifit.feature.training.ui.state.TrainingUiEvent
import com.jlsh.aifit.feature.user.domain.model.GoalType
import com.jlsh.aifit.feature.user.domain.model.WorkoutLocation

private val FREQUENCY_OPTIONS = listOf(3, 4, 5, 6)
private val DURATION_OPTIONS = listOf(30, 45, 60, 75, 90)
private val WEEKS_OPTIONS = listOf(4, 6, 8, 10, 12)

@Composable
fun GeneratePlanScreen(
    adaptive: Boolean,
    basePlanId: String?,
    onNavigateBack: () -> Unit,
    onNavigateToDetail: (planId: String) -> Unit,
    viewModel: TrainingViewModel = hiltViewModel(),
) {
    val generateState by viewModel.generateUiState.collectAsStateWithLifecycle()
    val userFitnessLevel by viewModel.userFitnessLevel.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Form state
    var frequency by remember { mutableStateOf(4) }
    var sessionDuration by remember { mutableStateOf(60) }
    var durationWeeks by remember { mutableStateOf(8) }
    var goalType by remember { mutableStateOf(GoalType.GAIN_MUSCLE.name) }
    var location by remember { mutableStateOf(WorkoutLocation.GYM.name) }
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
                title = if (adaptive) "Plan adaptativo" else "Generar plan",
                onBack = if (isLoading) null else onNavigateBack,
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(AiFitSpacing.md),
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        strokeWidth = 3.dp,
                        modifier = Modifier.size(40.dp),
                    )
                    Text(
                        text = stringResource(R.string.training_generating_message),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        } else {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(paddingValues)
                .padding(horizontal = AiFitSpacing.md)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(AiFitSpacing.md),
        ) {
            Spacer(modifier = Modifier.height(AiFitSpacing.sm))

            AiFitDropdown(
                selectedValue = frequency.toString(),
                options = FREQUENCY_OPTIONS.map { it.toString() },
                onOptionSelected = { frequency = it.toInt() },
                label = "Frecuencia",
                displayMapper = { "$it días/semana" },
                modifier = Modifier.fillMaxWidth(),
            )

            AiFitDropdown(
                selectedValue = sessionDuration.toString(),
                options = DURATION_OPTIONS.map { it.toString() },
                onOptionSelected = { sessionDuration = it.toInt() },
                label = "Duración por sesión",
                displayMapper = { "$it min" },
                modifier = Modifier.fillMaxWidth(),
            )

            AiFitDropdown(
                selectedValue = durationWeeks.toString(),
                options = WEEKS_OPTIONS.map { it.toString() },
                onOptionSelected = { durationWeeks = it.toInt() },
                label = "Duración del plan",
                displayMapper = { "$it semanas" },
                modifier = Modifier.fillMaxWidth(),
            )

            AiFitDropdown(
                selectedValue = goalType,
                options = GoalType.entries.filter { it != GoalType.UNKNOWN }.map { it.name },
                onOptionSelected = { goalType = it },
                label = "Objetivo",
                displayMapper = { value ->
                    when (value) {
                        "GAIN_MUSCLE" -> "Ganar músculo"
                        "LOSE_WEIGHT" -> "Perder peso"
                        "IMPROVE_ENDURANCE" -> "Mejorar resistencia"
                        "MAINTAIN" -> "Mantenimiento"
                        "STRENGTH" -> "Fuerza"
                        "ATHLETIC_PERFORMANCE" -> "Rendimiento atlético"
                        else -> value.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            )

            AiFitDropdown(
                selectedValue = location,
                options = WorkoutLocation.entries.filter { it != WorkoutLocation.UNKNOWN }.map { it.name },
                onOptionSelected = { location = it },
                label = "Lugar de entrenamiento",
                displayMapper = { value ->
                    when (value) {
                        "GYM" -> "Gimnasio"
                        "HOME" -> "Casa"
                        "OUTDOOR" -> "Exterior"
                        "HOME_GYM" -> "Gimnasio en casa"
                        else -> value.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            )

            AiFitTextField(
                value = injuries,
                onValueChange = { injuries = it },
                label = "Lesiones (opcional)",
                singleLine = false,
                modifier = Modifier.fillMaxWidth(),
            )

            AiFitTextField(
                value = additionalNotes,
                onValueChange = { additionalNotes = it },
                label = "Notas adicionales (opcional)",
                singleLine = false,
                modifier = Modifier.fillMaxWidth(),
            )

            if (adaptive) {
                Text(
                    text = "ÁREAS DE ENFOQUE",
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
                    displayMapper = { it.replace("_", " ").lowercase().replaceFirstChar { c -> c.uppercase() } },
                    modifier = Modifier.fillMaxWidth(),
                )

                AiFitTextField(
                    value = avoidExercises,
                    onValueChange = { avoidExercises = it },
                    label = "Ejercicios a evitar (opcional)",
                    singleLine = false,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            Spacer(modifier = Modifier.height(AiFitSpacing.sm))

            AiGenerateButton(
                text = "GENERAR PLAN",
                loadingText = "Generando tu plan...",
                isLoading = isLoading,
                onClick = {
                    val injuriesVal = injuries.ifBlank { null }
                    val notesVal = additionalNotes.ifBlank { null }

                    if (adaptive) {
                        viewModel.onGenerateAdaptivePlan(
                            GenerateAdaptiveTrainingPlanRequestDto(
                                frequencyDaysPerWeek = frequency,
                                sessionDurationMinutes = sessionDuration,
                                durationWeeks = durationWeeks,
                                goalType = goalType,
                                fitnessLevel = userFitnessLevel,
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
                                frequencyDaysPerWeek = frequency,
                                sessionDurationMinutes = sessionDuration,
                                durationWeeks = durationWeeks,
                                goalType = goalType,
                                fitnessLevel = userFitnessLevel,
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
        } // else
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
                AiFitTopBar(title = "Generar plan", onBack = {})
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
                AiFitDropdown(
                    selectedValue = "4",
                    options = FREQUENCY_OPTIONS.map { it.toString() },
                    onOptionSelected = {},
                    label = "Frecuencia",
                    displayMapper = { "$it días/semana" },
                )
                AiFitDropdown(
                    selectedValue = "60",
                    options = DURATION_OPTIONS.map { it.toString() },
                    onOptionSelected = {},
                    label = "Duración por sesión",
                    displayMapper = { "$it min" },
                )
                AiFitDropdown(
                    selectedValue = "8",
                    options = WEEKS_OPTIONS.map { it.toString() },
                    onOptionSelected = {},
                    label = "Duración del plan",
                    displayMapper = { "$it semanas" },
                )
                AiFitDropdown(
                    selectedValue = "GAIN_MUSCLE",
                    options = listOf("GAIN_MUSCLE", "LOSE_WEIGHT"),
                    onOptionSelected = {},
                    label = "Objetivo",
                    displayMapper = { value ->
                        when (value) {
                            "GAIN_MUSCLE" -> "Ganar músculo"
                            "LOSE_WEIGHT" -> "Perder peso"
                            else -> value
                        }
                    },
                )
                AiGenerateButton(
                    text = "GENERAR PLAN",
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
                AiFitTopBar(title = "Plan adaptativo", onBack = {})
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
                AiFitDropdown(
                    selectedValue = "4",
                    options = FREQUENCY_OPTIONS.map { it.toString() },
                    onOptionSelected = {},
                    label = "Frecuencia",
                    displayMapper = { "$it días/semana" },
                )
                Text(
                    text = "ÁREAS DE ENFOQUE",
                    style = MaterialTheme.typography.labelSmall,
                    letterSpacing = 1.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                AiFitChipGroup(
                    options = listOf("CHEST", "BACK", "SHOULDERS", "BICEPS"),
                    selected = setOf("CHEST", "BACK"),
                    onSelectionChanged = {},
                    displayMapper = { it.replace("_", " ").lowercase().replaceFirstChar { c -> c.uppercase() } },
                )
                AiGenerateButton(
                    text = "GENERAR PLAN",
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


package com.jlsh.aifit.feature.diet.ui

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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jlsh.aifit.core.ui.components.buttons.AiGenerateButton
import com.jlsh.aifit.core.ui.components.inputs.AiFitChipGroup
import com.jlsh.aifit.core.ui.components.inputs.AiFitTextField
import com.jlsh.aifit.core.ui.components.layout.AiFitTopBar
import com.jlsh.aifit.core.ui.theme.AIFitTheme
import com.jlsh.aifit.core.ui.theme.AiFitSpacing
import com.jlsh.aifit.feature.diet.data.dto.GenerateAdaptiveDietPlanRequestDto
import com.jlsh.aifit.feature.diet.data.dto.GenerateDietPlanRequestDto
import com.jlsh.aifit.feature.diet.ui.state.DietUiEvent
import com.jlsh.aifit.feature.diet.ui.state.GenerateDietUiState

// ── Opciones predefinidas ────────────────────────────────────────────────────
private val DURATION_OPTIONS = listOf("2" to "2 semanas", "4" to "1 mes", "12" to "3 meses")
private val MEALS_OPTIONS = listOf("3" to "3 comidas", "4" to "4 comidas", "5" to "5 comidas")

private val GOAL_OPTIONS = listOf(
    "LOSE_WEIGHT" to "Perder grasa",
    "GAIN_MUSCLE" to "Ganar músculo",
    "MAINTAIN" to "Mantener peso",
    "BODY_RECOMPOSITION" to "Recomposición corporal",
)

private val PREFERENCE_OPTIONS = listOf(
    "NONE" to "Sin restricciones",
    "VEGETARIAN" to "Vegetariano",
    "VEGAN" to "Vegano",
    "GLUTEN_FREE" to "Sin gluten",
    "LACTOSE_FREE" to "Sin lactosa",
    "KETO" to "Keto",
    "MEDITERRANEAN" to "Mediterránea",
)

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
    var selectedDuration by remember { mutableStateOf(setOf("4")) }
    var selectedMeals by remember { mutableStateOf(setOf("4")) }
    var selectedGoal by remember { mutableStateOf(setOf("LOSE_WEIGHT")) }
    var selectedPreference by remember { mutableStateOf(setOf("NONE")) }
    var allergies by remember { mutableStateOf("") }
    var additionalNotes by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is DietUiEvent.NavigateToDetail -> onNavigateToDetail(event.planId)
                is DietUiEvent.ShowSnackbar -> snackbarHostState.showSnackbar(event.message)
                is DietUiEvent.NavigateBack -> onNavigateBack()
                else -> {}
            }
        }
    }

    val isLoading = generateState is GenerateDietUiState.Generating

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            AiFitTopBar(
                title = if (adaptive) "Plan de dieta adaptativo" else "Generar plan de dieta",
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
                        text = "Generando tu plan de dieta…",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = "Esto puede tardar unos segundos",
                        style = MaterialTheme.typography.bodySmall,
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

                // ── Duración ─────────────────────────────────────────
                Text(
                    text = "DURACIÓN",
                    style = MaterialTheme.typography.labelSmall,
                    letterSpacing = 1.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                AiFitChipGroup(
                    options = DURATION_OPTIONS.map { it.first },
                    selected = selectedDuration,
                    onSelectionChanged = { if (it.isNotEmpty()) selectedDuration = it },
                    multiSelect = false,
                    displayMapper = { key ->
                        DURATION_OPTIONS.firstOrNull { it.first == key }?.second ?: key
                    },
                    modifier = Modifier.fillMaxWidth(),
                )

                // ── Comidas por día ──────────────────────────────────
                Text(
                    text = "COMIDAS POR DÍA",
                    style = MaterialTheme.typography.labelSmall,
                    letterSpacing = 1.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                AiFitChipGroup(
                    options = MEALS_OPTIONS.map { it.first },
                    selected = selectedMeals,
                    onSelectionChanged = { if (it.isNotEmpty()) selectedMeals = it },
                    multiSelect = false,
                    displayMapper = { key ->
                        MEALS_OPTIONS.firstOrNull { it.first == key }?.second ?: key
                    },
                    modifier = Modifier.fillMaxWidth(),
                )

                // ── Objetivo ─────────────────────────────────────────
                Text(
                    text = "OBJETIVO",
                    style = MaterialTheme.typography.labelSmall,
                    letterSpacing = 1.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                AiFitChipGroup(
                    options = GOAL_OPTIONS.map { it.first },
                    selected = selectedGoal,
                    onSelectionChanged = { if (it.isNotEmpty()) selectedGoal = it },
                    multiSelect = false,
                    displayMapper = { key ->
                        GOAL_OPTIONS.firstOrNull { it.first == key }?.second ?: key
                    },
                    modifier = Modifier.fillMaxWidth(),
                )

                // ── Preferencia alimentaria ──────────────────────────
                Text(
                    text = "PREFERENCIA ALIMENTARIA",
                    style = MaterialTheme.typography.labelSmall,
                    letterSpacing = 1.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                AiFitChipGroup(
                    options = PREFERENCE_OPTIONS.map { it.first },
                    selected = selectedPreference,
                    onSelectionChanged = { if (it.isNotEmpty()) selectedPreference = it },
                    multiSelect = false,
                    displayMapper = { key ->
                        PREFERENCE_OPTIONS.firstOrNull { it.first == key }?.second ?: key
                    },
                    modifier = Modifier.fillMaxWidth(),
                )

                // ── Campos opcionales ────────────────────────────────
                AiFitTextField(
                    value = allergies,
                    onValueChange = { allergies = it },
                    label = "Alergias (opcional)",
                    modifier = Modifier.fillMaxWidth(),
                )

                AiFitTextField(
                    value = additionalNotes,
                    onValueChange = { additionalNotes = it },
                    label = "Notas adicionales (opcional)",
                    singleLine = false,
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(AiFitSpacing.sm))

                AiGenerateButton(
                    text = "GENERAR PLAN",
                    loadingText = "Generando tu plan…",
                    isLoading = isLoading,
                    onClick = {
                        val weeks = selectedDuration.firstOrNull()?.toIntOrNull() ?: 4
                        val meals = selectedMeals.firstOrNull()?.toIntOrNull() ?: 4
                        val goal = selectedGoal.firstOrNull()
                        val preference = selectedPreference.firstOrNull() ?: "NONE"
                        val allergiesVal = allergies.ifBlank { null }
                        val notesVal = additionalNotes.ifBlank { null }

                        if (adaptive) {
                            viewModel.onGenerateAdaptivePlan(
                                GenerateAdaptiveDietPlanRequestDto(
                                    durationWeeks = weeks,
                                    mealsPerDay = meals,
                                    dietPreference = preference,
                                    goalType = goal,
                                    allergies = allergiesVal,
                                    additionalNotes = notesVal,
                                    includeNutritionHistory = true,
                                ),
                            )
                        } else {
                            viewModel.onGeneratePlan(
                                GenerateDietPlanRequestDto(
                                    durationWeeks = weeks,
                                    mealsPerDay = meals,
                                    dietPreference = preference,
                                    goalType = goal,
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
        } // else
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
            topBar = { AiFitTopBar(title = "Generar plan de dieta", onBack = {}) },
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
                Text(
                    text = "DURACIÓN",
                    style = MaterialTheme.typography.labelSmall,
                    letterSpacing = 1.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                AiFitChipGroup(
                    options = DURATION_OPTIONS.map { it.first },
                    selected = setOf("4"),
                    onSelectionChanged = {},
                    multiSelect = false,
                    displayMapper = { key ->
                        DURATION_OPTIONS.firstOrNull { it.first == key }?.second ?: key
                    },
                )
                Text(
                    text = "OBJETIVO",
                    style = MaterialTheme.typography.labelSmall,
                    letterSpacing = 1.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                AiFitChipGroup(
                    options = GOAL_OPTIONS.map { it.first },
                    selected = setOf("LOSE_WEIGHT"),
                    onSelectionChanged = {},
                    multiSelect = false,
                    displayMapper = { key ->
                        GOAL_OPTIONS.firstOrNull { it.first == key }?.second ?: key
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


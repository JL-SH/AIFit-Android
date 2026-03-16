package com.jlsh.aifit.feature.user.ui

import android.content.res.Configuration
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jlsh.aifit.core.ui.components.buttons.PrimaryButton
import com.jlsh.aifit.core.ui.components.buttons.SecondaryButton
import com.jlsh.aifit.core.ui.components.inputs.AiFitDatePickerBottomSheet
import com.jlsh.aifit.core.ui.components.inputs.AiFitDropdown
import com.jlsh.aifit.core.ui.components.inputs.AiFitNumberField
import com.jlsh.aifit.core.ui.components.inputs.AiFitTextField
import com.jlsh.aifit.core.ui.theme.AIFitTheme
import com.jlsh.aifit.core.ui.theme.AiFitSpacing
import com.jlsh.aifit.feature.user.domain.model.Gender
import com.jlsh.aifit.feature.user.ui.state.UserUiEvent
import com.jlsh.aifit.feature.user.ui.state.UserUiState

private const val TOTAL_STEPS = 8

private data class WizardOption(
    val label: String,
    val value: String,
)

private val goalOptions = listOf(
    WizardOption("Perder grasa", "LOSE_WEIGHT"),
    WizardOption("Ganar músculo", "GAIN_MUSCLE"),
    WizardOption("Mantenerme", "MAINTAIN"),
    WizardOption("Mejorar rendimiento", "BODY_RECOMPOSITION"),
)

private val experienceOptions = listOf(
    WizardOption("Nunca he entrenado", "BEGINNER"),
    WizardOption("Menos de 1 año", "BEGINNER"),
    WizardOption("1-3 años", "INTERMEDIATE"),
    WizardOption("Más de 3 años", "ADVANCED"),
)

private val locationOptions = listOf(
    WizardOption("En casa", "HOME"),
    WizardOption("Gimnasio", "GYM"),
    WizardOption("Al aire libre", "OUTDOOR"),
    WizardOption("En casa con equipamiento", "HOME_GYM"),
)

private val sessionOptions = listOf(
    WizardOption("15-30 min", "30"),
    WizardOption("30-45 min", "45"),
    WizardOption("45-60 min", "60"),
    WizardOption("60-90 min", "90"),
    WizardOption("+90 min", "120"),
)

private val dietOptions = listOf(
    WizardOption("Sin restricciones", "NONE"),
    WizardOption("Vegetariano", "VEGETARIAN"),
    WizardOption("Vegano", "VEGAN"),
    WizardOption("Sin gluten", "GLUTEN_FREE"),
    WizardOption("Sin lactosa", "LACTOSE_FREE"),
    WizardOption("Mediterráneo", "MEDITERRANEAN"),
)

private val injuryOptions = listOf(
    "Rodilla",
    "Espalda lumbar",
    "Hombro",
    "Cervical",
    "Cadera",
    "Ninguna",
)

@Composable
fun CreateProfileScreen(
    onNavigateToOnboarding: () -> Unit,
    viewModel: UserViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val birthDateError by viewModel.birthDateError.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    var showDatePicker by rememberSaveable { mutableStateOf(false) }

    var currentStep by rememberSaveable { mutableStateOf(0) }

    var selectedGoal by rememberSaveable { mutableStateOf("") }
    var selectedExperienceIndex by rememberSaveable { mutableStateOf(-1) }
    var selectedLocation by rememberSaveable { mutableStateOf("") }
    var selectedWorkoutDays by rememberSaveable { mutableStateOf("") }
    var selectedAvailableMinutes by rememberSaveable { mutableStateOf("") }
    var selectedInjuries by rememberSaveable { mutableStateOf(listOf<String>()) }
    var injuriesDetail by rememberSaveable { mutableStateOf("") }
    var birthDate by rememberSaveable { mutableStateOf("") }
    var weight by rememberSaveable { mutableStateOf("") }
    var height by rememberSaveable { mutableStateOf("") }
    var selectedGender by rememberSaveable { mutableStateOf("") }
    var selectedDietPreference by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.onTargetWeightChanged("")
        viewModel.onCalorieTargetChanged("")
        viewModel.events.collect { event ->
            when (event) {
                is UserUiEvent.ProfileSaved -> onNavigateToOnboarding()
                is UserUiEvent.ShowSnackbar -> snackbarHostState.showSnackbar(event.message)
                else -> Unit
            }
        }
    }

    val continueEnabled = when (currentStep) {
        0 -> selectedGoal.isNotBlank()
        1 -> selectedExperienceIndex >= 0
        2 -> selectedLocation.isNotBlank()
        3 -> selectedWorkoutDays.isNotBlank()
        4 -> selectedAvailableMinutes.isNotBlank()
        5 -> true
        6 -> birthDate.isNotBlank() && weight.isNotBlank() && height.isNotBlank() && selectedGender.isNotBlank()
        7 -> selectedDietPreference.isNotBlank()
        else -> false
    }

    fun handleContinue() {
        when (currentStep) {
            1 -> {
                val fitnessLevel = experienceOptions.getOrNull(selectedExperienceIndex)?.value.orEmpty()
                val activityLevel = when (fitnessLevel) {
                    "BEGINNER" -> "LIGHT"
                    "INTERMEDIATE" -> "MODERATE"
                    "ADVANCED" -> "ACTIVE"
                    else -> ""
                }
                if (activityLevel.isNotBlank()) {
                    viewModel.onActivityLevelChanged(activityLevel)
                }
            }

            5 -> {
                val injuriesValue = if (selectedInjuries.contains("Ninguna")) {
                    ""
                } else {
                    val parts = mutableListOf<String>()
                    if (selectedInjuries.isNotEmpty()) parts += selectedInjuries.joinToString(", ")
                    if (injuriesDetail.isNotBlank()) parts += injuriesDetail.trim()
                    parts.joinToString(" | ")
                }
                viewModel.onInjuriesChanged(injuriesValue)
            }

            7 -> {
                viewModel.onSaveProfile()
                return
            }

            else -> Unit
        }

        if (currentStep < TOTAL_STEPS - 1) {
            currentStep += 1
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            snackbarHost = { SnackbarHost(snackbarHostState) },
        ) { paddingValues ->
            if (uiState is UserUiState.Loading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primaryContainer,
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = AiFitSpacing.md, vertical = AiFitSpacing.md),
                ) {
                    LinearProgressIndicator(
                        progress = { (currentStep + 1) / TOTAL_STEPS.toFloat() },
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    )

                    Spacer(Modifier.height(AiFitSpacing.md))

                    AnimatedContent(
                        targetState = currentStep,
                        transitionSpec = {
                            if (targetState > initialState) {
                                (slideInHorizontally { it } + fadeIn()) togetherWith
                                        (slideOutHorizontally { -it } + fadeOut())
                            } else {
                                (slideInHorizontally { -it } + fadeIn()) togetherWith
                                        (slideOutHorizontally { it } + fadeOut())
                            }
                        },
                        label = "onboarding_step_transition",
                        modifier = Modifier.weight(1f),
                    ) { step ->
                        when (step) {
                            0 -> WizardStepLayout(title = "¿Cuál es tu objetivo principal?") {
                                OptionCards(
                                    options = goalOptions,
                                    selectedValue = selectedGoal,
                                    onSelected = { option ->
                                        selectedGoal = option.value
                                        viewModel.onGoalTypeChanged(option.value)
                                    },
                                )
                            }

                            1 -> WizardStepLayout(title = "¿Cuánto tiempo llevas entrenando?") {
                                OptionCardsIndexed(
                                    options = experienceOptions,
                                    selectedIndex = selectedExperienceIndex,
                                    onSelected = { index, option ->
                                        selectedExperienceIndex = index
                                        viewModel.onFitnessLevelChanged(option.value)
                                    },
                                )
                            }

                            2 -> WizardStepLayout(title = "¿Dónde entrenas habitualmente?") {
                                OptionCards(
                                    options = locationOptions,
                                    selectedValue = selectedLocation,
                                    onSelected = { option ->
                                        selectedLocation = option.value
                                        viewModel.onPreferredLocationChanged(option.value)
                                    },
                                )
                            }

                            3 -> WizardStepLayout(title = "¿Cuántos días a la semana puedes entrenar?") {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(AiFitSpacing.sm),
                                ) {
                                    (1..7).forEach { day ->
                                        val value = day.toString()
                                        FilterChip(
                                            selected = selectedWorkoutDays == value,
                                            onClick = {
                                                selectedWorkoutDays = value
                                                viewModel.onWeeklyWorkoutDaysChanged(value)
                                            },
                                            label = { Text(text = value) },
                                        )
                                    }
                                }
                            }

                            4 -> WizardStepLayout(title = "¿Cuánto tiempo tienes por sesión?") {
                                OptionCards(
                                    options = sessionOptions,
                                    selectedValue = selectedAvailableMinutes,
                                    onSelected = { option ->
                                        selectedAvailableMinutes = option.value
                                        viewModel.onAvailableMinutesChanged(option.value)
                                    },
                                )
                            }

                            5 -> WizardStepLayout(title = "¿Tienes alguna lesión o limitación física?") {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(AiFitSpacing.sm),
                                ) {
                                    injuryOptions.forEach { injury ->
                                        val checked = selectedInjuries.contains(injury)
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    selectedInjuries = when {
                                                        injury == "Ninguna" && !checked -> listOf("Ninguna")
                                                        injury == "Ninguna" && checked -> emptyList()
                                                        checked -> selectedInjuries - injury
                                                        else -> (selectedInjuries - "Ninguna") + injury
                                                    }
                                                },
                                            verticalAlignment = Alignment.CenterVertically,
                                        ) {
                                            Checkbox(
                                                checked = checked,
                                                onCheckedChange = { isChecked ->
                                                    selectedInjuries = when {
                                                        injury == "Ninguna" && isChecked -> listOf("Ninguna")
                                                        injury == "Ninguna" && !isChecked -> emptyList()
                                                        !isChecked -> selectedInjuries - injury
                                                        else -> (selectedInjuries - "Ninguna") + injury
                                                    }
                                                },
                                            )
                                            Text(
                                                text = injury,
                                                style = MaterialTheme.typography.bodyLarge,
                                                color = MaterialTheme.colorScheme.onSurface,
                                            )
                                        }
                                    }

                                    AiFitTextField(
                                        value = injuriesDetail,
                                        onValueChange = { injuriesDetail = it },
                                        label = "Cuéntanos más... (opcional)",
                                        singleLine = false,
                                        modifier = Modifier.fillMaxWidth(),
                                    )
                                }
                            }

                            6 -> WizardStepLayout(title = "Datos físicos básicos") {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(AiFitSpacing.md),
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable(
                                                indication = null,
                                                interactionSource = remember { MutableInteractionSource() },
                                            ) { showDatePicker = true },
                                    ) {
                                        AiFitTextField(
                                            value = birthDate,
                                            onValueChange = {},
                                            label = "Fecha de nacimiento",
                                            error = birthDateError,
                                            enabled = false,
                                            trailingIcon = Icons.Rounded.CalendarMonth,
                                            modifier = Modifier.fillMaxWidth(),
                                        )
                                    }

                                    AiFitNumberField(
                                        value = weight,
                                        onValueChange = {
                                            weight = it
                                            viewModel.onWeightChanged(it)
                                        },
                                        label = "Peso actual",
                                        suffix = "kg",
                                    )

                                    AiFitNumberField(
                                        value = height,
                                        onValueChange = {
                                            height = it
                                            viewModel.onHeightChanged(it)
                                        },
                                        label = "Altura",
                                        suffix = "cm",
                                    )

                                    AiFitDropdown(
                                        selectedValue = selectedGender,
                                        options = Gender.entries
                                            .filter { it != Gender.UNKNOWN }
                                            .map { it.name },
                                        onOptionSelected = {
                                            selectedGender = it
                                            viewModel.onGenderChanged(it)
                                        },
                                        label = "Género",
                                        displayMapper = { it.toGenderDisplay() },
                                    )
                                }
                            }

                            7 -> WizardStepLayout(title = "¿Tienes alguna preferencia alimentaria?") {
                                OptionCards(
                                    options = dietOptions,
                                    selectedValue = selectedDietPreference,
                                    onSelected = { option ->
                                        selectedDietPreference = option.value
                                        viewModel.onDietPreferenceChanged(option.value)
                                    },
                                )
                            }
                        }
                    }

                    if (currentStep > 0) {
                        SecondaryButton(
                            text = "Atrás",
                            onClick = { currentStep -= 1 },
                            modifier = Modifier.fillMaxWidth(),
                        )

                        Spacer(Modifier.height(AiFitSpacing.sm))
                    }

                    PrimaryButton(
                        text = if (currentStep == TOTAL_STEPS - 1) "COMPLETAR PERFIL" else "Continuar",
                        onClick = ::handleContinue,
                        enabled = continueEnabled,
                        isLoading = currentStep == TOTAL_STEPS - 1 && uiState is UserUiState.Saving,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }

    AiFitDatePickerBottomSheet(
        isVisible = showDatePicker,
        initialDate = birthDate.takeIf { it.isNotBlank() },
        onDateSelected = { isoDate ->
            birthDate = isoDate
            viewModel.onBirthDateChanged(isoDate)
            showDatePicker = false
        },
        onDismiss = { showDatePicker = false },
    )
}

@Composable
private fun WizardStepLayout(
    title: String,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(AiFitSpacing.lg))
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(AiFitSpacing.xl))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f, fill = false),
            contentAlignment = Alignment.Center,
        ) {
            content()
        }
    }
}

@Composable
private fun OptionCards(
    options: List<WizardOption>,
    selectedValue: String,
    onSelected: (WizardOption) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AiFitSpacing.sm),
    ) {
        options.forEach { option ->
            val selected = selectedValue == option.value
            Surface(
                onClick = { onSelected(option) },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                color = if (selected) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                },
            ) {
                Text(
                    text = option.label,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (selected) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                    modifier = Modifier.padding(AiFitSpacing.md),
                )
            }
        }
    }
}

@Composable
private fun OptionCardsIndexed(
    options: List<WizardOption>,
    selectedIndex: Int,
    onSelected: (Int, WizardOption) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AiFitSpacing.sm),
    ) {
        options.forEachIndexed { index, option ->
            val selected = selectedIndex == index
            Surface(
                onClick = { onSelected(index, option) },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                color = if (selected) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                },
            ) {
                Text(
                    text = option.label,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (selected) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                    modifier = Modifier.padding(AiFitSpacing.md),
                )
            }
        }
    }
}

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "CreateProfileScreen Dark",
)
@Composable
private fun CreateProfileScreenPreview() {
    AIFitTheme(darkTheme = true) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                LinearProgressIndicator(
                    progress = { 1f / TOTAL_STEPS },
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                )

                Text(
                    text = "¿Cuál es tu objetivo principal?",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                OptionCards(
                    options = goalOptions,
                    selectedValue = "LOSE_WEIGHT",
                    onSelected = {},
                )

                PrimaryButton(
                    text = "Continuar",
                    onClick = {},
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}
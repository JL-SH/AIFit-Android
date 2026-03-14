package com.jlsh.aifit.feature.user.ui

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jlsh.aifit.core.ui.components.buttons.PrimaryButton
import com.jlsh.aifit.core.ui.components.inputs.AiFitDatePickerBottomSheet
import com.jlsh.aifit.core.ui.components.inputs.AiFitDropdown
import com.jlsh.aifit.core.ui.components.inputs.AiFitNumberField
import com.jlsh.aifit.core.ui.components.inputs.AiFitTextField
import com.jlsh.aifit.core.ui.components.layout.AiFitTopBar
import com.jlsh.aifit.core.ui.theme.AIFitTheme
import com.jlsh.aifit.core.ui.theme.AiFitSpacing
import com.jlsh.aifit.feature.user.domain.model.ActivityLevel
import com.jlsh.aifit.feature.user.domain.model.DietPreference
import com.jlsh.aifit.feature.user.domain.model.FitnessLevel
import com.jlsh.aifit.feature.user.domain.model.Gender
import com.jlsh.aifit.feature.user.domain.model.GoalType
import com.jlsh.aifit.feature.user.domain.model.WorkoutLocation
import com.jlsh.aifit.feature.user.ui.state.UserUiEvent
import com.jlsh.aifit.feature.user.ui.state.UserUiState

@Composable
fun UserProfileScreen(
    onNavigateBack: () -> Unit,
    viewModel: UserViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val name by viewModel.name.collectAsStateWithLifecycle()
    val birthDate by viewModel.birthDate.collectAsStateWithLifecycle()
    val birthDateError by viewModel.birthDateError.collectAsStateWithLifecycle()
    val gender by viewModel.gender.collectAsStateWithLifecycle()
    val height by viewModel.height.collectAsStateWithLifecycle()
    val weight by viewModel.weight.collectAsStateWithLifecycle()
    val targetWeight by viewModel.targetWeight.collectAsStateWithLifecycle()
    val goalType by viewModel.goalType.collectAsStateWithLifecycle()
    val activityLevel by viewModel.activityLevel.collectAsStateWithLifecycle()
    val fitnessLevel by viewModel.fitnessLevel.collectAsStateWithLifecycle()
    val preferredLocation by viewModel.preferredLocation.collectAsStateWithLifecycle()
    val dietPreference by viewModel.dietPreference.collectAsStateWithLifecycle()
    val weeklyWorkoutDays by viewModel.weeklyWorkoutDays.collectAsStateWithLifecycle()
    val availableMinutes by viewModel.availableMinutes.collectAsStateWithLifecycle()
    val injuries by viewModel.injuries.collectAsStateWithLifecycle()
    val calorieTarget by viewModel.calorieTarget.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    var showDatePicker by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is UserUiEvent.NavigateBack -> onNavigateBack()
                is UserUiEvent.ShowSnackbar -> snackbarHostState.showSnackbar(event.message)
                else -> Unit
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            AiFitTopBar(
                title = "Editar perfil",
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
            Spacer(Modifier.height(AiFitSpacing.sm))

            AiFitTextField(
                value = name,
                onValueChange = viewModel::onNameChanged,
                label = "Nombre completo",
                modifier = Modifier.fillMaxWidth(),
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                    ) { showDatePicker = true },
            ) {
                AiFitTextField(
                    value = if (birthDate.isNotBlank()) birthDate else "",
                    onValueChange = {},
                    label = "Fecha de nacimiento",
                    error = birthDateError,
                    enabled = false,
                    trailingIcon = Icons.Rounded.CalendarMonth,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            AiFitDropdown(
                selectedValue = gender,
                options = Gender.entries.filter { it != Gender.UNKNOWN }.map { it.name },
                onOptionSelected = viewModel::onGenderChanged,
                label = "Género",
                displayMapper = { it.toGenderDisplay() },
            )

            AiFitNumberField(
                value = height,
                onValueChange = viewModel::onHeightChanged,
                label = "Altura",
                suffix = "cm",
            )

            AiFitNumberField(
                value = weight,
                onValueChange = viewModel::onWeightChanged,
                label = "Peso actual",
                suffix = "kg",
            )

            AiFitNumberField(
                value = targetWeight,
                onValueChange = viewModel::onTargetWeightChanged,
                label = "Peso objetivo",
                suffix = "kg",
            )

            AiFitDropdown(
                selectedValue = goalType,
                options = GoalType.entries.filter { it != GoalType.UNKNOWN }.map { it.name },
                onOptionSelected = viewModel::onGoalTypeChanged,
                label = "Objetivo",
                displayMapper = { it.toGoalTypeDisplay() },
            )

            AiFitDropdown(
                selectedValue = activityLevel,
                options = ActivityLevel.entries.filter { it != ActivityLevel.UNKNOWN }.map { it.name },
                onOptionSelected = viewModel::onActivityLevelChanged,
                label = "Nivel de actividad",
                displayMapper = { it.toActivityLevelDisplay() },
            )

            AiFitDropdown(
                selectedValue = fitnessLevel,
                options = FitnessLevel.entries.filter { it != FitnessLevel.UNKNOWN }.map { it.name },
                onOptionSelected = viewModel::onFitnessLevelChanged,
                label = "Nivel de fitness",
                displayMapper = { it.toFitnessLevelDisplay() },
            )

            AiFitDropdown(
                selectedValue = preferredLocation,
                options = WorkoutLocation.entries.filter { it != WorkoutLocation.UNKNOWN }.map { it.name },
                onOptionSelected = viewModel::onPreferredLocationChanged,
                label = "Ubicación preferida",
                displayMapper = { it.toPreferredLocationDisplay() },
            )

            AiFitDropdown(
                selectedValue = dietPreference,
                options = DietPreference.entries.filter { it != DietPreference.UNKNOWN }.map { it.name },
                onOptionSelected = viewModel::onDietPreferenceChanged,
                label = "Preferencia dietética",
                displayMapper = { it.toDietPreferenceDisplay() },
            )

            AiFitNumberField(
                value = weeklyWorkoutDays,
                onValueChange = viewModel::onWeeklyWorkoutDaysChanged,
                label = "Días de entrenamiento/semana",
            )

            AiFitNumberField(
                value = availableMinutes,
                onValueChange = viewModel::onAvailableMinutesChanged,
                label = "Minutos disponibles/sesión",
            )

            AiFitTextField(
                value = injuries,
                onValueChange = viewModel::onInjuriesChanged,
                label = "Lesiones o limitaciones",
                singleLine = false,
                modifier = Modifier.fillMaxWidth(),
            )

            AiFitNumberField(
                value = calorieTarget,
                onValueChange = viewModel::onCalorieTargetChanged,
                label = "Objetivo calórico",
                suffix = "kcal",
            )

            Spacer(Modifier.height(AiFitSpacing.sm))

            PrimaryButton(
                text = "GUARDAR",
                onClick = viewModel::onSaveProfile,
                isLoading = uiState is UserUiState.Saving,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(AiFitSpacing.lg))
        }
    }

    AiFitDatePickerBottomSheet(
        isVisible = showDatePicker,
        initialDate = birthDate.takeIf { it.isNotBlank() },
        onDateSelected = { isoDate ->
            viewModel.onBirthDateChanged(isoDate)
            showDatePicker = false
        },
        onDismiss = { showDatePicker = false },
    )
}

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "UserProfileScreen Dark",
)
@Composable
private fun UserProfileScreenPreview() {
    AIFitTheme(darkTheme = true) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                AiFitTopBar(
                    title = "Editar perfil",
                    onBack = {},
                )
            },
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Spacer(Modifier.height(8.dp))

                AiFitTextField(
                    value = "Carlos García",
                    onValueChange = {},
                    label = "Nombre completo",
                    modifier = Modifier.fillMaxWidth(),
                )

                AiFitTextField(
                    value = "1990-05-15",
                    onValueChange = {},
                    label = "Fecha de nacimiento (yyyy-MM-dd)",
                    modifier = Modifier.fillMaxWidth(),
                )

                AiFitDropdown(
                    selectedValue = "MALE",
                    options = listOf("MALE", "FEMALE", "OTHER"),
                    onOptionSelected = {},
                    label = "Género",
                    displayMapper = { it.toGenderDisplay() },
                )

                AiFitNumberField(
                    value = "180",
                    onValueChange = {},
                    label = "Altura",
                    suffix = "cm",
                )

                AiFitNumberField(
                    value = "82",
                    onValueChange = {},
                    label = "Peso actual",
                    suffix = "kg",
                )

                AiFitDropdown(
                    selectedValue = "GAIN_MUSCLE",
                    options = listOf("LOSE_WEIGHT", "GAIN_MUSCLE", "MAINTAIN"),
                    onOptionSelected = {},
                    label = "Objetivo",
                    displayMapper = { it.toGoalTypeDisplay() },
                )

                Spacer(Modifier.height(8.dp))

                PrimaryButton(
                    text = "GUARDAR",
                    onClick = {},
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(Modifier.height(24.dp))
            }
        }
    }
}


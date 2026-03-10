package com.jlsh.aifit.feature.user.ui

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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jlsh.aifit.core.ui.components.buttons.PrimaryButton
import com.jlsh.aifit.core.ui.components.inputs.AiFitDropdown
import com.jlsh.aifit.core.ui.components.inputs.AiFitNumberField
import com.jlsh.aifit.core.ui.components.inputs.AiFitTextField
import com.jlsh.aifit.core.ui.theme.AIFitTheme
import com.jlsh.aifit.core.ui.theme.AiFitSpacing
import com.jlsh.aifit.feature.user.domain.model.ActivityLevel
import com.jlsh.aifit.feature.user.domain.model.DietPreference
import com.jlsh.aifit.feature.user.domain.model.FitnessLevel
import com.jlsh.aifit.feature.user.domain.model.Gender
import com.jlsh.aifit.feature.user.domain.model.GoalType
import com.jlsh.aifit.feature.user.domain.model.PreferredLocation
import com.jlsh.aifit.feature.user.ui.state.UserUiEvent
import com.jlsh.aifit.feature.user.ui.state.UserUiState

@Composable
fun CreateProfileScreen(
    onNavigateToMain: () -> Unit,
    viewModel: UserViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val birthDate by viewModel.birthDate.collectAsStateWithLifecycle()
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

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is UserUiEvent.ProfileSaved -> onNavigateToMain()
                is UserUiEvent.ShowSnackbar -> snackbarHostState.showSnackbar(event.message)
                else -> Unit
            }
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
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(paddingValues)
                    .padding(horizontal = AiFitSpacing.md)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(AiFitSpacing.md),
            ) {
                Spacer(Modifier.height(AiFitSpacing.xxl))

                Text(
                    text = "Completa tu perfil",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                Text(
                    text = "Cuéntanos sobre ti para personalizar tu experiencia",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Spacer(Modifier.height(AiFitSpacing.sm))

                AiFitTextField(
                    value = birthDate,
                    onValueChange = viewModel::onBirthDateChanged,
                    label = "Fecha de nacimiento (yyyy-MM-dd)",
                    modifier = Modifier.fillMaxWidth(),
                )

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
                    options = PreferredLocation.entries.filter { it != PreferredLocation.UNKNOWN }.map { it.name },
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
                    text = "COMPLETAR PERFIL",
                    onClick = viewModel::onSaveProfile,
                    isLoading = uiState is UserUiState.Saving,
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(Modifier.height(AiFitSpacing.xxl))
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
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Spacer(Modifier.height(48.dp))

                Text(
                    text = "Completa tu perfil",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                Text(
                    text = "Cuéntanos sobre ti para personalizar tu experiencia",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Spacer(Modifier.height(8.dp))

                AiFitDropdown(
                    selectedValue = "GAIN_MUSCLE",
                    options = listOf("LOSE_WEIGHT", "GAIN_MUSCLE", "MAINTAIN"),
                    onOptionSelected = {},
                    label = "Objetivo",
                    displayMapper = { it.toGoalTypeDisplay() },
                )

                AiFitDropdown(
                    selectedValue = "INTERMEDIATE",
                    options = listOf("BEGINNER", "INTERMEDIATE", "ADVANCED"),
                    onOptionSelected = {},
                    label = "Nivel de fitness",
                    displayMapper = { it.toFitnessLevelDisplay() },
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

                Spacer(Modifier.height(8.dp))

                PrimaryButton(
                    text = "COMPLETAR PERFIL",
                    onClick = {},
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(Modifier.height(48.dp))
            }
        }
    }
}


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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jlsh.aifit.R
import com.jlsh.aifit.core.ui.components.buttons.PrimaryButton
import com.jlsh.aifit.core.ui.components.feedback.ErrorScreen
import com.jlsh.aifit.core.ui.components.feedback.LoadingScreen
import com.jlsh.aifit.core.ui.components.inputs.AiFitNumberField
import com.jlsh.aifit.core.ui.components.layout.AiFitTopBar
import com.jlsh.aifit.core.ui.theme.AIFitTheme
import com.jlsh.aifit.core.ui.theme.AiFitSpacing
import com.jlsh.aifit.feature.nutrition.ui.state.NutritionTargetUiState
import com.jlsh.aifit.feature.nutrition.ui.state.NutritionUiEvent

@Composable
fun NutritionTargetScreen(
    onNavigateBack: () -> Unit,
    viewModel: NutritionViewModel = hiltViewModel(),
) {
    val targetState by viewModel.targetState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.loadNutritionTarget()
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                AiFitTopBar(
                    title = stringResource(R.string.nutrition_target_title),
                    onBack = onNavigateBack,
                    background = MaterialTheme.colorScheme.background,
                )
            },
            snackbarHost = { SnackbarHost(snackbarHostState) },
        ) { paddingValues ->
            when (val state = targetState) {
                is NutritionTargetUiState.Loading -> LoadingScreen()
                is NutritionTargetUiState.Error -> ErrorScreen(
                    message = state.message,
                    onRetry = { viewModel.loadNutritionTarget() },
                )
                is NutritionTargetUiState.Ready -> {
                    NutritionTargetContent(
                        state = state,
                        onSave = viewModel::onUpdateTarget,
                        modifier = Modifier.padding(paddingValues),
                    )
                }
            }
        }
    }
}

@Composable
private fun NutritionTargetContent(
    state: NutritionTargetUiState.Ready,
    onSave: (calories: String, protein: String, carbs: String, fat: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var calories by remember(state.calorieTarget) { mutableStateOf(state.calorieTarget) }
    var protein by remember(state.proteinTarget) { mutableStateOf(state.proteinTarget) }
    var carbs by remember(state.carbsTarget) { mutableStateOf(state.carbsTarget) }
    var fat by remember(state.fatTarget) { mutableStateOf(state.fatTarget) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = AiFitSpacing.md)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(AiFitSpacing.md),
    ) {
        Spacer(modifier = Modifier.height(AiFitSpacing.sm))

        // Set by chip
        Row {
            Surface(
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                shape = RoundedCornerShape(6.dp),
            ) {
                Text(
                    text = state.setBy,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                )
            }
        }

        AiFitNumberField(
            value = calories,
            onValueChange = { calories = it },
            label = stringResource(R.string.nutrition_target_calories_label),
            suffix = "kcal",
            modifier = Modifier.fillMaxWidth(),
        )

        AiFitNumberField(
            value = protein,
            onValueChange = { protein = it },
            label = stringResource(R.string.nutrition_target_protein_label),
            suffix = "g",
            modifier = Modifier.fillMaxWidth(),
        )

        AiFitNumberField(
            value = carbs,
            onValueChange = { carbs = it },
            label = stringResource(R.string.nutrition_target_carbs_label),
            suffix = "g",
            modifier = Modifier.fillMaxWidth(),
        )

        AiFitNumberField(
            value = fat,
            onValueChange = { fat = it },
            label = stringResource(R.string.nutrition_target_fat_label),
            suffix = "g",
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(AiFitSpacing.sm))

        PrimaryButton(
            text = stringResource(R.string.common_save),
            isLoading = state.isSaving,
            onClick = { onSave(calories, protein, carbs, fat) },
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(AiFitSpacing.lg))
    }
}

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "NutritionTargetScreen Dark",
)
@Composable
private fun NutritionTargetScreenPreview() {
    AIFitTheme(darkTheme = true) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
        ) {
            NutritionTargetContent(
                state = NutritionTargetUiState.Ready(
                    calorieTarget = "2200",
                    proteinTarget = "165",
                    carbsTarget = "250",
                    fatTarget = "73",
                    setBy = "MANUAL",
                ),
                onSave = { _, _, _, _ -> },
            )
        }
    }
}


package com.jlsh.aifit.feature.diet.ui

import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.*
import android.content.res.Configuration
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jlsh.aifit.R
import com.jlsh.aifit.core.ui.components.buttons.AiGenerateButton
import com.jlsh.aifit.core.ui.components.feedback.ErrorScreen
import com.jlsh.aifit.core.ui.components.inputs.AiFitChipGroup
import com.jlsh.aifit.core.ui.components.inputs.AiFitNumberField
import com.jlsh.aifit.core.ui.components.inputs.AiFitTextField
import com.jlsh.aifit.core.ui.components.layout.AiFitTopBar
import com.jlsh.aifit.core.ui.theme.AIFitTheme
import com.jlsh.aifit.core.ui.theme.AiFitSpacing
import com.jlsh.aifit.core.ui.theme.FullShape
import com.jlsh.aifit.feature.diet.data.dto.GenerateAdaptiveDietPlanRequestDto
import com.jlsh.aifit.feature.diet.data.dto.GenerateDietPlanRequestDto
import com.jlsh.aifit.feature.diet.ui.state.DietUiEvent
import com.jlsh.aifit.feature.diet.ui.state.GenerateDietUiState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// ── Opciones predefinidas ────────────────────────────────────────────────────
private val DURATION_OPTIONS = listOf("2" to "2 semanas", "4" to "1 mes", "12" to "3 meses")
private val MEALS_OPTIONS = listOf("3" to "3 comidas", "4" to "4 comidas", "5" to "5 comidas")

private val GOAL_OPTIONS = listOf(
    "LOSE_WEIGHT" to "Perder grasa",
    "GAIN_MUSCLE" to "Ganar músculo",
    "MAINTAIN" to "Mantener peso",
)

private val PREFERENCE_OPTIONS = listOf(
    "NONE" to "Sin restricciones",
    "VEGETARIAN" to "Vegetariano",
    "VEGAN" to "Vegano",
    "GLUTEN_FREE" to "Sin gluten",
    "LACTOSE_FREE" to "Sin lactosa",
)

// ── Animación de carga ──────────────────────────────────────────────────────
private const val FACT_SLOTS = 4
private const val ADAPTIVE_PROGRESS_K = 0.018f

private val DIET_FACTS = listOf(
    "💡 Una alimentación balanceada proporciona todos los nutrientes que tu cuerpo necesita.",
    "🥩 La proteína ayuda a mantener la masa muscular y te mantiene saciado más tiempo.",
    "🥗 Incluir verduras en cada comida aumenta la fibra y los micronutrientes.",
    "💧 Beber suficiente agua mejora la digestión y el rendimiento físico.",
    "📈 Cambios pequeños y constantes son más sostenibles que dietas extremas.",
    "🔥 La calidad de los alimentos importa tanto como la cantidad de calorías.",
    "😴 Un buen descanso regula las hormonas del hambre y la saciedad.",
    "🧠 Planificar las comidas reduce decisiones impulsivas y mejora la adherencia.",
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
    var dailyCalories by remember { mutableStateOf("") }
    var allergies by remember { mutableStateOf("") }
    var additionalNotes by remember { mutableStateOf("") }

    // Animation state
    val progress = remember { Animatable(0f) }
    var elapsedSeconds by rememberSaveable { mutableIntStateOf(0) }
    var visibleFacts by rememberSaveable {
        mutableStateOf(DIET_FACTS.take(FACT_SLOTS))
    }
    var nextFactIndex by rememberSaveable { mutableIntStateOf(FACT_SLOTS) }

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
    val errorState = generateState as? GenerateDietUiState.Error

    val submitPlan: () -> Unit = {
        val weeks = selectedDuration.firstOrNull()?.toIntOrNull() ?: 4
        val meals = selectedMeals.firstOrNull()?.toIntOrNull() ?: 4
        val goal = selectedGoal.firstOrNull()
        val preference = selectedPreference.firstOrNull() ?: "NONE"
        val calories = dailyCalories.toIntOrNull()
        val allergiesVal = allergies.ifBlank { null }
        val notesVal = additionalNotes.ifBlank { null }

        if (adaptive) {
            viewModel.onGenerateAdaptivePlan(
                GenerateAdaptiveDietPlanRequestDto(
                    durationWeeks = weeks,
                    mealsPerDay = meals,
                    dietPreference = preference,
                    goalType = goal,
                    dailyCalories = calories,
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
                    dailyCalories = calories,
                    allergies = allergiesVal,
                    additionalNotes = notesVal,
                ),
            )
        }
    }

    // Manage animation while loading
    LaunchedEffect(isLoading) {
        if (isLoading) {
            elapsedSeconds = 0
            visibleFacts = DIET_FACTS.take(FACT_SLOTS)
            nextFactIndex = FACT_SLOTS
            progress.snapTo(0f)

            // Adaptive progress
            launch {
                while (true) {
                    val current = progress.value
                    val velocity = ADAPTIVE_PROGRESS_K * (0.95f - current)
                    val next = (current + velocity).coerceAtMost(0.95f)
                    progress.animateTo(next, animationSpec = tween(500))
                }
            }

            // Timer
            launch {
                while (true) {
                    delay(1000L)
                    elapsedSeconds++
                }
            }

            // Fact rotation
            launch {
                delay(5000L)
                while (true) {
                    repeat(FACT_SLOTS) { slotIndex ->
                        val updated = visibleFacts.toMutableList()
                        updated[slotIndex] = DIET_FACTS[nextFactIndex]
                        visibleFacts = updated
                        nextFactIndex = (nextFactIndex + 1) % DIET_FACTS.size
                        delay(5000L)
                    }
                }
            }
        } else if (generateState is GenerateDietUiState.Success) {
            progress.animateTo(
                targetValue = 1f,
                animationSpec = tween(600, easing = FastOutSlowInEasing),
            )
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            AiFitTopBar(
                title = if (adaptive) stringResource(R.string.diet_generate_title_adaptive) else stringResource(R.string.diet_generate_title),
                onBack = if (isLoading && errorState == null) null else onNavigateBack,
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { paddingValues ->
        if (errorState != null) {
            ErrorScreen(
                message = errorState.message,
                onRetry = submitPlan,
                modifier = Modifier.padding(paddingValues),
            )
        } else if (isLoading) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = AiFitSpacing.md)
                    .background(MaterialTheme.colorScheme.background),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(modifier = Modifier.height(AiFitSpacing.xxl))

                DietPhaseHero()

                Spacer(modifier = Modifier.height(AiFitSpacing.xl))

                DietProgressSection(progress = progress.value)

                Spacer(modifier = Modifier.height(AiFitSpacing.lg))

                DietFactsColumn(
                    facts = visibleFacts,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                )

                Text(
                    text = stringResource(R.string.diet_generate_loading_timer, elapsedSeconds),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(AiFitSpacing.xl))
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
                    text = stringResource(R.string.diet_generate_duration_label),
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
                    text = stringResource(R.string.diet_generate_meals_label),
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
                    text = stringResource(R.string.diet_generate_goal_label),
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
                    text = stringResource(R.string.diet_generate_preference_label),
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

                // ── Calorías diarias ─────────────────────────────────
                AiFitNumberField(
                    value = dailyCalories,
                    onValueChange = { dailyCalories = it },
                    label = stringResource(R.string.diet_generate_calories_label),
                    suffix = "kcal",
                    modifier = Modifier.fillMaxWidth(),
                )

                // ── Campos opcionales ────────────────────────────────
                AiFitTextField(
                    value = allergies,
                    onValueChange = { allergies = it },
                    label = stringResource(R.string.diet_generate_allergies_label),
                    modifier = Modifier.fillMaxWidth(),
                )

                AiFitTextField(
                    value = additionalNotes,
                    onValueChange = { additionalNotes = it },
                    label = stringResource(R.string.diet_generate_notes_label),
                    singleLine = false,
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(AiFitSpacing.sm))

                AiGenerateButton(
                    text = stringResource(R.string.diet_generate_btn),
                    loadingText = stringResource(R.string.diet_generate_loading_text),
                    isLoading = isLoading,
                    onClick = submitPlan,
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(AiFitSpacing.lg))
            }
        } // else
    }
}

// ── Composables privados de la pantalla de carga animada ─────────────────────

@Composable
private fun DietPhaseHero() {
    val infiniteTransition = rememberInfiniteTransition(label = "diet_hero_transition")
    val iconScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "diet_icon_scale",
    )

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            modifier = Modifier.size(112.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            tonalElevation = 0.dp,
            shadowElevation = 0.dp,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = PhosphorIcons.Regular.ForkKnife,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier
                        .size(80.dp)
                        .scale(iconScale),
                )
            }
        }

        Spacer(modifier = Modifier.height(AiFitSpacing.md))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.diet_generate_hero_title),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )
            DietAnimatedDots()
        }

        Spacer(modifier = Modifier.height(AiFitSpacing.xs))

        Text(
            text = stringResource(R.string.diet_generate_hero_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun DietAnimatedDots() {
    val infiniteTransition = rememberInfiniteTransition(label = "diet_dots_transition")

    Row(verticalAlignment = Alignment.CenterVertically) {
        repeat(3) { index ->
            val alpha by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = keyframes {
                        durationMillis = 1_200
                        0f at 0
                        0f at 100
                        1f at 250
                        1f at 750
                        0f at 1_200
                    },
                    repeatMode = RepeatMode.Restart,
                    initialStartOffset = StartOffset(index * 300),
                ),
                label = "diet_dot_alpha_$index",
            )

            Text(
                text = ".",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = alpha),
            )
        }
    }
}

@Composable
private fun DietProgressSection(progress: Float) {
    val percentage = (progress * 100).toInt().coerceIn(0, 100)

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AiFitSpacing.sm),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = FullShape,
                ),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress.coerceIn(0f, 1f))
                    .height(12.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = FullShape,
                    ),
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
        ) {
            Text(
                text = "$percentage%",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun DietFactsColumn(
    facts: List<String>,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(AiFitSpacing.sm),
    ) {
        repeat(FACT_SLOTS) { slotIndex ->
            AnimatedContent(
                targetState = facts.getOrElse(slotIndex) { "" },
                transitionSpec = {
                    (slideInVertically { it } + fadeIn()) togetherWith
                        (slideOutVertically { -it } + fadeOut())
                },
                label = "diet_fact_slot_$slotIndex",
            ) { message ->
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    tonalElevation = 0.dp,
                    shadowElevation = 0.dp,
                ) {
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(AiFitSpacing.md),
                    )
                }
            }
        }
    }
}

// ── Previews ────────────────────────────────────────────────────────────────

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
                    text = stringResource(R.string.diet_generate_duration_label),
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
                    text = stringResource(R.string.diet_generate_goal_label),
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

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "GenerateDietScreen Loading Dark",
)
@Composable
private fun GenerateDietScreenLoadingPreview() {
    AIFitTheme(darkTheme = true) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = AiFitSpacing.md),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(modifier = Modifier.height(AiFitSpacing.xxl))
                DietPhaseHero()
                Spacer(modifier = Modifier.height(AiFitSpacing.xl))
                DietProgressSection(progress = 0.45f)
                Spacer(modifier = Modifier.height(AiFitSpacing.lg))
                DietFactsColumn(
                    facts = DIET_FACTS.take(FACT_SLOTS),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                )
                Text(
                    text = "Generando tu plan… 12s",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(AiFitSpacing.xl))
            }
        }
    }
}

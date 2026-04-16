package com.jlsh.aifit.feature.training.ui

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FitnessCenter
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
import androidx.compose.ui.text.style.TextAlign
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
import com.jlsh.aifit.core.ui.theme.FullShape
import com.jlsh.aifit.feature.training.data.dto.GenerateAdaptiveTrainingPlanRequestDto
import com.jlsh.aifit.feature.training.data.dto.GenerateTrainingPlanRequestDto
import com.jlsh.aifit.feature.training.domain.model.MuscleGroup
import com.jlsh.aifit.feature.training.ui.state.GeneratePlanUiState
import com.jlsh.aifit.feature.training.ui.state.TrainingUiEvent
import com.jlsh.aifit.feature.user.domain.model.GoalType
import com.jlsh.aifit.feature.user.domain.model.WorkoutLocation
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// ── Opciones predefinidas ────────────────────────────────────────────────────
private val FREQUENCY_OPTIONS = listOf(
    "3" to "3 días",
    "4" to "4 días",
    "5" to "5 días",
    "6" to "6 días",
)

private val SESSION_DURATION_OPTIONS = listOf(
    "30" to "30 min",
    "45" to "45 min",
    "60" to "60 min",
    "75" to "75 min",
    "90" to "90 min",
)

private val WEEKS_OPTIONS = listOf(
    "4" to "4 semanas",
    "6" to "6 semanas",
    "8" to "8 semanas",
    "10" to "10 semanas",
    "12" to "12 semanas",
)

private val GOAL_OPTIONS = listOf(
    "GAIN_MUSCLE" to "Ganar músculo",
    "LOSE_WEIGHT" to "Perder grasa",
    "MAINTAIN" to "Mantenimiento",
    "BODY_RECOMPOSITION" to "Recomposición corporal",
)

private val LOCATION_OPTIONS = listOf(
    "GYM" to "Gimnasio",
    "HOME" to "Casa",
    "OUTDOOR" to "Exterior",
    "HOME_GYM" to "Gym en casa",
)

// ── Animación de carga ──────────────────────────────────────────────────────
private const val TRAINING_FACT_SLOTS = 4
private const val TRAINING_PROGRESS_K = 0.018f

private val TRAINING_FACTS = listOf(
    "💪 La progresión de carga es clave para ganar fuerza y músculo.",
    "🔄 La variedad de ejercicios previene la adaptación y maximiza los resultados.",
    "😴 El descanso es cuando tus músculos crecen realmente.",
    "🧘 Incluir movilidad en tu rutina reduce lesiones y mejora el rendimiento.",
    "📊 Registrar tus entrenamientos te ayuda a detectar progresos y ajustar el plan.",
    "🍗 La proteína es esencial para la recuperación y la hipertrofia muscular.",
    "⏱️ El tiempo de descanso entre series influye en la intensidad y adaptación.",
    "🏋️ La técnica correcta previene lesiones y maximiza el trabajo muscular.",
)

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

    // Form state — chips devuelven Sets<String>
    var selectedFrequency by remember { mutableStateOf(setOf("4")) }
    var selectedSessionDuration by remember { mutableStateOf(setOf("60")) }
    var selectedDurationWeeks by remember { mutableStateOf(setOf("8")) }
    var selectedGoal by remember { mutableStateOf(setOf("GAIN_MUSCLE")) }
    var selectedLocation by remember { mutableStateOf(setOf("GYM")) }
    var injuries by remember { mutableStateOf("") }
    var additionalNotes by remember { mutableStateOf("") }

    // Adaptive fields
    var focusAreas by remember { mutableStateOf<Set<String>>(emptySet()) }
    var avoidExercises by remember { mutableStateOf("") }

    // Animation state
    val progress = remember { Animatable(0f) }
    var elapsedSeconds by rememberSaveable { mutableIntStateOf(0) }
    var visibleFacts by rememberSaveable { mutableStateOf(TRAINING_FACTS.take(TRAINING_FACT_SLOTS)) }
    var nextFactIndex by rememberSaveable { mutableIntStateOf(TRAINING_FACT_SLOTS) }

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

    // Gestionar animación mientras carga
    LaunchedEffect(isLoading) {
        if (isLoading) {
            elapsedSeconds = 0
            visibleFacts = TRAINING_FACTS.take(TRAINING_FACT_SLOTS)
            nextFactIndex = TRAINING_FACT_SLOTS
            progress.snapTo(0f)

            // Progreso adaptativo
            launch {
                while (true) {
                    val current = progress.value
                    val velocity = TRAINING_PROGRESS_K * (0.95f - current)
                    val next = (current + velocity).coerceAtMost(0.95f)
                    progress.animateTo(next, animationSpec = tween(500))
                }
            }

            // Cronómetro
            launch {
                while (true) {
                    delay(1000L)
                    elapsedSeconds++
                }
            }

            // Rotación de facts
            launch {
                delay(5000L)
                while (true) {
                    repeat(TRAINING_FACT_SLOTS) { slotIndex ->
                        val updated = visibleFacts.toMutableList()
                        updated[slotIndex] = TRAINING_FACTS[nextFactIndex]
                        visibleFacts = updated
                        nextFactIndex = (nextFactIndex + 1) % TRAINING_FACTS.size
                        delay(5000L)
                    }
                }
            }
        } else if (generateState is GeneratePlanUiState.Success) {
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
                title = if (adaptive) "Plan adaptativo" else "Generar plan",
                onBack = if (isLoading) null else onNavigateBack,
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { paddingValues ->
        if (isLoading) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = AiFitSpacing.md)
                    .background(MaterialTheme.colorScheme.background),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(modifier = Modifier.height(AiFitSpacing.xxl))

                TrainingPhaseHero()

                Spacer(modifier = Modifier.height(AiFitSpacing.xl))

                TrainingProgressSection(progress = progress.value)

                Spacer(modifier = Modifier.height(AiFitSpacing.lg))

                TrainingFactsColumn(
                    facts = visibleFacts,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                )

                Text(
                    text = "Generando tu plan… ${elapsedSeconds}s",
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

            // ── Frecuencia semanal ────────────────────────────────
            Text(
                text = "FRECUENCIA SEMANAL",
                style = MaterialTheme.typography.labelSmall,
                letterSpacing = 1.5.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            AiFitChipGroup(
                options = FREQUENCY_OPTIONS.map { it.first },
                selected = selectedFrequency,
                onSelectionChanged = { if (it.isNotEmpty()) selectedFrequency = it },
                multiSelect = false,
                displayMapper = { key ->
                    FREQUENCY_OPTIONS.firstOrNull { it.first == key }?.second ?: key
                },
                modifier = Modifier.fillMaxWidth(),
            )

            // ── Duración por sesión ───────────────────────────────
            Text(
                text = "DURACIÓN POR SESIÓN",
                style = MaterialTheme.typography.labelSmall,
                letterSpacing = 1.5.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            AiFitChipGroup(
                options = SESSION_DURATION_OPTIONS.map { it.first },
                selected = selectedSessionDuration,
                onSelectionChanged = { if (it.isNotEmpty()) selectedSessionDuration = it },
                multiSelect = false,
                displayMapper = { key ->
                    SESSION_DURATION_OPTIONS.firstOrNull { it.first == key }?.second ?: key
                },
                modifier = Modifier.fillMaxWidth(),
            )

            // ── Duración del plan ─────────────────────────────────
            Text(
                text = "DURACIÓN DEL PLAN",
                style = MaterialTheme.typography.labelSmall,
                letterSpacing = 1.5.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            AiFitChipGroup(
                options = WEEKS_OPTIONS.map { it.first },
                selected = selectedDurationWeeks,
                onSelectionChanged = { if (it.isNotEmpty()) selectedDurationWeeks = it },
                multiSelect = false,
                displayMapper = { key ->
                    WEEKS_OPTIONS.firstOrNull { it.first == key }?.second ?: key
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

            // ── Lugar de entrenamiento ────────────────────────────
            Text(
                text = "LUGAR DE ENTRENAMIENTO",
                style = MaterialTheme.typography.labelSmall,
                letterSpacing = 1.5.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            AiFitChipGroup(
                options = LOCATION_OPTIONS.map { it.first },
                selected = selectedLocation,
                onSelectionChanged = { if (it.isNotEmpty()) selectedLocation = it },
                multiSelect = false,
                displayMapper = { key ->
                    LOCATION_OPTIONS.firstOrNull { it.first == key }?.second ?: key
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
                    val frequencyVal = selectedFrequency.firstOrNull()?.toIntOrNull() ?: 4
                    val sessionDurationVal = selectedSessionDuration.firstOrNull()?.toIntOrNull() ?: 60
                    val durationWeeksVal = selectedDurationWeeks.firstOrNull()?.toIntOrNull() ?: 8
                    val goalTypeVal = selectedGoal.firstOrNull() ?: GoalType.GAIN_MUSCLE.name
                    val locationVal = selectedLocation.firstOrNull() ?: WorkoutLocation.GYM.name
                    val injuriesVal = injuries.ifBlank { null }
                    val notesVal = additionalNotes.ifBlank { null }

                    if (adaptive) {
                        viewModel.onGenerateAdaptivePlan(
                            GenerateAdaptiveTrainingPlanRequestDto(
                                frequencyDaysPerWeek = frequencyVal,
                                sessionDurationMinutes = sessionDurationVal,
                                durationWeeks = durationWeeksVal,
                                goalType = goalTypeVal,
                                fitnessLevel = userFitnessLevel,
                                location = locationVal,
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
                                frequencyDaysPerWeek = frequencyVal,
                                sessionDurationMinutes = sessionDurationVal,
                                durationWeeks = durationWeeksVal,
                                goalType = goalTypeVal,
                                fitnessLevel = userFitnessLevel,
                                location = locationVal,
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

// ── Composables privados de la pantalla de carga animada ─────────────────────

@Composable
private fun TrainingPhaseHero() {
    val infiniteTransition = rememberInfiniteTransition(label = "training_hero_transition")
    val iconScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "training_icon_scale",
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
                    imageVector = Icons.Rounded.FitnessCenter,
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
                text = "Diseñando tu plan de entrenamiento",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )
            TrainingAnimatedDots()
        }

        Spacer(modifier = Modifier.height(AiFitSpacing.xs))

        Text(
            text = "Calculando series, reps y progresión personalizada",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun TrainingAnimatedDots() {
    val infiniteTransition = rememberInfiniteTransition(label = "training_dots_transition")

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
                label = "training_dot_alpha_$index",
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
private fun TrainingProgressSection(progress: Float) {
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
private fun TrainingFactsColumn(
    facts: List<String>,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(AiFitSpacing.sm),
    ) {
        repeat(TRAINING_FACT_SLOTS) { slotIndex ->
            AnimatedContent(
                targetState = facts.getOrElse(slotIndex) { "" },
                transitionSpec = {
                    (slideInVertically { it } + fadeIn()) togetherWith
                        (slideOutVertically { -it } + fadeOut())
                },
                label = "training_fact_slot_$slotIndex",
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

// ── Previews ─────────────────────────────────────────────────────────────────

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
            topBar = { AiFitTopBar(title = "Generar plan", onBack = {}) },
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
                    text = "FRECUENCIA SEMANAL",
                    style = MaterialTheme.typography.labelSmall,
                    letterSpacing = 1.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                AiFitChipGroup(
                    options = FREQUENCY_OPTIONS.map { it.first },
                    selected = setOf("4"),
                    onSelectionChanged = {},
                    multiSelect = false,
                    displayMapper = { key -> FREQUENCY_OPTIONS.firstOrNull { it.first == key }?.second ?: key },
                )
                Text(
                    text = "OBJETIVO",
                    style = MaterialTheme.typography.labelSmall,
                    letterSpacing = 1.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                AiFitChipGroup(
                    options = GOAL_OPTIONS.map { it.first },
                    selected = setOf("GAIN_MUSCLE"),
                    onSelectionChanged = {},
                    multiSelect = false,
                    displayMapper = { key -> GOAL_OPTIONS.firstOrNull { it.first == key }?.second ?: key },
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
    name = "GeneratePlanScreen Loading Dark",
)
@Composable
private fun GeneratePlanScreenLoadingPreview() {
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
                TrainingPhaseHero()
                Spacer(modifier = Modifier.height(AiFitSpacing.xl))
                TrainingProgressSection(progress = 0.45f)
                Spacer(modifier = Modifier.height(AiFitSpacing.lg))
                TrainingFactsColumn(
                    facts = TRAINING_FACTS.take(TRAINING_FACT_SLOTS),
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






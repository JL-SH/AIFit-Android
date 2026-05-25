package com.jlsh.aifit.feature.user.ui

import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.*
import android.content.res.Configuration
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jlsh.aifit.R
import com.jlsh.aifit.core.ui.components.feedback.ErrorScreen
import com.jlsh.aifit.core.ui.theme.AIFitTheme
import com.jlsh.aifit.core.ui.theme.AiFitSpacing
import com.jlsh.aifit.core.ui.theme.FullShape
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private data class OnboardingPhaseUi(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
)

// onboardingPhases will be built at compose time via @Composable

private const val MESSAGE_SLOTS = 6
private const val ADAPTIVE_PROGRESS_K = 0.018f

@Composable
fun OnboardingGeneratingScreen(
    onSuccess: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val progress = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    val fitnessFacts = stringArrayResource(R.array.onboarding_fitness_facts).toList()
    val errorState = state as? OnboardingState.Error
    val onboardingPhases = listOf(
        OnboardingPhaseUi(
            title = stringResource(R.string.onboarding_phase_profile_title),
            subtitle = stringResource(R.string.onboarding_phase_profile_subtitle),
            icon = PhosphorIcons.Regular.UserCircle,
        ),
        OnboardingPhaseUi(
            title = stringResource(R.string.onboarding_phase_training_title),
            subtitle = stringResource(R.string.onboarding_phase_training_subtitle),
            icon = PhosphorIcons.Regular.Barbell,
        ),
        OnboardingPhaseUi(
            title = stringResource(R.string.onboarding_phase_nutrition_title),
            subtitle = stringResource(R.string.onboarding_phase_nutrition_subtitle),
            icon = PhosphorIcons.Regular.ForkKnife,
        ),
    )

    var elapsedSeconds by rememberSaveable { mutableIntStateOf(0) }
    var visibleFacts by rememberSaveable { mutableStateOf(fitnessFacts.take(MESSAGE_SLOTS)) }
    var nextFactIndex by rememberSaveable { mutableIntStateOf(MESSAGE_SLOTS) }
    var shouldHandleGeneratingState by remember { mutableStateOf(false) }
    var cameFromFullGeneration by remember { mutableStateOf(false) }

    var progressJob by remember { mutableStateOf<Job?>(null) }
    var timerJob by remember { mutableStateOf<Job?>(null) }
    var factsJob by remember { mutableStateOf<Job?>(null) }

    val currentPhase = when {
        elapsedSeconds < 8 -> onboardingPhases[0]
        elapsedSeconds < 33 -> onboardingPhases[1]
        else -> onboardingPhases[2]
    }

    fun resetVisualState() {
        elapsedSeconds = 0
        visibleFacts = fitnessFacts.take(MESSAGE_SLOTS)
        nextFactIndex = MESSAGE_SLOTS
    }

    fun startVisualLoop() {
        progressJob?.cancel()
        timerJob?.cancel()
        factsJob?.cancel()

        progressJob = null
        timerJob = null
        factsJob = null
    }

    DisposableEffect(Unit) {
        onDispose {
            progressJob?.cancel()
            timerJob?.cancel()
            factsJob?.cancel()
        }
    }

    LaunchedEffect(Unit) {
        if (viewModel.state.value is OnboardingState.Idle) {
            resetVisualState()
            shouldHandleGeneratingState = true
            viewModel.generatePlan()
        }
    }

    LaunchedEffect(state) {
        if (state is OnboardingState.Generating && shouldHandleGeneratingState) {
            shouldHandleGeneratingState = false
            startVisualLoop()

            timerJob = launch {
                while (true) {
                    delay(1000L)
                    elapsedSeconds += 1
                }
            }

            factsJob = launch {
                delay(5000L)
                while (true) {
                    repeat(MESSAGE_SLOTS) { slotIndex ->
                        val updated = visibleFacts.toMutableList()
                        updated[slotIndex] = fitnessFacts[nextFactIndex]
                        visibleFacts = updated
                        nextFactIndex = (nextFactIndex + 1) % fitnessFacts.size
                        delay(5000L)
                    }
                }
            }

            progressJob = launch {
                progress.snapTo(0f)
                while (true) {
                    val current = progress.value
                    val velocity = ADAPTIVE_PROGRESS_K * (0.95f - current)
                    val next = (current + velocity).coerceAtMost(0.95f)
                    progress.animateTo(
                        targetValue = next,
                        animationSpec = tween(durationMillis = 500),
                    )
                }
            }
        }
    }

    // Observe state for success/error
    LaunchedEffect(state) {
        when (val currentState = state) {
            is OnboardingState.Generating -> {
                cameFromFullGeneration = true
            }
            is OnboardingState.RegeneratingTraining,
            is OnboardingState.RegeneratingDiet -> {
                cameFromFullGeneration = false
            }
            is OnboardingState.Ready -> {
                if (cameFromFullGeneration) {
                    progressJob?.cancel()
                    timerJob?.cancel()
                    factsJob?.cancel()
                    progress.animateTo(
                        targetValue = 1f,
                        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
                    )
                    onSuccess()
                }
            }
            is OnboardingState.Error -> {
                progressJob?.cancel()
                timerJob?.cancel()
                factsJob?.cancel()
            }
            else -> Unit
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        if (errorState != null) {
            ErrorScreen(
                message = errorState.message,
                onRetry = {
                    resetVisualState()
                    scope.launch { progress.snapTo(0f) }
                    shouldHandleGeneratingState = true
                    viewModel.generatePlan()
                },
            )
        } else {
        Scaffold(
            containerColor = Color.Transparent,
            snackbarHost = { SnackbarHost(snackbarHostState) },
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = AiFitSpacing.md),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(modifier = Modifier.height(AiFitSpacing.xxl))

                AnimatedContent(
                    targetState = currentPhase,
                    transitionSpec = {
                        (fadeIn(animationSpec = tween(500)) +
                            slideInVertically(animationSpec = tween(500)) { it / 4 }) togetherWith
                            (fadeOut(animationSpec = tween(350)) +
                                slideOutVertically(animationSpec = tween(350)) { -it / 4 })
                    },
                    label = "phase_content",
                ) { phase ->
                    PhaseHero(phase = phase)
                }

                Spacer(modifier = Modifier.height(AiFitSpacing.xl))

                ProgressSection(progress = progress.value)

                Spacer(modifier = Modifier.height(AiFitSpacing.lg))

                FactsStack(
                    facts = visibleFacts,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                )

                Text(
                    text = stringResource(R.string.onboarding_generating_status, elapsedSeconds),
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
}

@Composable
private fun PhaseHero(
    phase: OnboardingPhaseUi,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "phase_hero_transition")
    val iconScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "phase_icon_scale",
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Surface(
            modifier = Modifier.size(112.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            tonalElevation = 0.dp,
            shadowElevation = 0.dp,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = phase.icon,
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
                text = phase.title,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )
            AnimatedDots()
        }

        Spacer(modifier = Modifier.height(AiFitSpacing.xs))

        Text(
            text = phase.subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun AnimatedDots() {
    val infiniteTransition = rememberInfiniteTransition(label = "animated_dots_transition")

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
                label = "dot_alpha_$index",
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
private fun ProgressSection(progress: Float) {
    val percentage = (progress * 100).toInt().coerceIn(0, 100)

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AiFitSpacing.sm),
    ) {
        OnboardingProgressBar(progress = progress)
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
private fun OnboardingProgressBar(progress: Float) {
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
}

@Composable
private fun FactsStack(
    facts: List<String>,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(AiFitSpacing.sm),
    ) {
        repeat(MESSAGE_SLOTS) { slotIndex ->
            AnimatedContent(
                targetState = facts.getOrElse(slotIndex) { "" },
                transitionSpec = {
                    (slideInVertically { it } + fadeIn()) togetherWith
                        (slideOutVertically { -it } + fadeOut())
                },
                label = "fact_slot_$slotIndex",
            ) { message ->
                FactCard(
                    text = message,
                    textStyle = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}

@Composable
private fun FactCard(
    text: String,
    textStyle: TextStyle = MaterialTheme.typography.bodyMedium,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Text(
            text = text,
            style = textStyle,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(AiFitSpacing.md),
        )
    }
}

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "OnboardingGeneratingScreen Dark",
)
@Composable
private fun OnboardingGeneratingScreenPreview() {
    AIFitTheme(darkTheme = true) {
        val facts = stringArrayResource(R.array.onboarding_fitness_facts).toList()
        val phases = listOf(
            OnboardingPhaseUi(
                title = stringResource(R.string.onboarding_phase_profile_title),
                subtitle = stringResource(R.string.onboarding_phase_profile_subtitle),
                icon = PhosphorIcons.Regular.UserCircle,
            ),
            OnboardingPhaseUi(
                title = stringResource(R.string.onboarding_phase_training_title),
                subtitle = stringResource(R.string.onboarding_phase_training_subtitle),
                icon = PhosphorIcons.Regular.Barbell,
            ),
            OnboardingPhaseUi(
                title = stringResource(R.string.onboarding_phase_nutrition_title),
                subtitle = stringResource(R.string.onboarding_phase_nutrition_subtitle),
                icon = PhosphorIcons.Regular.ForkKnife,
            ),
        )
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
                PhaseHero(phase = phases[1])
                Spacer(modifier = Modifier.height(AiFitSpacing.xl))
                ProgressSection(progress = 0.67f)
                Spacer(modifier = Modifier.height(AiFitSpacing.lg))
                FactsStack(
                    facts = facts.take(6),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                )
                Text(
                    text = stringResource(R.string.onboarding_generating_status, 27),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(AiFitSpacing.xl))
            }
        }
    }
}

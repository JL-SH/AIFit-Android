package com.jlsh.aifit.feature.home.ui

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jlsh.aifit.core.ui.components.buttons.PrimaryButton
import com.jlsh.aifit.core.ui.components.buttons.SecondaryButton
import com.jlsh.aifit.core.ui.components.display.AdherenceBar
import com.jlsh.aifit.core.ui.components.display.AiFitCard
import com.jlsh.aifit.core.ui.components.display.AvatarSize
import com.jlsh.aifit.core.ui.components.display.ChartEntry
import com.jlsh.aifit.core.ui.components.display.LineChartView
import com.jlsh.aifit.core.ui.components.display.MacroProgressBar
import com.jlsh.aifit.core.ui.components.display.MacroRingChart
import com.jlsh.aifit.core.ui.components.display.MacroRingData
import com.jlsh.aifit.core.ui.components.display.PlanStatusBadge
import com.jlsh.aifit.core.ui.components.display.StreakBadge
import com.jlsh.aifit.core.ui.components.display.UserAvatar
import com.jlsh.aifit.core.ui.components.feedback.InlineLoadingIndicator
import com.jlsh.aifit.core.ui.theme.AIFitTheme
import com.jlsh.aifit.core.ui.theme.AiFitSpacing
import com.jlsh.aifit.feature.gamification.domain.model.Streak
import com.jlsh.aifit.feature.gamification.domain.model.StreakStatus
import com.jlsh.aifit.feature.gamification.domain.model.StreakType
import com.jlsh.aifit.feature.home.ui.state.HomeUiEvent
import com.jlsh.aifit.feature.home.ui.state.HomeUiState
import com.jlsh.aifit.feature.home.ui.state.TodayNutritionState
import com.jlsh.aifit.feature.home.ui.state.TodayTrainingState
import com.jlsh.aifit.feature.progress.domain.model.BodyWeightLog
import com.jlsh.aifit.feature.progress.domain.model.WeeklyProgressSummary
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import com.jlsh.aifit.core.ui.components.display.StreakStatus as BadgeStreakStatus

@Composable
fun HomeScreen(
    onNavigateToWorkoutLog: (planId: String) -> Unit,
    onNavigateToTrackMeal: () -> Unit,
    onNavigateToProgressDashboard: () -> Unit,
    onNavigateToBodyWeight: () -> Unit,
    onNavigateToGamification: (tab: String) -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToGeneratePlan: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is HomeUiEvent.NavigateToWorkoutLog -> onNavigateToWorkoutLog(event.planId)
                is HomeUiEvent.NavigateToTrackMeal -> onNavigateToTrackMeal()
                is HomeUiEvent.NavigateToProgressDashboard -> onNavigateToProgressDashboard()
                is HomeUiEvent.NavigateToBodyWeight -> onNavigateToBodyWeight()
                is HomeUiEvent.NavigateToGamification -> onNavigateToGamification(event.tab)
                is HomeUiEvent.NavigateToProfile -> onNavigateToProfile()
                is HomeUiEvent.NavigateToGeneratePlan -> onNavigateToGeneratePlan()
                is HomeUiEvent.ShowSnackbar -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        when (val state = uiState) {
            is HomeUiState.Loading -> {
                InlineLoadingIndicator(
                    message = "Cargando…",
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(AiFitSpacing.xl),
                )
            }

            is HomeUiState.Error -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(AiFitSpacing.md),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = state.message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                    )
                    Spacer(modifier = Modifier.height(AiFitSpacing.md))
                    SecondaryButton(
                        text = "Reintentar",
                        onClick = { viewModel.loadAll() },
                    )
                }
            }

            is HomeUiState.Success -> {
                HomeContent(
                    state = state,
                    onStartSession = viewModel::onStartSession,
                    onLogMeal = viewModel::onLogMeal,
                    onProgressDashboard = viewModel::onProgressDashboard,
                    onBodyWeight = viewModel::onBodyWeight,
                    onStreakTap = { viewModel.onGamification("ACHIEVEMENTS") },
                    onProfile = viewModel::onProfile,
                    onCreatePlan = viewModel::onCreatePlan,
                )
            }
        }
    }
}

// ── Content ──────────────────────────────────────────────────────────────────

@Composable
private fun HomeContent(
    state: HomeUiState.Success,
    onStartSession: (String) -> Unit,
    onLogMeal: () -> Unit,
    onProgressDashboard: () -> Unit,
    onBodyWeight: () -> Unit,
    onStreakTap: () -> Unit,
    onProfile: () -> Unit,
    onCreatePlan: () -> Unit,
) {
    LazyColumn(
        contentPadding = PaddingValues(
            start = AiFitSpacing.md,
            end = AiFitSpacing.md,
            top = AiFitSpacing.md,
            bottom = 88.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(AiFitSpacing.md),
    ) {
        // 1. Greeting header
        item(key = "greeting") {
            GreetingHeader(
                userName = state.userName,
                avatarUrl = state.avatarUrl,
                onProfileClick = onProfile,
            )
        }

        // 2. Today's Training card
        item(key = "training") {
            TodayTrainingCard(
                training = state.todayTraining,
                onStartSession = onStartSession,
                onCreatePlan = onCreatePlan,
            )
        }

        // 3. Today's Nutrition card
        item(key = "nutrition") {
            TodayNutritionCard(
                nutrition = state.todayNutrition,
                onLogMeal = onLogMeal,
            )
        }

        // 4. Streak row
        if (state.streaks.isNotEmpty()) {
            item(key = "streaks") {
                StreakRow(
                    streaks = state.streaks,
                    onTap = onStreakTap,
                )
            }
        }

        // 5. Weekly Progress card
        if (state.weeklySummary != null) {
            item(key = "weekly") {
                WeeklyProgressCard(
                    summary = state.weeklySummary,
                    onTap = onProgressDashboard,
                )
            }
        }

        // 6. Weight trend mini-chart
        if (state.weightEntries.size >= 2) {
            item(key = "weight") {
                WeightTrendCard(
                    entries = state.weightEntries,
                    onTap = onBodyWeight,
                )
            }
        }
    }
}

// ── 1. Greeting ──────────────────────────────────────────────────────────────

@Composable
private fun GreetingHeader(
    userName: String,
    avatarUrl: String?,
    onProfileClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = AiFitSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "${HomeViewModel.greetingForTime()},",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = userName,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        UserAvatar(
            name = userName,
            imageUrl = avatarUrl,
            size = AvatarSize.DEFAULT,
            modifier = Modifier.clickable(onClick = onProfileClick),
        )
    }
}

// ── 2. Today's Training ──────────────────────────────────────────────────────

@Composable
private fun TodayTrainingCard(
    training: TodayTrainingState?,
    onStartSession: (String) -> Unit,
    onCreatePlan: () -> Unit,
) {
    AiFitCard(
        containerColor = MaterialTheme.colorScheme.secondaryContainer,
    ) {
        Column(
            modifier = Modifier.padding(AiFitSpacing.md),
            verticalArrangement = Arrangement.spacedBy(AiFitSpacing.sm),
        ) {
            if (training != null) {
                PlanStatusBadge(status = "ACTIVE")
                Text(
                    text = training.planName,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                )
                Text(
                    text = "${training.dayName}  ·  ${training.exerciseCount} ejercicios",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f),
                )
                AdherenceBar(percentage = training.adherencePercentage)
                PrimaryButton(
                    text = "INICIAR SESIÓN",
                    onClick = { onStartSession(training.planId) },
                    modifier = Modifier.fillMaxWidth(),
                )
            } else {
                Text(
                    text = "Entrenamiento de hoy",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                )
                Text(
                    text = "No tienes un plan de entrenamiento activo",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f),
                )
                SecondaryButton(
                    text = "CREAR PLAN",
                    onClick = onCreatePlan,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

// ── 3. Today's Nutrition ─────────────────────────────────────────────────────

@Composable
private fun TodayNutritionCard(
    nutrition: TodayNutritionState?,
    onLogMeal: () -> Unit,
) {
    AiFitCard {
        Column(
            modifier = Modifier.padding(AiFitSpacing.md),
            verticalArrangement = Arrangement.spacedBy(AiFitSpacing.sm),
        ) {
            Text(
                text = "Nutrición de hoy",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )

            if (nutrition != null && (nutrition.caloriesConsumed > 0 || nutrition.calorieTarget > 0)) {
                MacroRingChart(
                    data = MacroRingData(
                        currentCalories = nutrition.caloriesConsumed.toFloat(),
                        targetCalories = nutrition.calorieTarget.toFloat().coerceAtLeast(1f),
                        currentProtein = nutrition.proteinConsumed.toFloat(),
                        targetProtein = nutrition.proteinTarget.toFloat().coerceAtLeast(1f),
                        currentCarbs = nutrition.carbsConsumed.toFloat(),
                        targetCarbs = nutrition.carbsTarget.toFloat().coerceAtLeast(1f),
                        currentFat = nutrition.fatConsumed.toFloat(),
                        targetFat = nutrition.fatTarget.toFloat().coerceAtLeast(1f),
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    size = 100.dp,
                    strokeWidth = 10.dp,
                )

                Spacer(modifier = Modifier.height(AiFitSpacing.xs))

                MacroProgressBar(
                    name = "Proteína",
                    current = nutrition.proteinConsumed.toFloat(),
                    target = nutrition.proteinTarget.toFloat(),
                    color = MaterialTheme.colorScheme.primaryContainer,
                )
                MacroProgressBar(
                    name = "Carbos",
                    current = nutrition.carbsConsumed.toFloat(),
                    target = nutrition.carbsTarget.toFloat(),
                    color = MaterialTheme.colorScheme.tertiary,
                )
                MacroProgressBar(
                    name = "Grasa",
                    current = nutrition.fatConsumed.toFloat(),
                    target = nutrition.fatTarget.toFloat(),
                    color = MaterialTheme.colorScheme.secondary,
                )

                SecondaryButton(
                    text = "REGISTRAR COMIDA",
                    onClick = onLogMeal,
                    modifier = Modifier.fillMaxWidth(),
                )
            } else {
                Text(
                    text = "Registra tu primera comida del día",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                SecondaryButton(
                    text = "AÑADIR COMIDA",
                    onClick = onLogMeal,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

// ── 4. Streak Row ────────────────────────────────────────────────────────────

@Composable
private fun StreakRow(
    streaks: List<Streak>,
    onTap: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onTap)
            .padding(vertical = AiFitSpacing.xs),
        horizontalArrangement = Arrangement.spacedBy(AiFitSpacing.lg),
    ) {
        streaks.forEach { streak ->
            StreakBadge(
                count = streak.currentCount,
                label = streakLabel(streak.type),
                status = streak.status.toBadgeStatus(),
            )
        }
    }
}

// ── 5. Weekly Progress ───────────────────────────────────────────────────────

@Composable
private fun WeeklyProgressCard(
    summary: WeeklyProgressSummary,
    onTap: () -> Unit,
) {
    AiFitCard(
        modifier = Modifier.clickable(onClick = onTap),
    ) {
        Column(
            modifier = Modifier.padding(AiFitSpacing.md),
            verticalArrangement = Arrangement.spacedBy(AiFitSpacing.sm),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Progreso semanal",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Icon(
                    imageVector = Icons.Rounded.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp),
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "Entrenamientos",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "${summary.workoutsThisWeek} / ${summary.workoutsTarget}",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primaryContainer,
                )
            }

            val adherence = if (summary.workoutsTarget > 0) {
                (summary.workoutsThisWeek.toFloat() / summary.workoutsTarget * 100f)
                    .coerceIn(0f, 100f)
            } else 0f
            AdherenceBar(percentage = adherence)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "Calorías promedio",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "${"%.0f".format(summary.averageCaloriesToday)} / ${summary.calorieTarget}",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primaryContainer,
                )
            }
        }
    }
}

// ── 6. Weight Trend ──────────────────────────────────────────────────────────

@Composable
private fun WeightTrendCard(
    entries: List<BodyWeightLog>,
    onTap: () -> Unit,
) {
    AiFitCard(
        modifier = Modifier.clickable(onClick = onTap),
    ) {
        Column(
            modifier = Modifier.padding(AiFitSpacing.md),
            verticalArrangement = Arrangement.spacedBy(AiFitSpacing.sm),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Tendencia de peso",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Icon(
                    imageVector = Icons.Rounded.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp),
                )
            }

            LineChartView(
                entries = entries.map { log ->
                    ChartEntry(
                        label = log.date.format(DateTimeFormatter.ofPattern("dd/MM")),
                        value = log.weight.toFloat(),
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
            )
        }
    }
}

// ── Helpers ──────────────────────────────────────────────────────────────────

private fun streakLabel(type: StreakType): String = when (type) {
    StreakType.WORKOUT -> "Entrenamiento"
    StreakType.NUTRITION -> "Nutrición"
    StreakType.COMBINED -> "Combinada"
    StreakType.LOGIN -> "Conexión"
    StreakType.UNKNOWN -> "Racha"
}

private fun StreakStatus.toBadgeStatus(): BadgeStreakStatus = when (this) {
    StreakStatus.ACTIVE -> BadgeStreakStatus.ACTIVE
    StreakStatus.FROZEN -> BadgeStreakStatus.FROZEN
    StreakStatus.BROKEN -> BadgeStreakStatus.BROKEN
    StreakStatus.UNKNOWN -> BadgeStreakStatus.BROKEN
}

// ── Preview ──────────────────────────────────────────────────────────────────

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "HomeScreen Dark",
)
@Composable
private fun HomeScreenPreview() {
    AIFitTheme(darkTheme = true) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
        ) {
            HomeContent(
                state = HomeUiState.Success(
                    userName = "Carlos",
                    avatarUrl = null,
                    todayTraining = TodayTrainingState(
                        planId = "1",
                        planName = "Full Body Strength",
                        dayName = "Día 1 — Pecho & Espalda",
                        exerciseCount = 6,
                        adherencePercentage = 75f,
                    ),
                    todayNutrition = TodayNutritionState(
                        caloriesConsumed = 1450,
                        calorieTarget = 2200,
                        proteinConsumed = 90.0,
                        proteinTarget = 130.0,
                        carbsConsumed = 120.0,
                        carbsTarget = 250.0,
                        fatConsumed = 45.0,
                        fatTarget = 80.0,
                    ),
                    streaks = listOf(
                        Streak(
                            type = StreakType.WORKOUT,
                            status = StreakStatus.ACTIVE,
                            currentCount = 12,
                            longestCount = 15,
                            lastActivityDate = LocalDate.now(),
                            startedAt = "",
                        ),
                        Streak(
                            type = StreakType.NUTRITION,
                            status = StreakStatus.ACTIVE,
                            currentCount = 8,
                            longestCount = 20,
                            lastActivityDate = LocalDate.now(),
                            startedAt = "",
                        ),
                    ),
                    weeklySummary = WeeklyProgressSummary(
                        workoutsThisWeek = 3,
                        workoutsTarget = 4,
                        averageCaloriesToday = 2100.0,
                        calorieTarget = 2200,
                        currentStreak = 12,
                        bodyWeight = 78.5,
                    ),
                    weightEntries = listOf(
                        BodyWeightLog("1", 79.0, LocalDate.now().minusDays(6), null, LocalDate.now()),
                        BodyWeightLog("2", 78.8, LocalDate.now().minusDays(5), null, LocalDate.now()),
                        BodyWeightLog("3", 78.5, LocalDate.now().minusDays(4), null, LocalDate.now()),
                        BodyWeightLog("4", 78.7, LocalDate.now().minusDays(3), null, LocalDate.now()),
                        BodyWeightLog("5", 78.3, LocalDate.now().minusDays(2), null, LocalDate.now()),
                        BodyWeightLog("6", 78.1, LocalDate.now().minusDays(1), null, LocalDate.now()),
                        BodyWeightLog("7", 78.5, LocalDate.now(), null, LocalDate.now()),
                    ),
                ),
                onStartSession = {},
                onLogMeal = {},
                onProgressDashboard = {},
                onBodyWeight = {},
                onStreakTap = {},
                onProfile = {},
                onCreatePlan = {},
            )
        }
    }
}




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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.TrendingDown
import androidx.compose.material.icons.automirrored.rounded.TrendingFlat
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.MonitorWeight
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
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
import com.jlsh.aifit.core.ui.components.display.StreakBadge
import com.jlsh.aifit.core.ui.components.display.UserAvatar
import com.jlsh.aifit.core.ui.components.feedback.LoadingScreen
import com.jlsh.aifit.core.ui.theme.AIFitTheme
import com.jlsh.aifit.core.ui.theme.AiFitSpacing
import com.jlsh.aifit.feature.gamification.domain.model.AchievementDefinition
import com.jlsh.aifit.feature.gamification.domain.model.Streak
import com.jlsh.aifit.feature.gamification.domain.model.StreakStatus
import com.jlsh.aifit.feature.gamification.domain.model.StreakType
import com.jlsh.aifit.feature.gamification.domain.model.UserAchievement
import com.jlsh.aifit.feature.home.ui.components.LogWeightSheet
import com.jlsh.aifit.feature.home.ui.state.HomeUiEvent
import com.jlsh.aifit.feature.home.ui.state.HomeUiState
import com.jlsh.aifit.feature.home.ui.state.ActivePlanSummary
import com.jlsh.aifit.feature.home.ui.state.NextMealState
import com.jlsh.aifit.feature.home.ui.state.TodayNutritionState
import com.jlsh.aifit.feature.home.ui.state.TodayTrainingState
import com.jlsh.aifit.feature.progress.domain.model.BodyWeightLog
import com.jlsh.aifit.feature.progress.domain.model.WeeklyProgressSummary
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import com.jlsh.aifit.core.ui.components.display.StreakStatus as BadgeStreakStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToWorkoutSession: (planId: String, dayId: String) -> Unit,
    onNavigateToTrackMeal: () -> Unit,
    onNavigateToProgressDashboard: () -> Unit,
    onNavigateToBodyWeight: () -> Unit,
    onNavigateToGamification: (tab: String) -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToGeneratePlan: () -> Unit,
    onNavigateToTrainingDetail: (planId: String) -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val lifecycleOwner = LocalLifecycleOwner.current

    var showWeightSheet by rememberSaveable { mutableStateOf(false) }
    val weightSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            viewModel.onResumed()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is HomeUiEvent.NavigateToWorkoutSession ->
                    onNavigateToWorkoutSession(event.planId, event.dayId)
                is HomeUiEvent.NavigateToWorkoutLog -> Unit
                is HomeUiEvent.NavigateToTrainingDetail -> onNavigateToTrainingDetail(event.planId)
                is HomeUiEvent.NavigateToTrackMeal -> onNavigateToTrackMeal()
                is HomeUiEvent.NavigateToProgressDashboard -> onNavigateToProgressDashboard()
                is HomeUiEvent.NavigateToBodyWeight -> onNavigateToBodyWeight()
                is HomeUiEvent.NavigateToGamification -> onNavigateToGamification(event.tab)
                is HomeUiEvent.NavigateToProfile -> onNavigateToProfile()
                is HomeUiEvent.NavigateToGeneratePlan -> onNavigateToGeneratePlan()
                is HomeUiEvent.ShowLogWeightSheet -> showWeightSheet = true
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
                LoadingScreen()
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
                        text = "REINTENTAR",
                        onClick = { viewModel.loadAll() },
                    )
                }
            }

            is HomeUiState.Success -> {
                HomeContent(
                    state = state,
                    onStartSession = viewModel::onStartSession,
                    onViewDetail = viewModel::onViewTrainingDetail,
                    onLogMeal = viewModel::onLogMeal,
                    onLogWeight = viewModel::onLogWeight,
                    onProgressDashboard = viewModel::onProgressDashboard,
                    onBodyWeight = viewModel::onBodyWeight,
                    onStreakTap = { viewModel.onGamification("ACHIEVEMENTS") },
                    onProfile = viewModel::onProfile,
                    onCreatePlan = viewModel::onCreatePlan,
                )
            }
        }
    }

    if (showWeightSheet) {
        LogWeightSheet(
            sheetState = weightSheetState,
            onDismiss = { showWeightSheet = false },
            onConfirm = { weight ->
                showWeightSheet = false
                viewModel.onSaveWeight(weight)
            },
        )
    }
}

// ── Content ──────────────────────────────────────────────────────────────────

@Composable
private fun HomeContent(
    state: HomeUiState.Success,
    onStartSession: (String) -> Unit,
    onViewDetail: (String) -> Unit,
    onLogMeal: () -> Unit,
    onLogWeight: () -> Unit,
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
            top = AiFitSpacing.sm,
            bottom = 88.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(AiFitSpacing.md),
    ) {
        item(key = "greeting") {
            GreetingHeader(
                userName = state.userName,
                avatarUrl = state.avatarUrl,
                onProfileClick = onProfile,
            )
        }

        item(key = "training") {
            TodayTrainingCard(
                training = state.todayTraining,
                activePlan = state.activePlan,
                onStartSession = onStartSession,
                onViewDetail = onViewDetail,
                onCreatePlan = onCreatePlan,
            )
        }

        // ── Motivation card (BUG-026) ──
        if (state.trainingStreakDays > 0 || state.lastAchievement != null || state.nextAchievement != null) {
            item(key = "motivation") {
                MotivationCard(
                    streakDays = state.trainingStreakDays,
                    lastAchievement = state.lastAchievement,
                    nextAchievement = state.nextAchievement,
                    onTap = onStreakTap,
                )
            }
        }

        item(key = "nutrition") {
            TodayNutritionCard(
                nutrition = state.todayNutrition,
                onLogMeal = onLogMeal,
            )
        }

        item(key = "next_meal") {
            val upcoming = state.nextMeal
            if (upcoming is NextMealState.Upcoming) {
                NextMealCard(
                    nextMeal = upcoming,
                    onLogMeal = onLogMeal,
                )
            }
        }

        // Current weight card — always visible when there's at least one entry
        item(key = "current_weight") {
            CurrentWeightCard(
                weightEntries = state.weightEntries,
                onLogWeight = onLogWeight,
                onTap = onBodyWeight,
            )
        }

        if (state.streaks.isNotEmpty()) {
            item(key = "streaks") {
                StreaksCard(
                    streaks = state.streaks,
                    onTap = onStreakTap,
                )
            }
        }

        if (state.weeklySummary != null) {
            item(key = "weekly") {
                WeeklyProgressCard(
                    summary = state.weeklySummary,
                    weightEntries = state.weightEntries,
                    onTap = onProgressDashboard,
                    onLogWeight = onLogWeight,
                )
            }
        }

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

private val dateFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM", Locale("es"))

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
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(AiFitSpacing.xs),
        ) {
            Text(
                text = "${HomeViewModel.greetingForTime()}, $userName",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = LocalDate.now().format(dateFormatter)
                    .replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(modifier = Modifier.width(AiFitSpacing.md))
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
    activePlan: ActivePlanSummary?,
    onStartSession: (String) -> Unit,
    onViewDetail: (String) -> Unit,
    onCreatePlan: () -> Unit,
) {
    AiFitCard(
        containerColor = MaterialTheme.colorScheme.secondaryContainer,
    ) {
        Column(
            modifier = Modifier.padding(AiFitSpacing.md),
            verticalArrangement = Arrangement.spacedBy(AiFitSpacing.sm),
        ) {
            // Section header
            SectionTitle(
                icon = Icons.Rounded.FitnessCenter,
                title = "ENTRENAMIENTO DE HOY",
            )

            when {
                // ── Case 1: Active plan + workout today ──────────────────────
                training != null -> {
                    Spacer(modifier = Modifier.height(AiFitSpacing.xs))

                    Text(
                        text = training.planName,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = "${training.dayName}  ·  ${training.exerciseCount} ejercicios",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    // Exercise list
                    if (training.exerciseNames.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(AiFitSpacing.xs))
                        Column(verticalArrangement = Arrangement.spacedBy(AiFitSpacing.xs)) {
                            training.exerciseNames.forEach { name ->
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(AiFitSpacing.sm),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(4.dp)
                                            .background(
                                                color = MaterialTheme.colorScheme.primaryContainer,
                                                shape = RoundedCornerShape(2.dp),
                                            ),
                                    )
                                    Text(
                                        text = name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface,
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(AiFitSpacing.xs))
                    AdherenceBar(percentage = training.adherencePercentage)
                    Spacer(modifier = Modifier.height(AiFitSpacing.xs))

                    if (training.isCompleted) {
                        // ── Completed banner ──
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(8.dp),
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(
                                        horizontal = AiFitSpacing.md,
                                        vertical = AiFitSpacing.sm,
                                    ),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(AiFitSpacing.sm),
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.CheckCircle,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primaryContainer,
                                    modifier = Modifier.size(24.dp),
                                )
                                Column {
                                    Text(
                                        text = "Completado",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = MaterialTheme.colorScheme.primaryContainer,
                                    )
                                    Text(
                                        text = "${training.exerciseCount} ejercicios",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(AiFitSpacing.xs))
                        SecondaryButton(
                            text = "VER DETALLE",
                            onClick = { onViewDetail(training.planId) },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    } else {
                        // ── Active: two symmetric buttons ──
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(AiFitSpacing.sm),
                        ) {
                            PrimaryButton(
                                text = "COMENZAR SESIÓN",
                                onClick = { onStartSession(training.planId) },
                                modifier = Modifier.weight(1f),
                            )
                            SecondaryButton(
                                text = "VER DETALLE",
                                onClick = { onViewDetail(training.planId) },
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }

                // ── Case 2: Active plan + rest day today ─────────────────────
                activePlan != null -> {
                    Spacer(modifier = Modifier.height(AiFitSpacing.xs))
                    Text(
                        text = activePlan.name,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = "Hoy toca descanso 💤",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = "El descanso es parte esencial del progreso. ¡Descansa bien!",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(AiFitSpacing.xs))
                    SecondaryButton(
                        text = "VER PLAN",
                        onClick = { onViewDetail(activePlan.id) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                // ── Case 3: No active plan ────────────────────────────────────
                else -> {
                    Spacer(modifier = Modifier.height(AiFitSpacing.xs))
                    Text(
                        text = "No tienes un plan de entrenamiento activo",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(AiFitSpacing.xs))
                    SecondaryButton(
                        text = "CREAR PLAN",
                        onClick = onCreatePlan,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
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
    AiFitCard (
        containerColor = MaterialTheme.colorScheme.secondaryContainer
    ){
        Column(
            modifier = Modifier.padding(AiFitSpacing.md),
            verticalArrangement = Arrangement.spacedBy(AiFitSpacing.sm),
        ) {
            SectionTitle(
                icon = Icons.Rounded.Restaurant,
                title = "NUTRICIÓN DE HOY",
            )

            if (nutrition != null && (nutrition.caloriesConsumed > 0 || nutrition.calorieTarget > 0)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                ) {
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
                        size = 120.dp,
                        strokeWidth = 10.dp,
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(AiFitSpacing.xs)) {
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
                }

                Spacer(modifier = Modifier.height(AiFitSpacing.xs))

                PrimaryButton(
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
                Spacer(modifier = Modifier.height(AiFitSpacing.xs))
                PrimaryButton(
                    text = "REGISTRAR COMIDA",
                    onClick = onLogMeal,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

// ── 4. Next Meal ─────────────────────────────────────────────────────────────

@Composable
private fun NextMealCard(
    nextMeal: NextMealState.Upcoming,
    onLogMeal: () -> Unit,
) {
    AiFitCard(onClick = onLogMeal, containerColor = MaterialTheme.colorScheme.secondaryContainer) {
        Column(
            modifier = Modifier.padding(AiFitSpacing.md),
            verticalArrangement = Arrangement.spacedBy(AiFitSpacing.sm),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(AiFitSpacing.sm),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Schedule,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(18.dp),
                    )
                    Text(
                        text = "Próxima comida",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
                Text(
                    text = nextMeal.estimatedTime,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Text(
                text = nextMeal.mealName,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "${nextMeal.calories} kcal",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primaryContainer,
                )
                Text(
                    text = "${nextMeal.proteinG.toInt()}g P  ·  ${nextMeal.carbsG.toInt()}g C  ·  ${nextMeal.fatG.toInt()}g G",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

// ── 5. Streaks ───────────────────────────────────────────────────────────────

// ── 4b. Motivation (BUG-026) ─────────────────────────────────────────────────

@Composable
private fun MotivationCard(
    streakDays: Int,
    lastAchievement: UserAchievement?,
    nextAchievement: AchievementDefinition?,
    onTap: () -> Unit,
) {
    AiFitCard(onClick = onTap, containerColor = MaterialTheme.colorScheme.secondaryContainer) {
        Column(
            modifier = Modifier.padding(AiFitSpacing.md),
            verticalArrangement = Arrangement.spacedBy(AiFitSpacing.sm),
        ) {
            SectionTitle(
                icon = Icons.Rounded.EmojiEvents,
                title = "MOTIVACIÓN",
            )

            // Streak row
            if (streakDays > 0) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(AiFitSpacing.sm),
                ) {
                    Text(
                        text = "🔥",
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(
                        text = "$streakDays días seguidos entrenando",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }

            // Last achievement
            if (lastAchievement != null) {
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant,
                    thickness = 0.5.dp,
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(AiFitSpacing.sm),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.EmojiEvents,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(20.dp),
                    )
                    Column {
                        Text(
                            text = "Último logro: ${lastAchievement.achievement.name}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = lastAchievement.unlockedAt.take(10),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            // Next achievement
            if (nextAchievement != null) {
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant,
                    thickness = 0.5.dp,
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(AiFitSpacing.sm),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp),
                    )
                    Column {
                        Text(
                            text = "Próximo: ${nextAchievement.name}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = nextAchievement.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                        )
                    }
                }
            }
        }
    }
}

// ── 5a. Streaks ──────────────────────────────────────────────────────────────

@Composable
private fun StreaksCard(
    streaks: List<Streak>,
    onTap: () -> Unit,
) {
    AiFitCard(onClick = onTap, containerColor = MaterialTheme.colorScheme.secondaryContainer) {
        Column(
            modifier = Modifier.padding(AiFitSpacing.md),
            verticalArrangement = Arrangement.spacedBy(AiFitSpacing.sm),
        ) {
            Text(
                text = "RACHAS",
                style = MaterialTheme.typography.labelSmall,
                letterSpacing = 1.5.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AiFitSpacing.xl),
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
    }
}

// ── 5b. Current Weight ────────────────────────────────────────────────────────

@Composable
private fun CurrentWeightCard(
    weightEntries: List<BodyWeightLog>,
    onLogWeight: () -> Unit,
    onTap: () -> Unit,
) {
    val currentWeight = weightEntries.lastOrNull()?.weight
    val previousWeight = weightEntries.dropLast(1).lastOrNull()?.weight
    val weightDelta = if (currentWeight != null && previousWeight != null) {
        currentWeight - previousWeight
    } else null

    AiFitCard(
        onClick = if (currentWeight != null) onTap else null,
        containerColor = MaterialTheme.colorScheme.secondaryContainer,
    ) {
        Column(
            modifier = Modifier.padding(AiFitSpacing.md),
            verticalArrangement = Arrangement.spacedBy(AiFitSpacing.sm),
        ) {
            SectionTitle(
                icon = Icons.Rounded.MonitorWeight,
                title = "MI PESO",
            )

            if (currentWeight != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom,
                ) {
                    Text(
                        text = "${"%.1f".format(currentWeight)} kg",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    if (weightDelta != null) {
                        val deltaText = if (weightDelta >= 0) {
                            "+${"%.1f".format(weightDelta)} kg"
                        } else {
                            "${"%.1f".format(weightDelta)} kg"
                        }
                        val trendIcon = when {
                            weightDelta < -0.05 -> Icons.AutoMirrored.Rounded.TrendingDown
                            weightDelta > 0.05 -> Icons.AutoMirrored.Rounded.TrendingUp
                            else -> Icons.AutoMirrored.Rounded.TrendingFlat
                        }
                        val trendColor = when {
                            weightDelta < -0.05 -> MaterialTheme.colorScheme.primaryContainer
                            weightDelta > 0.05 -> MaterialTheme.colorScheme.error
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(AiFitSpacing.xs),
                        ) {
                            Icon(
                                imageVector = trendIcon,
                                contentDescription = null,
                                tint = trendColor,
                                modifier = Modifier.size(18.dp),
                            )
                            Text(
                                text = deltaText,
                                style = MaterialTheme.typography.bodyMedium,
                                color = trendColor,
                            )
                        }
                    }
                }

                val lastDate = weightEntries.lastOrNull()?.date
                if (lastDate != null) {
                    Text(
                        text = "Último registro: ${lastDate.format(DateTimeFormatter.ofPattern("d MMM", Locale("es")))}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                Text(
                    text = "Aún no has registrado tu peso",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onLogWeight)
                    .padding(vertical = AiFitSpacing.xs),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AiFitSpacing.sm),
            ) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(18.dp),
                )
                Text(
                    text = "Registrar peso",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primaryContainer,
                )
            }
        }
    }
}

// ── 6. Weekly Progress ───────────────────────────────────────────────────────

@Composable
private fun WeeklyProgressCard(
    summary: WeeklyProgressSummary,
    weightEntries: List<BodyWeightLog>,
    onTap: () -> Unit,
    onLogWeight: () -> Unit,
) {
    val currentWeight = weightEntries.lastOrNull()?.weight
    val previousWeight = weightEntries.dropLast(1).lastOrNull()?.weight
    val weightDelta = if (currentWeight != null && previousWeight != null) {
        currentWeight - previousWeight
    } else null

    AiFitCard(onClick = onTap, containerColor = MaterialTheme.colorScheme.secondaryContainer) {
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
                    text = "PROGRESO SEMANAL",
                    style = MaterialTheme.typography.labelSmall,
                    letterSpacing = 1.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Icon(
                    imageVector = Icons.Rounded.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp),
                )
            }

            // Training adherence
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

            val trainingAdherence = if (summary.workoutsTarget > 0) {
                (summary.workoutsThisWeek.toFloat() / summary.workoutsTarget * 100f)
                    .coerceIn(0f, 100f)
            } else 0f
            AdherenceBar(percentage = trainingAdherence)

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant,
                thickness = 0.5.dp,
            )

            // Nutrition adherence
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
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }

            val nutritionAdherence = if (summary.calorieTarget > 0) {
                (summary.averageCaloriesToday.toFloat() / summary.calorieTarget * 100f)
                    .coerceIn(0f, 100f)
            } else 0f
            AdherenceBar(percentage = nutritionAdherence)

            // Weight row
            if (currentWeight != null) {
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant,
                    thickness = 0.5.dp,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(AiFitSpacing.sm),
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.MonitorWeight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp),
                        )
                        Text(
                            text = "${"%.1f".format(currentWeight)} kg",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                    if (weightDelta != null) {
                        val deltaText = if (weightDelta >= 0) {
                            "+${"%.1f".format(weightDelta)} kg"
                        } else {
                            "${"%.1f".format(weightDelta)} kg"
                        }
                        val trendIcon = when {
                            weightDelta < -0.05 -> Icons.AutoMirrored.Rounded.TrendingDown
                            weightDelta > 0.05 -> Icons.AutoMirrored.Rounded.TrendingUp
                            else -> Icons.AutoMirrored.Rounded.TrendingFlat
                        }
                        val trendColor = when {
                            weightDelta < -0.05 -> MaterialTheme.colorScheme.primaryContainer
                            weightDelta > 0.05 -> MaterialTheme.colorScheme.error
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(AiFitSpacing.xs),
                        ) {
                            Icon(
                                imageVector = trendIcon,
                                contentDescription = null,
                                tint = trendColor,
                                modifier = Modifier.size(16.dp),
                            )
                            Text(
                                text = deltaText,
                                style = MaterialTheme.typography.labelMedium,
                                color = trendColor,
                            )
                        }
                    }
                }
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant,
                thickness = 0.5.dp,
            )

            // Log weight row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onLogWeight)
                    .padding(vertical = AiFitSpacing.xs),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AiFitSpacing.sm),
            ) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(18.dp),
                )
                Text(
                    text = "Registrar peso",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primaryContainer,
                )
            }
        }
    }
}

// ── 7. Weight Trend ──────────────────────────────────────────────────────────

@Composable
private fun WeightTrendCard(
    entries: List<BodyWeightLog>,
    onTap: () -> Unit,
) {
    AiFitCard(onClick = onTap, containerColor = MaterialTheme.colorScheme.secondaryContainer) {
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
                    text = "TENDENCIA DE PESO",
                    style = MaterialTheme.typography.labelSmall,
                    letterSpacing = 1.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
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

// ── Shared ───────────────────────────────────────────────────────────────────

@Composable
private fun SectionTitle(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AiFitSpacing.sm),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(16.dp),
        )
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            letterSpacing = 1.5.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

// ── Helpers ──────────────────────────────────────────────────────────────────

private fun streakLabel(type: StreakType): String = when (type) {
    StreakType.TRAINING -> "Entrenamiento"
    StreakType.NUTRITION -> "Nutrición"
    StreakType.COMBINED -> "Combinada"
    StreakType.UNKNOWN -> "Racha"
}

private fun StreakStatus.toBadgeStatus(): BadgeStreakStatus = when (this) {
    StreakStatus.ACTIVE -> BadgeStreakStatus.ACTIVE
    StreakStatus.BROKEN -> BadgeStreakStatus.BROKEN
    StreakStatus.RECOVERING -> BadgeStreakStatus.FROZEN
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
                        dayId = "d1",
                        planName = "Full Body Strength",
                        dayName = "Día 1 — Pecho & Espalda",
                        exerciseCount = 6,
                        exerciseNames = listOf(
                            "Press banca",
                            "Remo con barra",
                            "Sentadilla",
                            "Press militar",
                        ),
                        adherencePercentage = 75f,
                        isCompleted = false,
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
                    nextMeal = NextMealState.Upcoming(
                        mealName = "Pollo a la plancha con arroz",
                        estimatedTime = "13:00",
                        calories = 650,
                        proteinG = 45.0,
                        carbsG = 70.0,
                        fatG = 15.0,
                    ),
                    streaks = listOf(
                        Streak(
                            type = StreakType.TRAINING,
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
                onViewDetail = {},
                onLogMeal = {},
                onLogWeight = {},
                onProgressDashboard = {},
                onBodyWeight = {},
                onStreakTap = {},
                onProfile = {},
                onCreatePlan = {},
            )
        }
    }
}




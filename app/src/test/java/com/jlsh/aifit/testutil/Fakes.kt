package com.jlsh.aifit.testutil

import com.jlsh.aifit.feature.auth.data.dto.AuthResponseDto
import com.jlsh.aifit.feature.auth.domain.model.AuthToken
import com.jlsh.aifit.feature.chat.data.dto.ChatMessageResponseDto
import com.jlsh.aifit.feature.chat.data.dto.ChatSessionResponseDto
import com.jlsh.aifit.feature.chat.data.dto.ChatSessionSummaryResponseDto
import com.jlsh.aifit.feature.chat.data.local.ChatMessageEntity
import com.jlsh.aifit.feature.chat.data.local.ChatSessionEntity
import com.jlsh.aifit.feature.chat.domain.model.ChatMessage
import com.jlsh.aifit.feature.chat.domain.model.ChatMessageRole
import com.jlsh.aifit.feature.chat.domain.model.ChatSession
import com.jlsh.aifit.feature.chat.domain.model.ChatSessionStatus
import com.jlsh.aifit.feature.metabolic.data.dto.ApplyMetabolicAdjustmentRequestDto
import com.jlsh.aifit.feature.metabolic.data.dto.MetabolicAdjustmentRecommendationResponseDto
import com.jlsh.aifit.feature.metabolic.data.dto.MetabolicAnalysisResponseDto
import com.jlsh.aifit.feature.metabolic.data.dto.MetabolicInsightResponseDto
import com.jlsh.aifit.feature.metabolic.data.dto.WeightTrendResponseDto
import com.jlsh.aifit.feature.metabolic.domain.model.AdjustmentMagnitude
import com.jlsh.aifit.feature.metabolic.domain.model.AdjustmentType
import com.jlsh.aifit.feature.metabolic.domain.model.AdjustmentUrgency
import com.jlsh.aifit.feature.metabolic.domain.model.MetabolicAdjustmentRecommendation
import com.jlsh.aifit.feature.metabolic.domain.model.MetabolicAnalysis
import com.jlsh.aifit.feature.metabolic.domain.model.MetabolicInsight
import com.jlsh.aifit.feature.metabolic.domain.model.MetabolicStatus
import com.jlsh.aifit.feature.metabolic.domain.model.WeightTrend as MetabolicWeightTrend
import com.jlsh.aifit.feature.user.data.dto.UserProfileResponseDto
import com.jlsh.aifit.feature.user.data.local.UserProfileEntity
import com.jlsh.aifit.feature.user.domain.model.ActivityLevel
import com.jlsh.aifit.feature.user.domain.model.CreateUserProfileRequest
import com.jlsh.aifit.feature.user.domain.model.DietPreference
import com.jlsh.aifit.feature.user.domain.model.FitnessLevel
import com.jlsh.aifit.feature.user.domain.model.Gender
import com.jlsh.aifit.feature.user.domain.model.GoalType
import com.jlsh.aifit.feature.user.domain.model.UpdateUserProfileRequest
import com.jlsh.aifit.feature.user.domain.model.UserProfile
import com.jlsh.aifit.feature.user.domain.model.WorkoutLocation

// ─── Training imports ──────────────────────────────────────────────────────────
import com.jlsh.aifit.feature.training.data.dto.TrainingDayResponseDto
import com.jlsh.aifit.feature.training.data.dto.TrainingExerciseResponseDto
import com.jlsh.aifit.feature.training.data.dto.TrainingPlanResponseDto
import com.jlsh.aifit.feature.training.data.dto.TrainingPlanSummaryResponseDto
import com.jlsh.aifit.feature.training.data.dto.GenerateTrainingPlanRequestDto
import com.jlsh.aifit.feature.training.data.local.TrainingPlanEntity
import com.jlsh.aifit.feature.training.domain.model.MuscleGroup
import com.jlsh.aifit.feature.training.domain.model.PlanStatus
import com.jlsh.aifit.feature.training.domain.model.TrainingDay
import com.jlsh.aifit.feature.training.domain.model.TrainingDayType
import com.jlsh.aifit.feature.training.domain.model.TrainingExercise
import com.jlsh.aifit.feature.training.domain.model.TrainingPlan
import java.time.LocalDateTime

// ─── Diet imports ──────────────────────────────────────────────────────────────
import com.jlsh.aifit.feature.diet.data.dto.DietDayResponseDto
import com.jlsh.aifit.feature.diet.data.dto.DietPlanResponseDto
import com.jlsh.aifit.feature.diet.data.dto.DietPlanSummaryResponseDto
import com.jlsh.aifit.feature.diet.data.dto.GenerateDietPlanRequestDto
import com.jlsh.aifit.feature.diet.data.dto.MealItemResponseDto
import com.jlsh.aifit.feature.diet.data.dto.MealResponseDto
import com.jlsh.aifit.feature.diet.data.local.DietPlanEntity
import com.jlsh.aifit.feature.diet.domain.model.DietDay
import com.jlsh.aifit.feature.diet.domain.model.DietPlan
import com.jlsh.aifit.feature.diet.domain.model.Meal
import com.jlsh.aifit.feature.diet.domain.model.MealItem
import com.jlsh.aifit.feature.diet.domain.model.MealType

// ─── Nutrition imports ─────────────────────────────────────────────────────────
import com.jlsh.aifit.feature.nutrition.data.dto.AnalyzeMealFromTextRequestDto
import com.jlsh.aifit.feature.nutrition.data.dto.FoodItemLogResponseDto
import com.jlsh.aifit.feature.nutrition.data.dto.MealLogResponseDto
import com.jlsh.aifit.feature.nutrition.data.dto.NutritionLogResponseDto
import com.jlsh.aifit.feature.nutrition.data.dto.NutritionTargetResponseDto
import com.jlsh.aifit.feature.nutrition.data.dto.TrackFoodItemRequestDto
import com.jlsh.aifit.feature.nutrition.data.dto.TrackMealRequestDto
import com.jlsh.aifit.feature.nutrition.data.dto.UpdateNutritionTargetRequestDto
import com.jlsh.aifit.feature.nutrition.data.local.NutritionLogEntity
import com.jlsh.aifit.feature.nutrition.domain.model.FoodItemLog
import com.jlsh.aifit.feature.nutrition.domain.model.MealLog
import com.jlsh.aifit.feature.nutrition.domain.model.NutritionLog
import com.jlsh.aifit.feature.nutrition.domain.model.NutritionTarget
import com.jlsh.aifit.feature.nutrition.domain.model.TargetSource
import java.time.LocalDate

// ─── Progress imports ──────────────────────────────────────────────────────────
import com.jlsh.aifit.feature.progress.data.dto.BestSetResponseDto
import com.jlsh.aifit.feature.progress.data.dto.BodyWeightLogResponseDto
import com.jlsh.aifit.feature.progress.data.dto.LogBodyWeightRequestDto
import com.jlsh.aifit.feature.progress.data.dto.NutritionAdherenceResponseDto
import com.jlsh.aifit.feature.progress.data.dto.PeriodResponseDto
import com.jlsh.aifit.feature.progress.data.dto.ProgressDashboardResponseDto
import com.jlsh.aifit.feature.progress.data.dto.StrengthProgressResponseDto
import com.jlsh.aifit.feature.progress.data.dto.WeeklyProgressSummaryResponseDto
import com.jlsh.aifit.feature.progress.data.dto.WeightEntryDto
import com.jlsh.aifit.feature.progress.data.dto.WeightProgressResponseDto
import com.jlsh.aifit.feature.progress.data.dto.WorkoutAdherenceResponseDto
import com.jlsh.aifit.feature.progress.data.local.BodyWeightEntity
import com.jlsh.aifit.feature.progress.domain.model.BodyWeightLog
import com.jlsh.aifit.feature.progress.domain.model.NutritionAdherence
import com.jlsh.aifit.feature.progress.domain.model.ProgressDashboard
import com.jlsh.aifit.feature.progress.domain.model.StrengthProgress
import com.jlsh.aifit.feature.progress.domain.model.WeeklyProgressSummary
import com.jlsh.aifit.feature.progress.domain.model.WeightEntry
import com.jlsh.aifit.feature.progress.domain.model.WeightProgress
import com.jlsh.aifit.feature.progress.domain.model.WeightTrend
import com.jlsh.aifit.feature.progress.domain.model.WorkoutAdherence

// ─── Workout imports ───────────────────────────────────────────────────────────
import com.jlsh.aifit.feature.workout.domain.model.WorkoutLog

// ─── Education imports ─────────────────────────────────────────────────────────
import com.jlsh.aifit.feature.education.data.dto.ContextualExplanationResponseDto
import com.jlsh.aifit.feature.education.data.dto.GlossaryDefinitionResponseDto
import com.jlsh.aifit.feature.education.data.dto.WhyThisResponseDto
import com.jlsh.aifit.feature.education.domain.model.ContextualExplanation
import com.jlsh.aifit.feature.education.domain.model.ExplanationReferenceType
import com.jlsh.aifit.feature.education.domain.model.GlossaryDefinition
import com.jlsh.aifit.feature.education.domain.model.KnowledgeLevel
import com.jlsh.aifit.feature.education.domain.model.WhyThisExplanation

// ─── Progression imports ───────────────────────────────────────────────────────
import com.jlsh.aifit.feature.progression.data.dto.PlanProgressionSummaryResponseDto
import com.jlsh.aifit.feature.progression.data.dto.ProgressionRecommendationResponseDto
import com.jlsh.aifit.feature.progression.domain.model.PlanProgressionSummary
import com.jlsh.aifit.feature.progression.domain.model.ProgressTrend
import com.jlsh.aifit.feature.progression.domain.model.ProgressionRecommendation
import com.jlsh.aifit.feature.progression.domain.model.ProgressionType

// ─── Gamification imports ──────────────────────────────────────────────────────
import com.jlsh.aifit.feature.gamification.data.dto.AchievementDefinitionResponseDto
import com.jlsh.aifit.feature.gamification.data.dto.AchievementExportEntryDto
import com.jlsh.aifit.feature.gamification.data.dto.ExerciseProgressionExportDto
import com.jlsh.aifit.feature.gamification.data.dto.PersonalRecordResponseDto
import com.jlsh.aifit.feature.gamification.data.dto.ProgressExportResponseDto
import com.jlsh.aifit.feature.gamification.data.dto.StreakExportSummaryDto
import com.jlsh.aifit.feature.gamification.data.dto.StreakResponseDto
import com.jlsh.aifit.feature.gamification.data.dto.UserAchievementResponseDto
import com.jlsh.aifit.feature.gamification.data.dto.WeeklyAdherenceExportDto
import com.jlsh.aifit.feature.gamification.data.dto.WeightSummaryExportDto
import com.jlsh.aifit.feature.gamification.domain.model.AchievementDefinition
import com.jlsh.aifit.feature.gamification.domain.model.AchievementRarity
import com.jlsh.aifit.feature.gamification.domain.model.AchievementType
import com.jlsh.aifit.feature.gamification.domain.model.PersonalRecord
import com.jlsh.aifit.feature.gamification.domain.model.ProgressExport
import com.jlsh.aifit.feature.gamification.domain.model.Streak
import com.jlsh.aifit.feature.gamification.domain.model.StreakStatus
import com.jlsh.aifit.feature.gamification.domain.model.StreakType
import com.jlsh.aifit.feature.gamification.domain.model.UserAchievement

// ─── Constantes base ───────────────────────────────────────────────────────────
/** A valid-looking JWT for test purposes (not a real token). */
const val FAKE_TOKEN = "eyJhbGciOiJIUzI1NiJ9.test.signature"
const val FAKE_USER_ID = "user-test-001"
const val FAKE_EMAIL = "test@aifit.com"
const val FAKE_NAME = "Test User"

// ─── Auth ─────────────────────────────────────────────────────────────────────
fun fakeAuthToken(
    token: String = FAKE_TOKEN,
    userId: String = FAKE_USER_ID,
    email: String = FAKE_EMAIL,
    name: String = FAKE_NAME,
    expiresIn: Long = 3_600_000L,
    profileComplete: Boolean = true,
) = AuthToken(
    token = token,
    userId = userId,
    email = email,
    name = name,
    expiresIn = expiresIn,
    profileComplete = profileComplete,
)

fun fakeAuthResponseDto(
    token: String = FAKE_TOKEN,
    userId: String = FAKE_USER_ID,
    email: String = FAKE_EMAIL,
    name: String = FAKE_NAME,
    expiresIn: Long = 3_600_000L,
    profileComplete: Boolean = true,
) = AuthResponseDto(
    token = token,
    userId = userId,
    email = email,
    name = name,
    expiresIn = expiresIn,
    profileComplete = profileComplete,
)

// ─── User ─────────────────────────────────────────────────────────────────────
fun fakeUserProfile(
    id: String = "user-1",
    name: String = "Test User",
    email: String = FAKE_EMAIL,
    authProvider: String = "LOCAL",
    goalType: GoalType? = GoalType.LOSE_WEIGHT,
    fitnessLevel: FitnessLevel? = FitnessLevel.BEGINNER,
    activityLevel: ActivityLevel? = ActivityLevel.MODERATE,
    gender: Gender? = null,
    profilePictureUrl: String? = null,
) = UserProfile(
    id = id,
    name = name,
    email = email,
    authProvider = authProvider,
    profilePictureUrl = profilePictureUrl,
    birthDate = null,
    gender = gender,
    height = null,
    weight = null,
    targetWeight = null,
    goalType = goalType,
    activityLevel = activityLevel,
    fitnessLevel = fitnessLevel,
    workoutLocation = null,
    dietPreference = null,
    knowledgeLevel = null,
    weeklyWorkoutDays = null,
    availableMinutesPerSession = null,
    injuries = null,
    calorieTarget = null,
)

fun fakeUserProfileEntity(
    id: String = "me",
    name: String = "Test User",
    email: String = FAKE_EMAIL,
    goalType: String? = "LOSE_WEIGHT",
    fitnessLevel: String? = "BEGINNER",
    profilePictureUrl: String? = null,
) = UserProfileEntity(
    id = id,
    name = name,
    email = email,
    goalType = goalType,
    fitnessLevel = fitnessLevel,
    profilePictureUrl = profilePictureUrl,
)

fun fakeUserProfileResponseDto(
    id: String = "user-1",
    name: String = "Test User",
    email: String = FAKE_EMAIL,
    authProvider: String = "LOCAL",
    goalType: String? = "LOSE_WEIGHT",
    fitnessLevel: String? = "BEGINNER",
    activityLevel: String? = "MODERATE",
    gender: String? = null,
    birthDate: String? = null,
) = UserProfileResponseDto(
    id = id,
    name = name,
    email = email,
    authProvider = authProvider,
    profilePictureUrl = null,
    birthDate = birthDate,
    gender = gender,
    height = null,
    weight = null,
    targetWeight = null,
    goalType = goalType,
    activityLevel = activityLevel,
    fitnessLevel = fitnessLevel,
    preferredLocation = null,
    dietPreference = null,
    knowledgeLevel = null,
    weeklyWorkoutDays = null,
    availableMinutesPerSession = null,
    injuries = null,
    calorieTarget = null,
)

fun fakeCreateUserProfileRequest(
    goalType: GoalType? = GoalType.LOSE_WEIGHT,
    fitnessLevel: FitnessLevel? = FitnessLevel.BEGINNER,
    activityLevel: ActivityLevel? = ActivityLevel.MODERATE,
) = CreateUserProfileRequest(
    goalType = goalType,
    fitnessLevel = fitnessLevel,
    activityLevel = activityLevel,
)

fun fakeUpdateUserProfileRequest(
    goalType: GoalType? = GoalType.GAIN_MUSCLE,
    fitnessLevel: FitnessLevel? = FitnessLevel.INTERMEDIATE,
) = UpdateUserProfileRequest(
    goalType = goalType,
    fitnessLevel = fitnessLevel,
)

// ─── Training ──────────────────────────────────────────────────────────────────

private val DEFAULT_CREATED_AT = LocalDateTime.of(2026, 3, 1, 10, 0, 0)
private const val DEFAULT_CREATED_AT_STR = "2026-03-01T10:00:00"

fun fakeTrainingExercise(
    id: String = "exercise-1",
    name: String = "Bench Press",
    primaryMuscle: MuscleGroup = MuscleGroup.CHEST,
    secondaryMuscle: MuscleGroup? = MuscleGroup.TRICEPS,
    sets: Int = 4,
    repsMin: Int = 8,
    repsMax: Int = 12,
    restSeconds: Int = 90,
    order: Int = 1,
) = TrainingExercise(
    id = id, name = name, description = null,
    primaryMuscle = primaryMuscle, secondaryMuscle = secondaryMuscle,
    sets = sets, repsMin = repsMin, repsMax = repsMax,
    restSeconds = restSeconds, notes = null, order = order, targetRpe = null,
)

fun fakeTrainingDay(
    id: String = "day-1",
    dayNumber: Int = 1,
    name: String = "Push Day",
    estimatedDurationMinutes: Int = 60,
    exercises: List<TrainingExercise> = listOf(fakeTrainingExercise()),
    dayType: TrainingDayType = TrainingDayType.TRAINING,
) = TrainingDay(
    id = id, dayNumber = dayNumber, name = name,
    estimatedDurationMinutes = estimatedDurationMinutes,
    exercises = exercises, dayOfWeek = null, dayType = dayType,
)

fun fakeTrainingPlan(
    id: String = "plan-1",
    name: String = "Test Plan",
    status: PlanStatus = PlanStatus.ACTIVE,
    days: List<TrainingDay> = emptyList(),
    frequencyDaysPerWeek: Int = 4,
    durationWeeks: Int = 8,
    goalType: GoalType = GoalType.GAIN_MUSCLE,
    fitnessLevel: FitnessLevel = FitnessLevel.INTERMEDIATE,
    location: WorkoutLocation = WorkoutLocation.GYM,
    totalDays: Int = 32,
    createdAt: LocalDateTime = DEFAULT_CREATED_AT,
) = TrainingPlan(
    id = id, name = name, description = null,
    frequencyDaysPerWeek = frequencyDaysPerWeek,
    durationWeeks = durationWeeks,
    goalType = goalType, fitnessLevel = fitnessLevel,
    location = location, status = status,
    totalDays = totalDays, createdAt = createdAt,
    days = days,
)

fun fakeTrainingPlanEntity(
    id: String = "plan-1",
    userId: String = FAKE_USER_ID,
    name: String = "Test Plan",
    status: String = "ACTIVE",
) = TrainingPlanEntity(
    id = id, userId = userId, name = name,
    description = null, status = status,
    frequencyDaysPerWeek = 4, durationWeeks = 8,
    goalType = "GAIN_MUSCLE", fitnessLevel = "INTERMEDIATE",
    location = "GYM", totalDays = 32,
    createdAt = DEFAULT_CREATED_AT.toInstant(java.time.ZoneOffset.UTC).toEpochMilli(),
)

fun fakeTrainingPlanSummaryResponseDto(
    id: String = "plan-1",
    name: String = "Test Plan",
    status: String = "ACTIVE",
) = TrainingPlanSummaryResponseDto(
    id = id, name = name, description = null,
    frequencyDaysPerWeek = 4, durationWeeks = 8,
    goalType = "GAIN_MUSCLE", fitnessLevel = "INTERMEDIATE",
    location = "GYM", status = status, totalDays = 32,
    createdAt = DEFAULT_CREATED_AT_STR,
)

fun fakeTrainingExerciseResponseDto(
    id: String = "exercise-1",
    name: String = "Bench Press",
    primaryMuscle: String = "CHEST",
    secondaryMuscle: String? = "TRICEPS",
) = TrainingExerciseResponseDto(
    id = id, name = name, description = null,
    primaryMuscle = primaryMuscle, secondaryMuscle = secondaryMuscle,
    sets = 4, repsMin = 8, repsMax = 12,
    restSeconds = 90, notes = null, order = 1, targetRpe = null,
)

fun fakeTrainingDayResponseDto(
    id: String = "day-1",
    exercises: List<TrainingExerciseResponseDto> = listOf(fakeTrainingExerciseResponseDto()),
) = TrainingDayResponseDto(
    id = id, dayNumber = 1, name = "Push Day",
    estimatedDurationMinutes = 60, exercises = exercises,
    dayOfWeek = null, dayType = "TRAINING",
)

fun fakeTrainingPlanResponseDto(
    id: String = "plan-1",
    name: String = "Test Plan",
    status: String = "ACTIVE",
    days: List<TrainingDayResponseDto> = listOf(fakeTrainingDayResponseDto()),
) = TrainingPlanResponseDto(
    id = id, name = name, description = null,
    frequencyDaysPerWeek = 4, durationWeeks = 8,
    goalType = "GAIN_MUSCLE", fitnessLevel = "INTERMEDIATE",
    location = "GYM", status = status, totalDays = 32,
    createdAt = DEFAULT_CREATED_AT_STR, days = days,
)

fun fakeGenerateTrainingPlanRequestDto() = GenerateTrainingPlanRequestDto(
    frequencyDaysPerWeek = 4, sessionDurationMinutes = 60,
    durationWeeks = 8, goalType = "GAIN_MUSCLE",
    fitnessLevel = "INTERMEDIATE", location = "GYM",
)

// ─── Diet ──────────────────────────────────────────────────────────────────────

fun fakeMealItem(
    id: String = "item-1",
    name: String = "Chicken Breast",
    quantity: Float = 200f,
    unit: String = "g",
    calories: Int = 330,
) = MealItem(
    id = id, name = name, quantity = quantity, unit = unit,
    calories = calories, proteinGrams = 62f, carbsGrams = 0f, fatGrams = 7f,
)

fun fakeMeal(
    id: String = "meal-1",
    mealType: MealType = MealType.LUNCH,
    name: String = "Grilled Chicken",
    calories: Int = 500,
    items: List<MealItem> = listOf(fakeMealItem()),
) = Meal(
    id = id, mealType = mealType, name = name, time = "13:00",
    calories = calories, proteinGrams = 40, carbsGrams = 50, fatGrams = 15,
    items = items,
)

fun fakeDietDay(
    id: String = "dday-1",
    dayNumber: Int = 1,
    name: String = "Day 1",
    totalCalories: Int = 2000,
    meals: List<Meal> = listOf(fakeMeal()),
) = DietDay(id = id, dayNumber = dayNumber, name = name, totalCalories = totalCalories, meals = meals)

fun fakeDietPlan(
    id: String = "diet-plan-1",
    name: String = "Test Diet",
    status: PlanStatus = PlanStatus.ACTIVE,
    days: List<DietDay> = emptyList(),
    preference: DietPreference = DietPreference.NONE,
    dailyCalories: Int = 2000,
    createdAt: LocalDateTime = DEFAULT_CREATED_AT,
) = DietPlan(
    id = id, name = name, description = null,
    dailyCalories = dailyCalories, proteinGrams = 150,
    carbsGrams = 200, fatGrams = 70, durationWeeks = 4,
    preference = preference, status = status,
    totalDays = 28, createdAt = createdAt, days = days,
)

fun fakeDietPlanEntity(
    id: String = "diet-plan-1",
    userId: String = FAKE_USER_ID,
    name: String = "Test Diet",
    status: String = "ACTIVE",
) = DietPlanEntity(
    id = id, userId = userId, name = name, description = null,
    dailyCalories = 2000, proteinGrams = 150, carbsGrams = 200, fatGrams = 70,
    durationWeeks = 4, preference = "NONE", status = status, totalDays = 28,
    createdAt = DEFAULT_CREATED_AT.toInstant(java.time.ZoneOffset.UTC).toEpochMilli(),
)

fun fakeDietPlanSummaryResponseDto(
    id: String = "diet-plan-1",
    name: String = "Test Diet",
    status: String = "ACTIVE",
) = DietPlanSummaryResponseDto(
    id = id, name = name, description = null,
    dailyCalories = 2000, proteinGrams = 150, carbsGrams = 200, fatGrams = 70,
    durationWeeks = 4, preference = "NONE", status = status, totalDays = 28,
    createdAt = DEFAULT_CREATED_AT_STR,
)

fun fakeMealItemResponseDto(
    id: String = "item-1",
    name: String = "Chicken Breast",
) = MealItemResponseDto(
    id = id, name = name, quantity = 200f, unit = "g",
    calories = 330, proteinGrams = 62f, carbsGrams = 0f, fatGrams = 7f,
)

fun fakeMealResponseDto(
    id: String = "meal-1",
    mealType: String = "LUNCH",
    items: List<MealItemResponseDto> = listOf(fakeMealItemResponseDto()),
) = MealResponseDto(
    id = id, mealType = mealType, name = "Grilled Chicken", time = "13:00",
    calories = 500, proteinGrams = 40, carbsGrams = 50, fatGrams = 15,
    items = items,
)

fun fakeDietDayResponseDto(
    id: String = "dday-1",
    meals: List<MealResponseDto> = listOf(fakeMealResponseDto()),
) = DietDayResponseDto(
    id = id, dayNumber = 1, name = "Day 1", totalCalories = 2000, meals = meals,
)

fun fakeDietPlanResponseDto(
    id: String = "diet-plan-1",
    name: String = "Test Diet",
    status: String = "ACTIVE",
    days: List<DietDayResponseDto> = listOf(fakeDietDayResponseDto()),
) = DietPlanResponseDto(
    id = id, name = name, description = null,
    dailyCalories = 2000, proteinGrams = 150, carbsGrams = 200, fatGrams = 70,
    durationWeeks = 4, preference = "NONE", status = status, totalDays = 28,
    createdAt = DEFAULT_CREATED_AT_STR, days = days,
)

fun fakeGenerateDietPlanRequestDto() = GenerateDietPlanRequestDto(
    durationWeeks = 4, mealsPerDay = 3, dietPreference = "NONE",
)

// ─── Nutrition ─────────────────────────────────────────────────────────────────

fun fakeFoodItemLog(
    id: String = "food-item-1",
    name: String = "Chicken Breast",
    quantity: Double = 200.0,
    unit: String = "g",
    calories: Int = 330,
    proteinGrams: Double = 62.0,
    carbsGrams: Double = 0.0,
    fatGrams: Double = 7.0,
) = FoodItemLog(
    id = id, name = name, quantity = quantity, unit = unit,
    calories = calories, proteinGrams = proteinGrams,
    carbsGrams = carbsGrams, fatGrams = fatGrams,
)

fun fakeMealLog(
    id: String = "meal-log-1",
    mealType: MealType = MealType.LUNCH,
    name: String? = "Grilled Chicken",
    time: String = "13:00",
    calories: Int = 520,
    proteinGrams: Double = 45.0,
    carbsGrams: Double = 30.0,
    fatGrams: Double = 18.0,
    aiGenerated: Boolean = false,
    rawInputText: String? = null,
    items: List<FoodItemLog> = listOf(fakeFoodItemLog()),
) = MealLog(
    id = id, mealType = mealType, name = name, time = time,
    calories = calories, proteinGrams = proteinGrams,
    carbsGrams = carbsGrams, fatGrams = fatGrams,
    aiGenerated = aiGenerated, rawInputText = rawInputText,
    items = items,
)

fun fakeNutritionLog(
    id: String = "nutrition-log-1",
    date: LocalDate = LocalDate.of(2026, 4, 6),
    totalCalories: Int = 1450,
    totalProteinGrams: Double = 95.0,
    totalCarbsGrams: Double = 180.0,
    totalFatGrams: Double = 42.0,
    meals: List<MealLog> = listOf(fakeMealLog()),
) = NutritionLog(
    id = id, date = date, totalCalories = totalCalories,
    totalProteinGrams = totalProteinGrams, totalCarbsGrams = totalCarbsGrams,
    totalFatGrams = totalFatGrams, meals = meals,
)

fun fakeNutritionTarget(
    id: String = "target-1",
    calorieTarget: Int = 2200,
    proteinTarget: Double = 165.0,
    carbsTarget: Double = 250.0,
    fatTarget: Double = 73.0,
    effectiveFrom: LocalDate = LocalDate.of(2026, 4, 1),
    setBy: TargetSource = TargetSource.MANUAL,
) = NutritionTarget(
    id = id, calorieTarget = calorieTarget, proteinTarget = proteinTarget,
    carbsTarget = carbsTarget, fatTarget = fatTarget,
    effectiveFrom = effectiveFrom, setBy = setBy,
)

fun fakeNutritionLogEntity(
    id: String = "nutrition-log-1",
    date: Long = LocalDate.of(2026, 4, 6).toEpochDay(),
    totalCalories: Int = 1450,
    totalProteinGrams: Double = 95.0,
    totalCarbsGrams: Double = 180.0,
    totalFatGrams: Double = 42.0,
) = NutritionLogEntity(
    id = id, date = date, totalCalories = totalCalories,
    totalProteinGrams = totalProteinGrams, totalCarbsGrams = totalCarbsGrams,
    totalFatGrams = totalFatGrams,
)

fun fakeNutritionLogResponseDto(
    id: String = "nutrition-log-1",
    date: String = "2026-04-06",
    totalCalories: Int = 1450,
    totalProteinGrams: Double = 95.0,
    totalCarbsGrams: Double = 180.0,
    totalFatGrams: Double = 42.0,
    meals: List<MealLogResponseDto> = listOf(fakeMealLogResponseDto()),
) = NutritionLogResponseDto(
    id = id, date = date, totalCalories = totalCalories,
    totalProteinGrams = totalProteinGrams, totalCarbsGrams = totalCarbsGrams,
    totalFatGrams = totalFatGrams, meals = meals,
)

fun fakeMealLogResponseDto(
    id: String = "meal-log-1",
    mealType: String = "LUNCH",
    name: String? = "Grilled Chicken",
    time: String = "13:00",
    calories: Int = 520,
    proteinGrams: Double = 45.0,
    carbsGrams: Double = 30.0,
    fatGrams: Double = 18.0,
    aiGenerated: Boolean = false,
    rawInputText: String? = null,
    items: List<FoodItemLogResponseDto> = listOf(fakeFoodItemLogResponseDto()),
) = MealLogResponseDto(
    id = id, mealType = mealType, name = name, time = time,
    calories = calories, proteinGrams = proteinGrams,
    carbsGrams = carbsGrams, fatGrams = fatGrams,
    aiGenerated = aiGenerated, rawInputText = rawInputText, items = items,
)

fun fakeFoodItemLogResponseDto(
    id: String = "food-item-1",
    name: String = "Chicken Breast",
    quantity: Double = 200.0,
    unit: String = "g",
    calories: Int = 330,
    proteinGrams: Double = 62.0,
    carbsGrams: Double = 0.0,
    fatGrams: Double = 7.0,
) = FoodItemLogResponseDto(
    id = id, name = name, quantity = quantity, unit = unit,
    calories = calories, proteinGrams = proteinGrams,
    carbsGrams = carbsGrams, fatGrams = fatGrams,
)

fun fakeNutritionTargetResponseDto(
    id: String = "target-1",
    calorieTarget: Int = 2200,
    proteinTarget: Double = 165.0,
    carbsTarget: Double = 250.0,
    fatTarget: Double = 73.0,
    effectiveFrom: String = "2026-04-01",
    setBy: String = "MANUAL",
) = NutritionTargetResponseDto(
    id = id, calorieTarget = calorieTarget, proteinTarget = proteinTarget,
    carbsTarget = carbsTarget, fatTarget = fatTarget,
    effectiveFrom = effectiveFrom, setBy = setBy,
)

fun fakeTrackMealRequestDto(
    date: String = "2026-04-06",
    mealType: String = "LUNCH",
    name: String? = "Grilled Chicken",
    time: String = "13:00",
    items: List<TrackFoodItemRequestDto> = listOf(fakeTrackFoodItemRequestDto()),
) = TrackMealRequestDto(
    date = date, mealType = mealType, name = name, time = time, items = items,
)

fun fakeTrackFoodItemRequestDto(
    name: String = "Chicken Breast",
    quantity: Double? = 200.0,
    unit: String = "g",
    calories: Int? = 330,
    proteinGrams: Double? = 62.0,
    carbsGrams: Double? = 0.0,
    fatGrams: Double? = 7.0,
) = TrackFoodItemRequestDto(
    name = name, quantity = quantity, unit = unit,
    calories = calories, proteinGrams = proteinGrams,
    carbsGrams = carbsGrams, fatGrams = fatGrams,
)

fun fakeAnalyzeMealFromTextRequestDto(
    date: String = "2026-04-06",
    mealType: String = "LUNCH",
    time: String = "13:00",
    text: String = "Grilled chicken with rice and salad",
) = AnalyzeMealFromTextRequestDto(
    date = date, mealType = mealType, time = time, text = text,
)

fun fakeUpdateNutritionTargetRequestDto(
    calorieTarget: Int? = 2200,
    proteinTarget: Double? = 165.0,
    carbsTarget: Double? = 250.0,
    fatTarget: Double? = 73.0,
) = UpdateNutritionTargetRequestDto(
    calorieTarget = calorieTarget, proteinTarget = proteinTarget,
    carbsTarget = carbsTarget, fatTarget = fatTarget,
)

// ─── Progress ──────────────────────────────────────────────────────────────────

fun fakeWorkoutAdherence(
    plannedSessions: Int = 12,
    completedSessions: Int = 10,
    adherencePercentage: Double = 83.3,
    currentStreak: Int = 5,
    longestStreak: Int = 8,
) = WorkoutAdherence(
    plannedSessions = plannedSessions,
    completedSessions = completedSessions,
    adherencePercentage = adherencePercentage,
    currentStreak = currentStreak,
    longestStreak = longestStreak,
)

fun fakeWeightProgress(
    startWeight: Double = 80.0,
    currentWeight: Double = 78.5,
    targetWeight: Double = 75.0,
    change: Double = -1.5,
    trend: WeightTrend = WeightTrend.LOSING,
    entries: List<WeightEntry> = listOf(
        WeightEntry(date = LocalDate.of(2026, 3, 1), weight = 80.0),
        WeightEntry(date = LocalDate.of(2026, 3, 15), weight = 79.0),
    ),
) = WeightProgress(
    startWeight = startWeight,
    currentWeight = currentWeight,
    targetWeight = targetWeight,
    change = change,
    trend = trend,
    entries = entries,
)

fun fakeNutritionAdherence(
    averageCalories: Double = 2100.0,
    calorieTarget: Int = 2200,
    adherencePercentage: Double = 95.5,
) = NutritionAdherence(
    averageCalories = averageCalories,
    calorieTarget = calorieTarget,
    adherencePercentage = adherencePercentage,
)

fun fakeStrengthProgress(
    exerciseName: String = "Bench Press",
    startMax: Double = 60.0,
    currentMax: Double = 70.0,
    changePercentage: Double = 16.7,
) = StrengthProgress(
    exerciseName = exerciseName,
    startMax = startMax,
    currentMax = currentMax,
    changePercentage = changePercentage,
)

fun fakeProgressDashboard(
    periodFrom: LocalDate = LocalDate.of(2026, 3, 1),
    periodTo: LocalDate = LocalDate.of(2026, 3, 31),
    workoutAdherence: WorkoutAdherence = fakeWorkoutAdherence(),
    weightProgress: WeightProgress = fakeWeightProgress(),
    nutritionAdherence: NutritionAdherence = fakeNutritionAdherence(),
    strengthProgress: List<StrengthProgress> = listOf(fakeStrengthProgress()),
) = ProgressDashboard(
    periodFrom = periodFrom,
    periodTo = periodTo,
    workoutAdherence = workoutAdherence,
    weightProgress = weightProgress,
    nutritionAdherence = nutritionAdherence,
    strengthProgress = strengthProgress,
)

fun fakeWeeklyProgressSummary(
    workoutsThisWeek: Int = 3,
    workoutsTarget: Int = 4,
    averageCaloriesToday: Double = 2050.0,
    calorieTarget: Int = 2200,
    currentStreak: Int = 5,
    bodyWeight: Double? = 78.5,
) = WeeklyProgressSummary(
    workoutsThisWeek = workoutsThisWeek,
    workoutsTarget = workoutsTarget,
    averageCaloriesToday = averageCaloriesToday,
    calorieTarget = calorieTarget,
    currentStreak = currentStreak,
    bodyWeight = bodyWeight,
)

fun fakeBodyWeightLog(
    id: String = "bw-1",
    weight: Double = 78.5,
    date: LocalDate = LocalDate.of(2026, 3, 15),
    notes: String? = "Morning weight",
    createdAt: LocalDate = LocalDate.of(2026, 3, 15),
) = BodyWeightLog(
    id = id, weight = weight, date = date,
    notes = notes, createdAt = createdAt,
)

fun fakeBodyWeightEntity(
    id: String = "bw-1",
    weight: Double = 78.5,
    date: Long = LocalDate.of(2026, 3, 15).toEpochDay(),
    notes: String? = "Morning weight",
    createdAt: Long = LocalDate.of(2026, 3, 15).toEpochDay(),
) = BodyWeightEntity(
    id = id, weight = weight, date = date,
    notes = notes, createdAt = createdAt,
)

fun fakeBodyWeightLogResponseDto(
    id: String = "bw-1",
    weight: Double = 78.5,
    date: String = "2026-03-15",
    notes: String? = "Morning weight",
    createdAt: String = "2026-03-15T10:00:00",
) = BodyWeightLogResponseDto(
    id = id, weight = weight, date = date,
    notes = notes, createdAt = createdAt,
)

fun fakeWorkoutAdherenceResponseDto(
    plannedSessions: Int = 12,
    completedSessions: Int = 10,
    adherencePercentage: Double = 83.3,
    currentStreak: Int = 5,
    longestStreak: Int = 8,
) = WorkoutAdherenceResponseDto(
    plannedSessions = plannedSessions,
    completedSessions = completedSessions,
    adherencePercentage = adherencePercentage,
    currentStreak = currentStreak,
    longestStreak = longestStreak,
)

fun fakeWeightProgressResponseDto(
    initialWeight: Double = 80.0,
    currentWeight: Double = 78.5,
    targetWeight: Double = 75.0,
    change: Double = -1.5,
    trend: String = "LOSING",
    weeklyAverage: Double = 78.8,
    entries: List<WeightEntryDto> = listOf(
        WeightEntryDto(date = "2026-03-01", weight = 80.0),
        WeightEntryDto(date = "2026-03-15", weight = 79.0),
    ),
) = WeightProgressResponseDto(
    initialWeight = initialWeight,
    currentWeight = currentWeight,
    targetWeight = targetWeight,
    change = change,
    trend = trend,
    weeklyAverage = weeklyAverage,
    entries = entries,
)

fun fakeNutritionAdherenceResponseDto(
    targetCalories: Int = 2200,
    averageCaloriesConsumed: Double = 2100.0,
    calorieAdherencePercentage: Double = 95.5,
    targetProtein: Double = 165.0,
    averageProteinConsumed: Double = 155.0,
    proteinAdherencePercentage: Double = 93.9,
    daysTracked: Int = 28,
) = NutritionAdherenceResponseDto(
    targetCalories = targetCalories,
    averageCaloriesConsumed = averageCaloriesConsumed,
    calorieAdherencePercentage = calorieAdherencePercentage,
    targetProtein = targetProtein,
    averageProteinConsumed = averageProteinConsumed,
    proteinAdherencePercentage = proteinAdherencePercentage,
    daysTracked = daysTracked,
)

fun fakeStrengthProgressResponseDto(
    exerciseName: String = "Bench Press",
    trainingExerciseId: String = "exercise-1",
    bestSetStart: BestSetResponseDto = BestSetResponseDto(date = "2026-03-01", reps = 8, weight = 60.0),
    bestSetEnd: BestSetResponseDto = BestSetResponseDto(date = "2026-03-30", reps = 8, weight = 70.0),
    progressionPercentage: Double = 16.7,
    trend: String = "IMPROVING",
) = StrengthProgressResponseDto(
    exerciseName = exerciseName,
    trainingExerciseId = trainingExerciseId,
    bestSetStart = bestSetStart,
    bestSetEnd = bestSetEnd,
    progressionPercentage = progressionPercentage,
    trend = trend,
)

fun fakeProgressDashboardResponseDto(
    period: PeriodResponseDto = PeriodResponseDto(from = "2026-03-01", to = "2026-03-31"),
    workoutAdherence: WorkoutAdherenceResponseDto = fakeWorkoutAdherenceResponseDto(),
    weightProgress: WeightProgressResponseDto = fakeWeightProgressResponseDto(),
    nutritionAdherence: NutritionAdherenceResponseDto = fakeNutritionAdherenceResponseDto(),
    strengthProgress: List<StrengthProgressResponseDto> = listOf(fakeStrengthProgressResponseDto()),
    generatedAt: String = "2026-03-31T23:59:59",
) = ProgressDashboardResponseDto(
    period = period,
    workoutAdherence = workoutAdherence,
    weightProgress = weightProgress,
    nutritionAdherence = nutritionAdherence,
    strengthProgress = strengthProgress,
    generatedAt = generatedAt,
)

fun fakeWeeklyProgressSummaryResponseDto(
    workoutsThisWeek: Int = 3,
    workoutsTarget: Int = 4,
    averageCaloriesToday: Double = 2050.0,
    calorieTarget: Int = 2200,
    currentStreak: Int = 5,
    bodyWeight: Double? = 78.5,
) = WeeklyProgressSummaryResponseDto(
    workoutsThisWeek = workoutsThisWeek,
    workoutsTarget = workoutsTarget,
    averageCaloriesToday = averageCaloriesToday,
    calorieTarget = calorieTarget,
    currentStreak = currentStreak,
    bodyWeight = bodyWeight,
)

fun fakeLogBodyWeightRequestDto(
    weight: Double = 78.5,
    date: String = "2026-03-15",
    notes: String? = "Morning weight",
) = LogBodyWeightRequestDto(
    weight = weight, date = date, notes = notes,
)

// ─── Education ─────────────────────────────────────────────────────────────────

fun fakeContextualExplanation(
    id: String = "expl-1",
    referenceType: ExplanationReferenceType = ExplanationReferenceType.TRAINING_EXERCISE,
    referenceId: String = "exercise-1",
    referenceName: String = "Bench Press",
    content: String = "The bench press is a compound exercise that targets the chest.",
    knowledgeLevel: KnowledgeLevel = KnowledgeLevel.BEGINNER,
    generatedAt: String = "2026-04-01T10:00:00",
) = ContextualExplanation(
    id = id, referenceType = referenceType, referenceId = referenceId,
    referenceName = referenceName, content = content,
    knowledgeLevel = knowledgeLevel, generatedAt = generatedAt,
)

fun fakeWhyThisExplanation(
    referenceType: ExplanationReferenceType = ExplanationReferenceType.TRAINING_EXERCISE,
    referenceId: String = "exercise-1",
    referenceName: String = "Bench Press",
    explanation: String = "This exercise is in your plan because it targets chest development.",
    knowledgeLevel: KnowledgeLevel = KnowledgeLevel.BEGINNER,
) = WhyThisExplanation(
    referenceType = referenceType, referenceId = referenceId,
    referenceName = referenceName, explanation = explanation,
    knowledgeLevel = knowledgeLevel,
)

fun fakeGlossaryDefinition(
    term: String = "Hypertrophy",
    definition: String = "The enlargement of an organ or tissue from the increase in size of its cells.",
    category: String = "BEGINNER",
    relatedTerms: List<String> = listOf("Progressive Overload", "Volume"),
) = GlossaryDefinition(
    term = term, definition = definition,
    category = category, relatedTerms = relatedTerms,
)

fun fakeContextualExplanationResponseDto(
    id: String = "expl-1",
    referenceType: String = "TRAINING_EXERCISE",
    referenceId: String = "exercise-1",
    referenceName: String = "Bench Press",
    content: String = "The bench press is a compound exercise that targets the chest.",
    knowledgeLevelAtGeneration: String = "BEGINNER",
    generatedAt: String = "2026-04-01T10:00:00",
) = ContextualExplanationResponseDto(
    id = id, referenceType = referenceType, referenceId = referenceId,
    referenceName = referenceName, content = content,
    knowledgeLevelAtGeneration = knowledgeLevelAtGeneration, generatedAt = generatedAt,
)

fun fakeWhyThisResponseDto(
    referenceType: String = "TRAINING_EXERCISE",
    referenceId: String = "exercise-1",
    referenceName: String = "Bench Press",
    explanation: String = "This exercise is in your plan because it targets chest development.",
    knowledgeLevelAtGeneration: String = "BEGINNER",
) = WhyThisResponseDto(
    referenceType = referenceType, referenceId = referenceId,
    referenceName = referenceName, explanation = explanation,
    knowledgeLevelAtGeneration = knowledgeLevelAtGeneration,
)

fun fakeGlossaryDefinitionResponseDto(
    term: String = "Hypertrophy",
    definition: String = "The enlargement of an organ or tissue from the increase in size of its cells.",
    knowledgeLevel: String = "BEGINNER",
    relatedTerms: List<String> = listOf("Progressive Overload", "Volume"),
) = GlossaryDefinitionResponseDto(
    term = term, definition = definition,
    knowledgeLevel = knowledgeLevel, relatedTerms = relatedTerms,
)

// ─── Progression ───────────────────────────────────────────────────────────────

fun fakeProgressionRecommendation(
    trainingExerciseId: String = "exercise-1",
    exerciseName: String = "Bench Press",
    type: ProgressionType = ProgressionType.INCREASE_LOAD,
    currentLoad: Double? = 60.0,
    suggestedLoad: Double? = 65.0,
    suggestedRepsMin: Int = 8,
    suggestedRepsMax: Int = 10,
    rationale: String = "Consistent performance in last 3 sessions allows a load increase.",
    confidence: Double = 0.85,
    basedOnSessions: Int = 3,
) = ProgressionRecommendation(
    trainingExerciseId = trainingExerciseId, exerciseName = exerciseName,
    type = type, currentLoad = currentLoad, suggestedLoad = suggestedLoad,
    suggestedRepsMin = suggestedRepsMin, suggestedRepsMax = suggestedRepsMax,
    rationale = rationale, confidence = confidence, basedOnSessions = basedOnSessions,
)

fun fakePlanProgressionSummary(
    planId: String = "plan-1",
    recommendations: List<ProgressionRecommendation> = listOf(fakeProgressionRecommendation()),
    overallTrend: ProgressTrend = ProgressTrend.IMPROVING,
    lastAnalyzedAt: String = "2026-04-01T10:00:00",
) = PlanProgressionSummary(
    planId = planId, recommendations = recommendations,
    overallTrend = overallTrend, lastAnalyzedAt = lastAnalyzedAt,
)

fun fakeProgressionRecommendationResponseDto(
    trainingExerciseId: String = "exercise-1",
    exerciseName: String = "Bench Press",
    type: String = "INCREASE_LOAD",
    currentLoad: Double? = 60.0,
    suggestedLoad: Double? = 65.0,
    suggestedRepsMin: Int = 8,
    suggestedRepsMax: Int = 10,
    rationale: String = "Consistent performance in last 3 sessions allows a load increase.",
    confidence: Double = 0.85,
    basedOnSessions: Int = 3,
) = ProgressionRecommendationResponseDto(
    trainingExerciseId = trainingExerciseId, exerciseName = exerciseName,
    type = type, currentLoad = currentLoad, suggestedLoad = suggestedLoad,
    suggestedRepsMin = suggestedRepsMin, suggestedRepsMax = suggestedRepsMax,
    rationale = rationale, confidence = confidence, basedOnSessions = basedOnSessions,
)

fun fakePlanProgressionSummaryResponseDto(
    trainingPlanId: String = "plan-1",
    totalExercises: Int = 10,
    exercisesAnalyzed: Int = 8,
    recommendations: List<ProgressionRecommendationResponseDto> = listOf(fakeProgressionRecommendationResponseDto()),
) = PlanProgressionSummaryResponseDto(
    trainingPlanId = trainingPlanId, totalExercises = totalExercises,
    exercisesAnalyzed = exercisesAnalyzed, recommendations = recommendations,
)

// ─── Workout ───────────────────────────────────────────────────────────────────

fun fakeWorkoutLog(
    id: String = "wl-1",
    trainingPlanId: String = "plan-1",
    trainingDayId: String = "day-1",
    date: LocalDate = LocalDate.of(2026, 4, 6),
    durationMinutes: Int? = 55,
    perceivedExertion: Int? = 7,
    notes: String? = null,
    totalExercises: Int = 5,
    completedAt: LocalDateTime = LocalDateTime.of(2026, 4, 6, 10, 30),
    isLocked: Boolean = false,
) = WorkoutLog(
    id = id, trainingPlanId = trainingPlanId, trainingDayId = trainingDayId,
    date = date, durationMinutes = durationMinutes,
    perceivedExertion = perceivedExertion, notes = notes,
    totalExercises = totalExercises, completedAt = completedAt,
    isLocked = isLocked,
)

// ─── Gamification ──────────────────────────────────────────────────────────────

fun fakeStreak(
    type: StreakType = StreakType.TRAINING,
    status: StreakStatus = StreakStatus.ACTIVE,
    currentCount: Int = 5,
    longestCount: Int = 10,
    lastActivityDate: LocalDate = LocalDate.of(2026, 4, 5),
    startedAt: String = "2026-03-01T10:00:00",
) = Streak(
    type = type, status = status, currentCount = currentCount,
    longestCount = longestCount, lastActivityDate = lastActivityDate,
    startedAt = startedAt,
)

fun fakeAchievementDefinition(
    id: String = "ach-def-1",
    code: String = "FIRST_WORKOUT",
    type: AchievementType = AchievementType.STRENGTH_MILESTONE,
    name: String = "First Workout",
    description: String = "Complete your first workout session.",
    rarity: AchievementRarity = AchievementRarity.COMMON,
    iconKey: String = "fitness_center",
) = AchievementDefinition(
    id = id, code = code, type = type, name = name,
    description = description, rarity = rarity, iconKey = iconKey,
)

fun fakeUserAchievement(
    id: String = "user-ach-1",
    achievement: AchievementDefinition = fakeAchievementDefinition(),
    unlockedAt: String = "2026-04-01T10:00:00",
    triggerDescription: String = "Completed first workout session",
) = UserAchievement(
    id = id, achievement = achievement,
    unlockedAt = unlockedAt, triggerDescription = triggerDescription,
)

fun fakePersonalRecord(
    id: String = "pr-1",
    exerciseName: String = "Bench Press",
    weightKg: Double = 80.0,
    reps: Int = 8,
    estimatedOneRepMax: Double = 100.0,
    achievedAt: String = "2026-04-01T10:00:00",
) = PersonalRecord(
    id = id, exerciseName = exerciseName, weightKg = weightKg,
    reps = reps, estimatedOneRepMax = estimatedOneRepMax, achievedAt = achievedAt,
)

fun fakeProgressExport(
    userId: String = "user-1",
    userName: String = "Test User",
    period: String = "LAST_MONTH",
    generatedAt: String = "2026-04-06T10:00:00",
    totalWorkouts: Int = 12,
    totalPRs: Int = 3,
    currentStreak: Int = 5,
    achievementsUnlocked: Int = 2,
    weightChange: Double? = -1.5,
    topExercises: List<String> = listOf("Bench Press: 16%"),
) = ProgressExport(
    userId = userId, userName = userName, period = period,
    generatedAt = generatedAt, totalWorkouts = totalWorkouts,
    totalPRs = totalPRs, currentStreak = currentStreak,
    achievementsUnlocked = achievementsUnlocked, weightChange = weightChange,
    topExercises = topExercises,
)

fun fakeStreakResponseDto(
    type: String = "TRAINING",
    status: String = "ACTIVE",
    currentCount: Int = 5,
    longestCount: Int = 10,
    lastActivityDate: String = "2026-04-05",
    startedAt: String = "2026-03-01T10:00:00",
) = StreakResponseDto(
    type = type, status = status, currentCount = currentCount,
    longestCount = longestCount, lastActivityDate = lastActivityDate,
    startedAt = startedAt,
)

fun fakeAchievementDefinitionResponseDto(
    id: String = "ach-def-1",
    code: String = "FIRST_WORKOUT",
    type: String = "STRENGTH_MILESTONE",
    name: String = "First Workout",
    description: String = "Complete your first workout session.",
    rarity: String = "COMMON",
    iconKey: String = "fitness_center",
) = AchievementDefinitionResponseDto(
    id = id, code = code, type = type, name = name,
    description = description, rarity = rarity, iconKey = iconKey,
)

fun fakeUserAchievementResponseDto(
    id: String = "user-ach-1",
    achievement: AchievementDefinitionResponseDto = fakeAchievementDefinitionResponseDto(),
    unlockedAt: String = "2026-04-01T10:00:00",
    triggerDescription: String = "Completed first workout session",
) = UserAchievementResponseDto(
    id = id, achievement = achievement,
    unlockedAt = unlockedAt, triggerDescription = triggerDescription,
)

fun fakePersonalRecordResponseDto(
    id: String = "pr-1",
    exerciseName: String = "Bench Press",
    weightKg: Double = 80.0,
    reps: Int = 8,
    estimatedOneRepMax: Double = 100.0,
    achievedAt: String = "2026-04-01T10:00:00",
) = PersonalRecordResponseDto(
    id = id, exerciseName = exerciseName, weightKg = weightKg,
    reps = reps, estimatedOneRepMax = estimatedOneRepMax, achievedAt = achievedAt,
)

fun fakeProgressExportResponseDto(
    userId: String = "user-1",
    userName: String = "Test User",
    period: String = "LAST_MONTH",
    generatedAt: String = "2026-04-06T10:00:00",
    weightSummary: WeightSummaryExportDto? = WeightSummaryExportDto(
        initialWeight = 80.0, currentWeight = 78.5, change = -1.5,
    ),
    personalRecords: List<PersonalRecordResponseDto> = listOf(fakePersonalRecordResponseDto()),
    weeklyAdherenceSummary: List<WeeklyAdherenceExportDto> = listOf(
        WeeklyAdherenceExportDto(weekStart = "2026-03-25", trainingDaysCompleted = 4, trainingDaysPlanned = 4, nutritionDaysTracked = 7),
    ),
    streaks: List<StreakExportSummaryDto> = listOf(
        StreakExportSummaryDto(type = "TRAINING", currentCount = 5, longestCount = 10, status = "ACTIVE"),
    ),
    unlockedAchievements: List<AchievementExportEntryDto> = listOf(
        AchievementExportEntryDto(name = "First Workout", rarity = "COMMON", unlockedAt = "2026-04-01T10:00:00"),
    ),
    topExercisesProgression: List<ExerciseProgressionExportDto> = listOf(
        ExerciseProgressionExportDto(exerciseName = "Bench Press", initialBestWeightKg = 60.0, currentBestWeightKg = 70.0, progressionPercentage = 16.7),
    ),
) = ProgressExportResponseDto(
    userId = userId, userName = userName, period = period,
    generatedAt = generatedAt, weightSummary = weightSummary,
    personalRecords = personalRecords, weeklyAdherenceSummary = weeklyAdherenceSummary,
    streaks = streaks, unlockedAchievements = unlockedAchievements,
    topExercisesProgression = topExercisesProgression,
)

// ─── Metabolic ─────────────────────────────────────────────────────────────────

fun fakeWeightTrend(
    averageWeeklyChange: Double = -0.3,
    trend: String = "LOSING",
    expectedWeeklyChange: Double = -0.5,
    deviationFromExpected: Double = 0.2,
    dataPoints: Int = 14,
) = MetabolicWeightTrend(
    averageWeeklyChange = averageWeeklyChange,
    trend = trend,
    expectedWeeklyChange = expectedWeeklyChange,
    deviationFromExpected = deviationFromExpected,
    dataPoints = dataPoints,
)

fun fakeMetabolicAdjustmentRecommendation(
    type: AdjustmentType = AdjustmentType.DECREASE_CALORIES,
    suggestedCalorieTarget: Int = 2000,
    suggestedProteinTarget: Double = 160.0,
    suggestedCarbsTarget: Double = 220.0,
    suggestedFatTarget: Double = 65.0,
    magnitude: AdjustmentMagnitude = AdjustmentMagnitude.MODERATE,
    urgency: AdjustmentUrgency = AdjustmentUrgency.SUGGESTED,
) = MetabolicAdjustmentRecommendation(
    type = type,
    suggestedCalorieTarget = suggestedCalorieTarget,
    suggestedProteinTarget = suggestedProteinTarget,
    suggestedCarbsTarget = suggestedCarbsTarget,
    suggestedFatTarget = suggestedFatTarget,
    magnitude = magnitude,
    urgency = urgency,
)

fun fakeMetabolicAnalysis(
    status: MetabolicStatus = MetabolicStatus.ON_TRACK,
    weightTrend: MetabolicWeightTrend = fakeWeightTrend(),
    calorieAdherenceRate: Double = 92.5,
    averageCalorieDeficitSurplus: Double = -150.0,
    recommendation: MetabolicAdjustmentRecommendation? = fakeMetabolicAdjustmentRecommendation(),
    rationale: String = "Your progress is on track. Minor calorie adjustment suggested.",
) = MetabolicAnalysis(
    status = status,
    weightTrend = weightTrend,
    calorieAdherenceRate = calorieAdherenceRate,
    averageCalorieDeficitSurplus = averageCalorieDeficitSurplus,
    recommendation = recommendation,
    rationale = rationale,
)

fun fakeMetabolicInsight(
    id: String = "insight-1",
    statusAtTime: MetabolicStatus = MetabolicStatus.ON_TRACK,
    adjustmentType: AdjustmentType = AdjustmentType.DECREASE_CALORIES,
    previousCalorieTarget: Int = 2200,
    newCalorieTarget: Int = 2000,
    magnitude: AdjustmentMagnitude = AdjustmentMagnitude.MODERATE,
    rationale: String = "Adherence high, reducing surplus.",
    appliedAt: String = "2026-04-01T10:00:00Z",
) = MetabolicInsight(
    id = id, statusAtTime = statusAtTime, adjustmentType = adjustmentType,
    previousCalorieTarget = previousCalorieTarget, newCalorieTarget = newCalorieTarget,
    magnitude = magnitude, rationale = rationale, appliedAt = appliedAt,
)

fun fakeWeightTrendResponseDto(
    averageWeeklyChange: Double = -0.3,
    trend: String = "LOSING",
    expectedWeeklyChange: Double = -0.5,
    deviationFromExpected: Double = 0.2,
    dataPoints: Int = 14,
) = WeightTrendResponseDto(
    averageWeeklyChange = averageWeeklyChange,
    trend = trend,
    expectedWeeklyChange = expectedWeeklyChange,
    deviationFromExpected = deviationFromExpected,
    dataPoints = dataPoints,
)

fun fakeMetabolicAdjustmentRecommendationResponseDto(
    type: String = "DECREASE_CALORIES",
    suggestedCalorieTarget: Int = 2000,
    suggestedProteinTarget: Double = 160.0,
    suggestedCarbsTarget: Double = 220.0,
    suggestedFatTarget: Double = 65.0,
    magnitude: String = "MODERATE",
    urgency: String = "SUGGESTED",
) = MetabolicAdjustmentRecommendationResponseDto(
    type = type, suggestedCalorieTarget = suggestedCalorieTarget,
    suggestedProteinTarget = suggestedProteinTarget,
    suggestedCarbsTarget = suggestedCarbsTarget,
    suggestedFatTarget = suggestedFatTarget,
    magnitude = magnitude, urgency = urgency,
)

fun fakeMetabolicAnalysisResponseDto(
    status: String = "ON_TRACK",
    weightTrend: WeightTrendResponseDto = fakeWeightTrendResponseDto(),
    calorieAdherenceRate: Double = 92.5,
    averageCalorieDeficitSurplus: Double = -150.0,
    recommendation: MetabolicAdjustmentRecommendationResponseDto? = fakeMetabolicAdjustmentRecommendationResponseDto(),
    rationale: String = "Your progress is on track.",
) = MetabolicAnalysisResponseDto(
    status = status, weightTrend = weightTrend,
    calorieAdherenceRate = calorieAdherenceRate,
    averageCalorieDeficitSurplus = averageCalorieDeficitSurplus,
    recommendation = recommendation, rationale = rationale,
)

fun fakeMetabolicInsightResponseDto(
    id: String = "insight-1",
    statusAtTime: String = "ON_TRACK",
    adjustmentType: String = "DECREASE_CALORIES",
    previousCalorieTarget: Int = 2200,
    newCalorieTarget: Int = 2000,
    magnitude: String = "MODERATE",
    rationale: String = "Adherence high, reducing surplus.",
    appliedAt: String = "2026-04-01T10:00:00Z",
) = MetabolicInsightResponseDto(
    id = id, statusAtTime = statusAtTime, adjustmentType = adjustmentType,
    previousCalorieTarget = previousCalorieTarget, newCalorieTarget = newCalorieTarget,
    magnitude = magnitude, rationale = rationale, appliedAt = appliedAt,
)

fun fakeApplyMetabolicAdjustmentRequestDto(
    newCalorieTarget: Int = 2000,
    newProteinTarget: Double = 160.0,
    newCarbsTarget: Double = 220.0,
    newFatTarget: Double = 65.0,
    adjustmentType: String = "DECREASE_CALORIES",
    magnitude: String = "MODERATE",
    rationale: String? = "On track adjustment",
) = ApplyMetabolicAdjustmentRequestDto(
    newCalorieTarget = newCalorieTarget, newProteinTarget = newProteinTarget,
    newCarbsTarget = newCarbsTarget, newFatTarget = newFatTarget,
    adjustmentType = adjustmentType, magnitude = magnitude, rationale = rationale,
)

// ─── Chat ──────────────────────────────────────────────────────────────────────

fun fakeChatMessage(
    id: String = "msg-1",
    role: ChatMessageRole = ChatMessageRole.USER,
    content: String = "How should I warm up before squats?",
    createdAt: String = "2026-04-06T10:00:00Z",
) = ChatMessage(id = id, role = role, content = content, createdAt = createdAt)

fun fakeChatSession(
    id: String = "session-1",
    title: String = "Workout Questions",
    status: ChatSessionStatus = ChatSessionStatus.ACTIVE,
    messages: List<ChatMessage> = listOf(fakeChatMessage()),
    createdAt: String = "2026-04-06T09:00:00Z",
    updatedAt: String = "2026-04-06T10:00:00Z",
    messageCount: Int = messages.size,
) = ChatSession(
    id = id, title = title, status = status, messages = messages,
    createdAt = createdAt, updatedAt = updatedAt, messageCount = messageCount,
)

fun fakeChatMessageResponseDto(
    id: String = "msg-1",
    role: String = "ASSISTANT",
    content: String = "Start with 5 minutes of light cardio.",
    createdAt: String = "2026-04-06T10:01:00Z",
) = ChatMessageResponseDto(id = id, role = role, content = content, createdAt = createdAt)

fun fakeChatSessionResponseDto(
    id: String = "session-1",
    title: String = "Workout Questions",
    status: String = "ACTIVE",
    messages: List<ChatMessageResponseDto> = listOf(fakeChatMessageResponseDto()),
    createdAt: String = "2026-04-06T09:00:00Z",
    updatedAt: String = "2026-04-06T10:00:00Z",
) = ChatSessionResponseDto(
    id = id, title = title, status = status, messages = messages,
    createdAt = createdAt, updatedAt = updatedAt,
)

fun fakeChatSessionSummaryResponseDto(
    id: String = "session-1",
    title: String = "Workout Questions",
    status: String = "ACTIVE",
    messageCount: Int = 3,
    createdAt: String = "2026-04-06T09:00:00Z",
    updatedAt: String = "2026-04-06T10:00:00Z",
) = ChatSessionSummaryResponseDto(
    id = id, title = title, status = status, messageCount = messageCount,
    createdAt = createdAt, updatedAt = updatedAt,
)

fun fakeChatSessionEntity(
    id: String = "session-1",
    title: String = "Workout Questions",
    status: String = "ACTIVE",
    messageCount: Int = 3,
    createdAt: Long = 1743933600000L,
    updatedAt: Long = 1743937200000L,
) = ChatSessionEntity(
    id = id, title = title, status = status,
    messageCount = messageCount, createdAt = createdAt, updatedAt = updatedAt,
)

fun fakeChatMessageEntity(
    id: String = "msg-1",
    sessionId: String = "session-1",
    role: String = "USER",
    content: String = "How should I warm up?",
    createdAt: Long = 1743933600000L,
) = ChatMessageEntity(
    id = id, sessionId = sessionId, role = role,
    content = content, createdAt = createdAt,
)


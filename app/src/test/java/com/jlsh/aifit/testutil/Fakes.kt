package com.jlsh.aifit.testutil

import com.jlsh.aifit.feature.auth.data.dto.AuthResponseDto
import com.jlsh.aifit.feature.auth.domain.model.AuthToken
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


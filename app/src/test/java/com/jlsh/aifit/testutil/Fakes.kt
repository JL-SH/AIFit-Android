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

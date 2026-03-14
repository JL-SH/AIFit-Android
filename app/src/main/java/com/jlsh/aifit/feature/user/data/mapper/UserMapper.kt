package com.jlsh.aifit.feature.user.data.mapper

import com.jlsh.aifit.feature.user.data.dto.CreateUserProfileRequestDto
import com.jlsh.aifit.feature.user.data.dto.UpdateUserProfileRequestDto
import com.jlsh.aifit.feature.user.data.dto.UserProfileResponseDto
import com.jlsh.aifit.feature.user.data.local.UserProfileEntity
import com.jlsh.aifit.feature.user.domain.model.ActivityLevel
import com.jlsh.aifit.feature.user.domain.model.CreateUserProfileRequest
import com.jlsh.aifit.feature.user.domain.model.DietPreference
import com.jlsh.aifit.feature.user.domain.model.FitnessLevel
import com.jlsh.aifit.feature.user.domain.model.Gender
import com.jlsh.aifit.feature.user.domain.model.GoalType
import com.jlsh.aifit.feature.user.domain.model.WorkoutLocation
import com.jlsh.aifit.feature.user.domain.model.UpdateUserProfileRequest
import com.jlsh.aifit.feature.user.domain.model.UserProfile
import java.time.LocalDate

object UserMapper {

    fun UserProfileResponseDto.toDomain(): UserProfile = UserProfile(
        id = id,
        name = name,
        email = email,
        authProvider = authProvider,
        profilePictureUrl = profilePictureUrl,
        birthDate = birthDate?.let { runCatching { LocalDate.parse(it) }.getOrNull() },
        gender = Gender.fromString(gender),
        height = height,
        weight = weight,
        targetWeight = targetWeight,
        goalType = GoalType.fromString(goalType),
        activityLevel = ActivityLevel.fromString(activityLevel),
        fitnessLevel = FitnessLevel.fromString(fitnessLevel),
        workoutLocation = WorkoutLocation.fromString(preferredLocation),
        dietPreference = DietPreference.fromString(dietPreference),
        knowledgeLevel = knowledgeLevel,
        weeklyWorkoutDays = weeklyWorkoutDays,
        availableMinutesPerSession = availableMinutesPerSession,
        injuries = injuries,
        calorieTarget = calorieTarget,
    )

    fun UserProfile.toEntity(): UserProfileEntity = UserProfileEntity(
        id = id,
        name = name,
        email = email,
        goalType = goalType?.name,
        fitnessLevel = fitnessLevel?.name,
        profilePictureUrl = profilePictureUrl,
    )

    fun UserProfileEntity.toDomain(): UserProfile = UserProfile(
        id = id,
        name = name,
        email = email,
        authProvider = "",
        profilePictureUrl = profilePictureUrl,
        birthDate = null,
        gender = null,
        height = null,
        weight = null,
        targetWeight = null,
        goalType = goalType?.let { GoalType.fromString(it) },
        activityLevel = null,
        fitnessLevel = fitnessLevel?.let { FitnessLevel.fromString(it) },
        workoutLocation = null,
        dietPreference = null,
        knowledgeLevel = null,
        weeklyWorkoutDays = null,
        availableMinutesPerSession = null,
        injuries = null,
        calorieTarget = null,
    )

    fun CreateUserProfileRequest.toDto(): CreateUserProfileRequestDto = CreateUserProfileRequestDto(
        birthDate = birthDate?.toString(),
        gender = gender?.name,
        goalType = goalType?.name,
        activityLevel = activityLevel?.name,
        fitnessLevel = fitnessLevel?.name,
        preferredLocation = workoutLocation?.name,
        dietPreference = dietPreference?.name,
        height = height,
        weight = weight,
        targetWeight = targetWeight,
        weeklyWorkoutDays = weeklyWorkoutDays,
        availableMinutesPerSession = availableMinutesPerSession,
        injuries = injuries,
        calorieTarget = calorieTarget,
    )

    fun UpdateUserProfileRequest.toDto(): UpdateUserProfileRequestDto = UpdateUserProfileRequestDto(
        birthDate = birthDate?.toString(),
        gender = gender?.name,
        goalType = goalType?.name,
        activityLevel = activityLevel?.name,
        fitnessLevel = fitnessLevel?.name,
        preferredLocation = workoutLocation?.name,
        dietPreference = dietPreference?.name,
        height = height,
        weight = weight,
        targetWeight = targetWeight,
        weeklyWorkoutDays = weeklyWorkoutDays,
        availableMinutesPerSession = availableMinutesPerSession,
        injuries = injuries,
        calorieTarget = calorieTarget,
    )
}


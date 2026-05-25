package com.jlsh.aifit.feature.user.data.mapper

import com.jlsh.aifit.feature.diet.data.mapper.DietMapper.toDomain
import com.jlsh.aifit.feature.nutrition.data.mapper.NutritionMapper.toDomain
import com.jlsh.aifit.feature.training.data.mapper.TrainingMapper.toDomain
import com.jlsh.aifit.feature.user.data.dto.CreateUserProfileRequestDto
import com.jlsh.aifit.feature.user.data.dto.OnboardingResultDto
import com.jlsh.aifit.feature.user.data.dto.UpdateUserProfileRequestDto
import com.jlsh.aifit.feature.user.data.dto.UserProfileResponseDto
import com.jlsh.aifit.feature.user.data.local.UserProfileEntity
import com.jlsh.aifit.feature.user.domain.model.ActivityLevel
import com.jlsh.aifit.feature.user.domain.model.CreateUserProfileRequest
import com.jlsh.aifit.feature.user.domain.model.DietPreference
import com.jlsh.aifit.feature.user.domain.model.FitnessLevel
import com.jlsh.aifit.feature.user.domain.model.Gender
import com.jlsh.aifit.feature.user.domain.model.GoalType
import com.jlsh.aifit.feature.user.domain.model.OnboardingResult
import com.jlsh.aifit.feature.user.domain.model.WorkoutLocation
import com.jlsh.aifit.feature.user.domain.model.UpdateUserProfileRequest
import com.jlsh.aifit.feature.user.domain.model.UserProfile
import java.time.LocalDate

/**
 * Conversions between DTO, Room entity and user domain models.
 */
object UserMapper {

    /**
     * Picks the best avatar URL among several candidates.
     *
     * Uploaded photos (Cloudinary) take priority over the default Google OAuth avatar.
     *
     * @param candidates URLs in order of input preference.
     * @return chosen URL, or `null` if there are no valid candidates.
     */
    fun pickBestProfilePictureUrl(vararg candidates: String?): String? {
        val valid = candidates.mapNotNull { it?.trim()?.takeIf { s -> s.isNotEmpty() } }
        return valid.firstOrNull { isUploadedProfilePhoto(it) }
            ?: valid.firstOrNull { !isDefaultGoogleAvatar(it) }
            ?: valid.firstOrNull()
    }

    /**
     * Indicates if the URL corresponds to a photo uploaded by the user (Cloudinary).
     *
     * @param url URL a evaluar.
     * @return `true` if the host is Cloudinary.
     */
    fun isUploadedProfilePhoto(url: String): Boolean =
        url.contains("cloudinary.com", ignoreCase = true)

    /**
     * Indicates whether the URL is the generic Google avatar after OAuth.
     *
     * @param url URL a evaluar.
     * @return `true` if the URL comes from Google-hosted domains.
     */
    fun isDefaultGoogleAvatar(url: String): Boolean =
        url.contains("googleusercontent.com", ignoreCase = true) ||
            url.contains("ggpht.com", ignoreCase = true)

    /**
     * Resolves the avatar URL from DTO fields and an optional fallback.
     *
     * @param profilePictureUrl Main photo field in the DTO.
     * @param profileImageUrl Alternative image URL field on the DTO.
     * @param fallback Fallback URL if previous ones are empty.
     * @return Best URL according to [pickBestProfilePictureUrl].
     */
    fun resolveProfilePictureUrl(
        profilePictureUrl: String?,
        profileImageUrl: String?,
        fallback: String? = null,
    ): String? = pickBestProfilePictureUrl(profilePictureUrl, profileImageUrl, fallback)

    /**
     * Maps the API response to the domain model [UserProfile].
     *
     * @param fallbackPictureUrl URL used if the DTO does not have a valid image.
     * @return Domain profile with parsed dates and enums.
     */
    fun UserProfileResponseDto.toDomain(fallbackPictureUrl: String? = null): UserProfile = UserProfile(
        id = id,
        name = name,
        email = email,
        authProvider = authProvider,
        profilePictureUrl = pickBestProfilePictureUrl(
            profilePictureUrl,
            profileImageUrl,
            fallbackPictureUrl,
        ),
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

    /** Persists essential profile fields in [UserProfileEntity].*/
    fun UserProfile.toEntity(): UserProfileEntity = UserProfileEntity(
        id = id,
        name = name,
        email = email,
        goalType = goalType?.name,
        fitnessLevel = fitnessLevel?.name,
        profilePictureUrl = profilePictureUrl,
    )

    /**
     * Rebuilds a [UserProfile] from local cache (partial data).
     *
     * @return Profile with fields not stored in Room as `null`.
     */
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

    /** Converts the profile creation request to the network DTO.*/
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

    /** Converts the profile update request to the network DTO.*/
    fun UpdateUserProfileRequest.toDto(): UpdateUserProfileRequestDto = UpdateUserProfileRequestDto(
        name = name,
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

    /** Map the result of onboarding (plans and nutritional objective) to the domain.*/
    fun OnboardingResultDto.toDomain(): OnboardingResult = OnboardingResult(
        trainingPlan = trainingPlan.toDomain(),
        dietPlan = dietPlan.toDomain(),
        nutritionTarget = nutritionTarget.toDomain(),
    )
}


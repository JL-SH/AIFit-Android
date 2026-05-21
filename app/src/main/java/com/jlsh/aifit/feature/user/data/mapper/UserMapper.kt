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
 * Conversiones entre DTO, entidad Room y modelos de dominio del usuario.
 */
object UserMapper {

    /**
     * Elige la mejor URL de avatar entre varios candidatos.
     *
     * Las fotos subidas (Cloudinary) tienen prioridad sobre el avatar por defecto de Google OAuth.
     *
     * @param candidates URLs en orden de preferencia de entrada.
     * @return URL elegida, o `null` si no hay candidatos válidos.
     */
    fun pickBestProfilePictureUrl(vararg candidates: String?): String? {
        val valid = candidates.mapNotNull { it?.trim()?.takeIf { s -> s.isNotEmpty() } }
        return valid.firstOrNull { isUploadedProfilePhoto(it) }
            ?: valid.firstOrNull { !isDefaultGoogleAvatar(it) }
            ?: valid.firstOrNull()
    }

    /**
     * Indica si la URL corresponde a una foto subida por el usuario (Cloudinary).
     *
     * @param url URL a evaluar.
     * @return `true` si el host es Cloudinary.
     */
    fun isUploadedProfilePhoto(url: String): Boolean =
        url.contains("cloudinary.com", ignoreCase = true)

    /**
     * Indica si la URL es el avatar genérico de Google tras OAuth.
     *
     * @param url URL a evaluar.
     * @return `true` si proviene de dominios de Google.
     */
    fun isDefaultGoogleAvatar(url: String): Boolean =
        url.contains("googleusercontent.com", ignoreCase = true) ||
            url.contains("ggpht.com", ignoreCase = true)

    /**
     * Resuelve la URL de avatar a partir de campos del DTO y un fallback opcional.
     *
     * @param profilePictureUrl Campo principal de foto en el DTO.
     * @param profileImageUrl Campo alternativo de imagen.
     * @param fallback URL de respaldo si los anteriores están vacíos.
     * @return Mejor URL según [pickBestProfilePictureUrl].
     */
    fun resolveProfilePictureUrl(
        profilePictureUrl: String?,
        profileImageUrl: String?,
        fallback: String? = null,
    ): String? = pickBestProfilePictureUrl(profilePictureUrl, profileImageUrl, fallback)

    /**
     * Mapea la respuesta de API al modelo de dominio [UserProfile].
     *
     * @param fallbackPictureUrl URL usada si el DTO no trae imagen válida.
     * @return Perfil de dominio con fechas y enums parseados.
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

    /** Persiste los campos esenciales del perfil en [UserProfileEntity]. */
    fun UserProfile.toEntity(): UserProfileEntity = UserProfileEntity(
        id = id,
        name = name,
        email = email,
        goalType = goalType?.name,
        fitnessLevel = fitnessLevel?.name,
        profilePictureUrl = profilePictureUrl,
    )

    /**
     * Reconstruye un [UserProfile] desde caché local (datos parciales).
     *
     * @return Perfil con campos no almacenados en Room en `null`.
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

    /** Convierte la petición de creación de perfil al DTO de red. */
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

    /** Convierte la petición de actualización de perfil al DTO de red. */
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

    /** Mapea el resultado de onboarding (planes y objetivo nutricional) a dominio. */
    fun OnboardingResultDto.toDomain(): OnboardingResult = OnboardingResult(
        trainingPlan = trainingPlan.toDomain(),
        dietPlan = dietPlan.toDomain(),
        nutritionTarget = nutritionTarget.toDomain(),
    )
}


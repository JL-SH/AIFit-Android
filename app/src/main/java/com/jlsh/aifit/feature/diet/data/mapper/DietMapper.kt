package com.jlsh.aifit.feature.diet.data.mapper

import com.jlsh.aifit.feature.diet.data.dto.DietDayResponseDto
import com.jlsh.aifit.feature.diet.data.dto.DietPlanResponseDto
import com.jlsh.aifit.feature.diet.data.dto.DietPlanSummaryResponseDto
import com.jlsh.aifit.feature.diet.data.dto.MealItemResponseDto
import com.jlsh.aifit.feature.diet.data.dto.MealResponseDto
import com.jlsh.aifit.feature.diet.data.local.DietPlanEntity
import com.jlsh.aifit.feature.diet.domain.model.DietDay
import com.jlsh.aifit.feature.diet.domain.model.DietPlan
import com.jlsh.aifit.feature.diet.domain.model.Meal
import com.jlsh.aifit.feature.diet.domain.model.MealItem
import com.jlsh.aifit.feature.diet.domain.model.MealType
import com.jlsh.aifit.feature.training.domain.model.PlanStatus
import com.jlsh.aifit.feature.user.domain.model.DietPreference
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

/**
 * Converts network DTOs and Room entities from the diet module to the domain model and vice versa.
 */
object DietMapper {

    private val isoFormatter: DateTimeFormatter = DateTimeFormatter.ISO_DATE_TIME

    /**
     * Maps a plan summary from the API to [DietPlan] without days (listings).
     *
     * @return Domain plan with empty [DietPlan.days].
     */
    fun DietPlanSummaryResponseDto.toDomain(): DietPlan = DietPlan(
        id = id,
        name = name,
        description = description,
        dailyCalories = dailyCalories,
        proteinGrams = proteinGrams,
        carbsGrams = carbsGrams,
        fatGrams = fatGrams,
        durationWeeks = durationWeeks,
        preference = DietPreference.fromString(preference),
        status = PlanStatus.fromString(status),
        totalDays = totalDays,
        createdAt = parseDateTime(createdAt),
        days = emptyList(),
    )

    /**
     * Maps the full detail of an API plan, including ordered days and meals.
     *
     * @return Domain plan with [DietPlan.days] populated.
     */
    fun DietPlanResponseDto.toDomain(): DietPlan = DietPlan(
        id = id,
        name = name,
        description = description,
        dailyCalories = dailyCalories,
        proteinGrams = proteinGrams,
        carbsGrams = carbsGrams,
        fatGrams = fatGrams,
        durationWeeks = durationWeeks,
        preference = DietPreference.fromString(preference),
        status = PlanStatus.fromString(status),
        totalDays = totalDays ?: days.size,
        createdAt = parseDateTime(createdAt),
        days = days.map { it.toDomain() }.sortedBy { it.dayNumber },
    )

    /**
     * Maps a diet day from the API to the domain model.
     */
    fun DietDayResponseDto.toDomain(): DietDay = DietDay(
        id = id,
        dayNumber = dayNumber,
        name = name,
        totalCalories = totalCalories,
        meals = meals.map { it.toDomain() },
    )

    /**
     * Maps a planned meal from the API to the domain model.
     */
    fun MealResponseDto.toDomain(): Meal = Meal(
        id = id,
        mealType = MealType.fromString(mealType),
        name = name,
        time = time,
        calories = calories,
        proteinGrams = proteinGrams,
        carbsGrams = carbsGrams,
        fatGrams = fatGrams,
        items = items.map { it.toDomain() },
    )

    /**
     * Maps a food within a planned meal to the domain model.
     */
    fun MealItemResponseDto.toDomain(): MealItem = MealItem(
        id = id,
        name = name,
        quantity = quantity,
        unit = unit,
        calories = calories,
        proteinGrams = proteinGrams,
        carbsGrams = carbsGrams,
        fatGrams = fatGrams,
    )

    /**
     * A domain plan persists in Room associated with [userId].
     *
     * @param userId Identifier of the user who owns the plan.
     */
    fun DietPlan.toEntity(userId: String): DietPlanEntity = DietPlanEntity(
        id = id,
        userId = userId,
        name = name,
        description = description,
        dailyCalories = dailyCalories,
        proteinGrams = proteinGrams,
        carbsGrams = carbsGrams,
        fatGrams = fatGrams,
        durationWeeks = durationWeeks,
        preference = preference.name,
        status = status.name,
        totalDays = totalDays,
        createdAt = createdAt.toInstant(ZoneOffset.UTC).toEpochMilli(),
    )

    /**
     * Restore a plan from Room; days are not stored in the summary entity.
     */
    fun DietPlanEntity.toDomain(): DietPlan = DietPlan(
        id = id,
        name = name,
        description = description,
        dailyCalories = dailyCalories,
        proteinGrams = proteinGrams,
        carbsGrams = carbsGrams,
        fatGrams = fatGrams,
        durationWeeks = durationWeeks,
        preference = DietPreference.fromString(preference),
        status = PlanStatus.fromString(status),
        totalDays = totalDays,
        createdAt = Instant.ofEpochMilli(createdAt).atZone(ZoneOffset.UTC).toLocalDateTime(),
        days = emptyList(),
    )

    private fun parseDateTime(raw: String): LocalDateTime =
        runCatching { LocalDateTime.parse(raw, isoFormatter) }
            .getOrDefault(LocalDateTime.now())
}

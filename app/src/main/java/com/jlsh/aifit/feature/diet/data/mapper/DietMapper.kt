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
 * Convierte DTOs de red y entidades Room del módulo de dieta al modelo de dominio y viceversa.
 */
object DietMapper {

    private val isoFormatter: DateTimeFormatter = DateTimeFormatter.ISO_DATE_TIME

    /**
     * Mapea un resumen de plan de la API a [DietPlan] sin días (listados).
     *
     * @return Plan de dominio con [DietPlan.days] vacío.
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
     * Mapea el detalle completo de un plan de la API, incluyendo días y comidas ordenados.
     *
     * @return Plan de dominio con [DietPlan.days] poblados.
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
     * Mapea un día de dieta de la API al modelo de dominio.
     */
    fun DietDayResponseDto.toDomain(): DietDay = DietDay(
        id = id,
        dayNumber = dayNumber,
        name = name,
        totalCalories = totalCalories,
        meals = meals.map { it.toDomain() },
    )

    /**
     * Mapea una comida planificada de la API al modelo de dominio.
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
     * Mapea un alimento dentro de una comida planificada al modelo de dominio.
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
     * Persiste un plan de dominio en Room asociado al [userId].
     *
     * @param userId Identificador del usuario propietario del plan.
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
     * Restaura un plan desde Room; los días no se almacenan en la entidad de resumen.
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

package com.jlsh.aifit.feature.nutrition.data.mapper

import com.jlsh.aifit.feature.diet.domain.model.MealType
import com.jlsh.aifit.feature.nutrition.data.dto.FoodItemLogResponseDto
import com.jlsh.aifit.feature.nutrition.data.dto.MealLogResponseDto
import com.jlsh.aifit.feature.nutrition.data.dto.NutritionLogResponseDto
import com.jlsh.aifit.feature.nutrition.data.dto.NutritionTargetResponseDto
import com.jlsh.aifit.feature.nutrition.data.local.NutritionLogEntity
import com.jlsh.aifit.feature.nutrition.data.local.NutritionTargetEntity
import com.jlsh.aifit.feature.nutrition.domain.model.FoodItemLog
import com.jlsh.aifit.feature.nutrition.domain.model.MealLog
import com.jlsh.aifit.feature.nutrition.domain.model.NutritionLog
import com.jlsh.aifit.feature.nutrition.domain.model.NutritionTarget
import com.jlsh.aifit.feature.nutrition.domain.model.TargetSource
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Convierte DTOs de red y entidades Room del módulo de nutrición al modelo de dominio y viceversa.
 */
object NutritionMapper {

    private val dateFormatter: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    /**
     * Mapea el registro nutricional diario de la API al modelo de dominio.
     */
    fun NutritionLogResponseDto.toDomain(): NutritionLog = NutritionLog(
        id = id,
        date = parseDate(date),
        totalCalories = totalCalories,
        totalProteinGrams = totalProteinGrams,
        totalCarbsGrams = totalCarbsGrams,
        totalFatGrams = totalFatGrams,
        meals = meals.map { it.toDomain() },
    )

    /**
     * Mapea una comida registrada de la API al modelo de dominio.
     */
    fun MealLogResponseDto.toDomain(): MealLog = MealLog(
        id = id,
        mealType = MealType.fromString(mealType),
        name = name,
        time = time,
        calories = calories,
        proteinGrams = proteinGrams,
        carbsGrams = carbsGrams,
        fatGrams = fatGrams,
        aiGenerated = aiGenerated,
        rawInputText = rawInputText,
        items = items.map { it.toDomain() },
    )

    /**
     * Mapea un alimento dentro de una comida registrada al modelo de dominio.
     */
    fun FoodItemLogResponseDto.toDomain(): FoodItemLog = FoodItemLog(
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
     * Mapea los objetivos nutricionales de la API al modelo de dominio.
     */
    fun NutritionTargetResponseDto.toDomain(): NutritionTarget = NutritionTarget(
        id = id,
        calorieTarget = calorieTarget,
        proteinTarget = proteinTarget,
        carbsTarget = carbsTarget,
        fatTarget = fatTarget,
        effectiveFrom = parseDate(effectiveFrom),
        setBy = TargetSource.fromString(setBy),
    )

    /**
     * Persiste un registro diario en Room (totales agregados; sin comidas en la entidad).
     */
    fun NutritionLog.toEntity(): NutritionLogEntity = NutritionLogEntity(
        id = id,
        date = date.toEpochDay(),
        totalCalories = totalCalories,
        totalProteinGrams = totalProteinGrams,
        totalCarbsGrams = totalCarbsGrams,
        totalFatGrams = totalFatGrams,
    )

    /**
     * Restaura un registro diario desde Room; [NutritionLog.meals] queda vacío.
     */
    fun NutritionLogEntity.toDomain(): NutritionLog = NutritionLog(
        id = id,
        date = LocalDate.ofEpochDay(date),
        totalCalories = totalCalories,
        totalProteinGrams = totalProteinGrams,
        totalCarbsGrams = totalCarbsGrams,
        totalFatGrams = totalFatGrams,
    )

    /**
     * Persiste objetivos nutricionales en Room.
     */
    fun NutritionTarget.toEntity(): NutritionTargetEntity = NutritionTargetEntity(
        id = id,
        calorieTarget = calorieTarget,
        proteinTarget = proteinTarget,
        carbsTarget = carbsTarget,
        fatTarget = fatTarget,
        effectiveFrom = effectiveFrom.toEpochDay(),
        setBy = setBy.name,
    )

    /**
     * Restaura objetivos nutricionales desde Room.
     */
    fun NutritionTargetEntity.toDomain(): NutritionTarget = NutritionTarget(
        id = id,
        calorieTarget = calorieTarget,
        proteinTarget = proteinTarget,
        carbsTarget = carbsTarget,
        fatTarget = fatTarget,
        effectiveFrom = LocalDate.ofEpochDay(effectiveFrom),
        setBy = TargetSource.fromString(setBy),
    )

    private fun parseDate(raw: String): LocalDate =
        runCatching { LocalDate.parse(raw, dateFormatter) }
            .getOrDefault(LocalDate.now())
}

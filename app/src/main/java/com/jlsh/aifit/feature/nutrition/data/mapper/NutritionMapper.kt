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

object NutritionMapper {

    private val dateFormatter: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    fun NutritionLogResponseDto.toDomain(): NutritionLog = NutritionLog(
        id = id,
        date = parseDate(date),
        totalCalories = totalCalories,
        totalProteinGrams = totalProteinGrams,
        totalCarbsGrams = totalCarbsGrams,
        totalFatGrams = totalFatGrams,
        meals = meals.map { it.toDomain() },
    )

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

    fun NutritionTargetResponseDto.toDomain(): NutritionTarget = NutritionTarget(
        id = id,
        calorieTarget = calorieTarget,
        proteinTarget = proteinTarget,
        carbsTarget = carbsTarget,
        fatTarget = fatTarget,
        effectiveFrom = parseDate(effectiveFrom),
        setBy = TargetSource.fromString(setBy),
    )

    fun NutritionLog.toEntity(): NutritionLogEntity = NutritionLogEntity(
        id = id,
        date = date.toEpochDay(),
        totalCalories = totalCalories,
        totalProteinGrams = totalProteinGrams,
        totalCarbsGrams = totalCarbsGrams,
        totalFatGrams = totalFatGrams,
    )

    fun NutritionLogEntity.toDomain(): NutritionLog = NutritionLog(
        id = id,
        date = LocalDate.ofEpochDay(date),
        totalCalories = totalCalories,
        totalProteinGrams = totalProteinGrams,
        totalCarbsGrams = totalCarbsGrams,
        totalFatGrams = totalFatGrams,
    )

    fun NutritionTarget.toEntity(): NutritionTargetEntity = NutritionTargetEntity(
        id = id,
        calorieTarget = calorieTarget,
        proteinTarget = proteinTarget,
        carbsTarget = carbsTarget,
        fatTarget = fatTarget,
        effectiveFrom = effectiveFrom.toEpochDay(),
        setBy = setBy.name,
    )

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


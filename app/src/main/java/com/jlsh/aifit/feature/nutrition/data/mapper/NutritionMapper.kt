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
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Converts network DTOs and Room entities from the nutrition module to the domain model and vice versa.
 */
object NutritionMapper {

    private val dateFormatter: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    private val mealsJson = Json { ignoreUnknownKeys = true }
    private val mealsListSerializer = ListSerializer(MealLogResponseDto.serializer())

    /**
     * Maps the daily nutrition log from the API to the domain model.
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
     * Maps a registered meal from the API to the domain model.
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
     * Maps a food within a registered food to the domain model.
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
     * Maps nutritional objectives from the API to the domain model.
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

    fun MealLog.toResponseDto(): MealLogResponseDto = MealLogResponseDto(
        id = id,
        mealType = mealType.name,
        name = name,
        time = time,
        calories = calories,
        proteinGrams = proteinGrams,
        carbsGrams = carbsGrams,
        fatGrams = fatGrams,
        aiGenerated = aiGenerated,
        rawInputText = rawInputText,
        items = items.map { it.toResponseDto() },
    )

    fun FoodItemLog.toResponseDto(): FoodItemLogResponseDto = FoodItemLogResponseDto(
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
     * Persists a daily record in Room including serialized meals for offline display.
     */
    fun NutritionLog.toEntity(): NutritionLogEntity = NutritionLogEntity(
        id = id,
        date = date.toEpochDay(),
        totalCalories = totalCalories,
        totalProteinGrams = totalProteinGrams,
        totalCarbsGrams = totalCarbsGrams,
        totalFatGrams = totalFatGrams,
        mealsJson = encodeMealsJson(meals),
    )

    /**
     * Restores a daily log from Room, including cached meals when available.
     */
    fun NutritionLogEntity.toDomain(): NutritionLog = NutritionLog(
        id = id,
        date = LocalDate.ofEpochDay(date),
        totalCalories = totalCalories,
        totalProteinGrams = totalProteinGrams,
        totalCarbsGrams = totalCarbsGrams,
        totalFatGrams = totalFatGrams,
        meals = decodeMealsJson(mealsJson),
    )

    fun encodeMealsJson(meals: List<MealLog>): String? {
        if (meals.isEmpty()) return null
        return mealsJson.encodeToString(mealsListSerializer, meals.map { it.toResponseDto() })
    }

    fun decodeMealsJson(raw: String?): List<MealLog> {
        if (raw.isNullOrBlank()) return emptyList()
        return runCatching {
            mealsJson.decodeFromString(mealsListSerializer, raw).map { it.toDomain() }
        }.getOrDefault(emptyList())
    }

    /**
     * Persist nutritional goals in Room.
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
     * Restore nutritional goals from Room.
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

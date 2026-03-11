package com.jlsh.aifit.feature.shopping.data.mapper

import com.jlsh.aifit.feature.shopping.data.dto.ShoppingCategoryGroupResponseDto
import com.jlsh.aifit.feature.shopping.data.dto.ShoppingItemResponseDto
import com.jlsh.aifit.feature.shopping.data.dto.ShoppingListResponseDto
import com.jlsh.aifit.feature.shopping.data.local.ShoppingListEntity
import com.jlsh.aifit.feature.shopping.domain.model.ShoppingCategory
import com.jlsh.aifit.feature.shopping.domain.model.ShoppingCategoryGroup
import com.jlsh.aifit.feature.shopping.domain.model.ShoppingItem
import com.jlsh.aifit.feature.shopping.domain.model.ShoppingList
import com.jlsh.aifit.feature.shopping.domain.model.ShoppingListPeriod
import java.time.Instant

object ShoppingMapper {

    fun ShoppingListResponseDto.toDomain(): ShoppingList = ShoppingList(
        id = id,
        dietPlanId = dietPlanId,
        period = ShoppingListPeriod.fromString(period),
        categories = categories.map { it.toDomain() },
        generatedAt = generatedAt,
    )

    fun ShoppingCategoryGroupResponseDto.toDomain(): ShoppingCategoryGroup =
        ShoppingCategoryGroup(
            category = ShoppingCategory.fromString(category),
            items = items.map { it.toDomain() },
        )

    fun ShoppingItemResponseDto.toDomain(): ShoppingItem = ShoppingItem(
        name = name,
        totalQuantity = totalQuantity,
        unit = unit,
        notes = notes,
        isChecked = false,
    )

    fun ShoppingListResponseDto.toEntity(): ShoppingListEntity = ShoppingListEntity(
        id = id,
        dietPlanId = dietPlanId,
        period = period,
        generatedAt = parseInstant(generatedAt),
    )

    fun ShoppingListEntity.toDomain(): ShoppingList = ShoppingList(
        id = id,
        dietPlanId = dietPlanId,
        period = ShoppingListPeriod.fromString(period),
        categories = emptyList(),
        generatedAt = Instant.ofEpochMilli(generatedAt).toString(),
    )

    private fun parseInstant(value: String): Long =
        runCatching { Instant.parse(value).toEpochMilli() }.getOrDefault(0L)
}


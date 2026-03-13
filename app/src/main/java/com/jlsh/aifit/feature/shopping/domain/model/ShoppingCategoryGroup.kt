package com.jlsh.aifit.feature.shopping.domain.model

enum class ShoppingCategory {
    PROTEINS,
    VEGETABLES,
    FRUITS,
    GRAINS_AND_CARBS,
    DAIRY,
    FATS_AND_OILS,
    CONDIMENTS_AND_SPICES,
    OTHER,
    UNKNOWN;

    companion object {
        fun fromString(value: String?): ShoppingCategory =
            value?.let { runCatching { valueOf(it) }.getOrDefault(UNKNOWN) } ?: UNKNOWN
    }
}

data class ShoppingCategoryGroup(
    val category: ShoppingCategory,
    val items: List<ShoppingItem>,
)


package com.jlsh.aifit.feature.shopping.domain.model

enum class ShoppingCategory {
    PROTEINS,
    VEGETABLES,
    FRUITS,
    GRAINS,
    DAIRY,
    FATS_OILS,
    CONDIMENTS,
    BEVERAGES,
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


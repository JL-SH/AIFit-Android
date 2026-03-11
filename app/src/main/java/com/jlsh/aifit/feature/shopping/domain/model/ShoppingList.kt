package com.jlsh.aifit.feature.shopping.domain.model

enum class ShoppingListPeriod {
    ONE_WEEK,
    TWO_WEEKS,
    ONE_MONTH,
    WEEKLY,
    BIWEEKLY,
    MONTHLY,
    UNKNOWN;

    companion object {
        fun fromString(value: String?): ShoppingListPeriod =
            value?.let { runCatching { valueOf(it) }.getOrDefault(UNKNOWN) } ?: UNKNOWN
    }
}

data class ShoppingList(
    val id: String,
    val dietPlanId: String,
    val period: ShoppingListPeriod,
    val categories: List<ShoppingCategoryGroup>,
    val generatedAt: String,
)


package com.jlsh.aifit.feature.user.domain.model

enum class DietPreference {
    NONE,
    VEGETARIAN,
    VEGAN,
    KETO,
    PALEO,
    GLUTEN_FREE,
    LACTOSE_FREE,
    MEDITERRANEAN,
    UNKNOWN;

    companion object {
        fun fromString(value: String?): DietPreference =
            value?.let { runCatching { valueOf(it) }.getOrDefault(UNKNOWN) } ?: UNKNOWN
    }
}


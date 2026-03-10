package com.jlsh.aifit.feature.user.domain.model

enum class DietPreference {
    STANDARD,
    VEGETARIAN,
    VEGAN,
    KETO,
    PALEO,
    MEDITERRANEAN,
    GLUTEN_FREE,
    DAIRY_FREE,
    UNKNOWN;

    companion object {
        fun fromString(value: String?): DietPreference =
            value?.let { runCatching { valueOf(it) }.getOrDefault(UNKNOWN) } ?: UNKNOWN
    }
}


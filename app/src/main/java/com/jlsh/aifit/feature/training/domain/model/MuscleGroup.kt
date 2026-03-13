package com.jlsh.aifit.feature.training.domain.model

enum class MuscleGroup {
    CHEST,
    BACK,
    SHOULDERS,
    BICEPS,
    TRICEPS,
    FOREARMS,
    LEGS,
    GLUTES,
    CORE,
    FULL_BODY,
    CARDIO,
    UNKNOWN;

    companion object {
        fun fromString(value: String?): MuscleGroup =
            value?.let { runCatching { valueOf(it) }.getOrDefault(UNKNOWN) } ?: UNKNOWN
    }
}


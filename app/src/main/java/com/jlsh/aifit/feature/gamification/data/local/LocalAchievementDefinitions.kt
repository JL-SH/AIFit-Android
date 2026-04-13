package com.jlsh.aifit.feature.gamification.data.local

import com.jlsh.aifit.feature.gamification.domain.model.AchievementDefinition
import com.jlsh.aifit.feature.gamification.domain.model.AchievementRarity
import com.jlsh.aifit.feature.gamification.domain.model.AchievementType

/**
 * Lista local y estática de todas las definiciones de logros de la aplicación.
 *
 * Se usa como fuente de verdad para mostrar los "próximos logros" (bloqueados) cuando
 * el endpoint del backend no está disponible o devuelve error. El campo [AchievementDefinition.code]
 * se usa para hacer el matching con los logros que el usuario ya ha desbloqueado (que llegan
 * del servidor con su `code` real). Los IDs locales solo se usan para la lista de bloqueados
 * y no necesitan coincidir con los IDs del servidor.
 */
internal object LocalAchievementDefinitions {

    val all: List<AchievementDefinition> = listOf(

        // ── COMMON ────────────────────────────────────────────────────────────────

        AchievementDefinition(
            id = "local_first_workout",
            code = "FIRST_WORKOUT",
            type = AchievementType.ADHERENCE_STREAK,
            name = "Primer entrenamiento",
            description = "Completa tu primera sesión de entrenamiento registrada en la app.",
            rarity = AchievementRarity.COMMON,
            iconKey = "fitness_center",
        ),
        AchievementDefinition(
            id = "local_first_training_plan",
            code = "FIRST_TRAINING_PLAN",
            type = AchievementType.FIRST_PLAN_COMPLETED,
            name = "Plan creado",
            description = "Genera tu primer plan de entrenamiento personalizado con IA.",
            rarity = AchievementRarity.COMMON,
            iconKey = "assignment",
        ),
        AchievementDefinition(
            id = "local_first_diet_plan",
            code = "FIRST_DIET_PLAN",
            type = AchievementType.FIRST_PLAN_COMPLETED,
            name = "Primer plan de dieta",
            description = "Genera tu primer plan de dieta personalizado con IA.",
            rarity = AchievementRarity.COMMON,
            iconKey = "restaurant",
        ),

        // ── UNCOMMON ──────────────────────────────────────────────────────────────

        AchievementDefinition(
            id = "local_streak_7",
            code = "STREAK_7",
            type = AchievementType.ADHERENCE_STREAK,
            name = "Semana de fuego 🔥",
            description = "Entrena 7 días consecutivos sin saltarte ninguno.",
            rarity = AchievementRarity.UNCOMMON,
            iconKey = "whatshot",
        ),
        AchievementDefinition(
            id = "local_first_pr",
            code = "FIRST_PR",
            type = AchievementType.PERSONAL_RECORD,
            name = "Primer récord personal",
            description = "Supera tu marca personal en cualquier ejercicio por primera vez.",
            rarity = AchievementRarity.UNCOMMON,
            iconKey = "emoji_events",
        ),
        AchievementDefinition(
            id = "local_nutrition_7",
            code = "NUTRITION_7",
            type = AchievementType.NUTRITION_CONSISTENCY,
            name = "Constancia nutricional",
            description = "Sigue tu plan de nutrición durante 7 días consecutivos.",
            rarity = AchievementRarity.UNCOMMON,
            iconKey = "local_dining",
        ),
        AchievementDefinition(
            id = "local_knowledge_first",
            code = "KNOWLEDGE_FIRST",
            type = AchievementType.KNOWLEDGE_ACQUIRED,
            name = "Alumno aventajado",
            description = "Consulta tu primer análisis de IA sobre tu progreso.",
            rarity = AchievementRarity.UNCOMMON,
            iconKey = "school",
        ),

        // ── RARE ──────────────────────────────────────────────────────────────────

        AchievementDefinition(
            id = "local_streak_30",
            code = "STREAK_30",
            type = AchievementType.ADHERENCE_STREAK,
            name = "Mes de hierro",
            description = "Entrena 30 días consecutivos sin descanso.",
            rarity = AchievementRarity.RARE,
            iconKey = "whatshot",
        ),
        AchievementDefinition(
            id = "local_plan_completed",
            code = "FIRST_PLAN_COMPLETE",
            type = AchievementType.FIRST_PLAN_COMPLETED,
            name = "Plan completado",
            description = "Finaliza un plan de entrenamiento de principio a fin.",
            rarity = AchievementRarity.RARE,
            iconKey = "done_all",
        ),
        AchievementDefinition(
            id = "local_4_consecutive_weeks",
            code = "CONSECUTIVE_4_WEEKS",
            type = AchievementType.CONSECUTIVE_WEEKS,
            name = "4 semanas seguidas",
            description = "Completa 4 semanas enteras de entrenamiento sin saltarte ningún día planificado.",
            rarity = AchievementRarity.RARE,
            iconKey = "calendar_today",
        ),
        AchievementDefinition(
            id = "local_pr_5",
            code = "PR_5",
            type = AchievementType.PERSONAL_RECORD,
            name = "Coleccionista de récords",
            description = "Establece récords personales en 5 ejercicios diferentes.",
            rarity = AchievementRarity.RARE,
            iconKey = "military_tech",
        ),
        AchievementDefinition(
            id = "local_weight_progress",
            code = "WEIGHT_GOAL_PROGRESS",
            type = AchievementType.WEIGHT_GOAL,
            name = "En el buen camino",
            description = "Registra progreso de peso constante durante 4 semanas hacia tu objetivo.",
            rarity = AchievementRarity.RARE,
            iconKey = "trending_down",
        ),

        // ── LEGENDARY ─────────────────────────────────────────────────────────────

        AchievementDefinition(
            id = "local_streak_100",
            code = "STREAK_100",
            type = AchievementType.ADHERENCE_STREAK,
            name = "Centenario de acero 💯",
            description = "Entrena 100 días consecutivos sin faltar ni uno.",
            rarity = AchievementRarity.LEGENDARY,
            iconKey = "whatshot",
        ),
        AchievementDefinition(
            id = "local_weight_goal",
            code = "WEIGHT_GOAL_REACHED",
            type = AchievementType.WEIGHT_GOAL,
            name = "Objetivo conseguido 🏆",
            description = "Alcanza el objetivo de peso que estableciste en tu perfil.",
            rarity = AchievementRarity.LEGENDARY,
            iconKey = "emoji_events",
        ),
        AchievementDefinition(
            id = "local_strength_master",
            code = "STRENGTH_MASTER",
            type = AchievementType.STRENGTH_MILESTONE,
            name = "Maestro de la fuerza",
            description = "Alcanza el nivel avanzado de fuerza en al menos 3 ejercicios compuestos.",
            rarity = AchievementRarity.LEGENDARY,
            iconKey = "fitness_center",
        ),
    )
}


package com.jlsh.aifit.navigation

object AuthRoutes {
    const val GRAPH = "auth"
    const val LOGIN = "auth/login"
    const val REGISTER = "auth/register"
    const val CREATE_PROFILE = "auth/create_profile"
    const val ONBOARDING_GENERATING = "auth/onboarding_generating"
    const val ONBOARDING_TRAINING_APPROVAL = "auth/onboarding_training_approval"
    const val ONBOARDING_NUTRITION_APPROVAL = "auth/onboarding_nutrition_approval"
}

object HomeRoutes {
    const val GRAPH = "home_graph"
    const val HOME = "home"
    const val DASHBOARD = "home/dashboard"
    const val BODY_WEIGHT = "home/body_weight"
    const val WEEKLY_SUMMARY = "home/weekly_summary"
    const val METABOLIC_ANALYSIS = "home/metabolic_analysis"
}

object TrainingRoutes {
    const val GRAPH = "training_graph"
    const val HUB = "training"
    const val DETAIL = "training/detail/{planId}"
    const val GENERATE = "training/generate?adaptive={adaptive}&basePlanId={basePlanId}"
    const val APPROVAL = "training/approval/{planId}"
    const val WORKOUT_SESSION = "training/session/{planId}/{dayId}"
    const val WORKOUT_LOG = "training/workout_log?planId={planId}"
    const val WORKOUT_DETAIL = "training/workout_detail/{logId}"
    const val WORKOUT_HISTORY = "training/workout_history"

    fun detailRoute(planId: String) = "training/detail/$planId"
    fun generateRoute(adaptive: Boolean = false, basePlanId: String? = null) =
        "training/generate?adaptive=$adaptive&basePlanId=${basePlanId ?: ""}"
    fun approvalRoute(planId: String) = "training/approval/$planId"
    fun workoutSessionRoute(planId: String, dayId: String) =
        "training/session/$planId/$dayId"
    fun workoutLogRoute(planId: String? = null) =
        "training/workout_log?planId=${planId ?: ""}"
    fun workoutDetailRoute(logId: String) = "training/workout_detail/$logId"
}

object NutritionRoutes {
    const val GRAPH = "nutrition_graph"
    const val HUB = "nutrition"
    const val TRACK_MEAL = "nutrition/track_meal?mode={mode}&prefilled={prefilled}"
    const val FOOD_VISION = "nutrition/food_vision"
    const val TARGET = "nutrition/target"
    const val DIET_DETAIL = "nutrition/diet_detail/{planId}"
    const val DIET_GENERATE = "nutrition/diet_generate?adaptive={adaptive}&basePlanId={basePlanId}"
    const val DIET_APPROVAL = "nutrition/diet/approval/{planId}"
    const val SHOPPING_DETAIL = "nutrition/shopping_detail/{listId}"

    fun trackMealRoute(mode: String = "", prefilled: String = "") =
        "nutrition/track_meal?mode=$mode&prefilled=$prefilled"
    fun dietDetailRoute(planId: String) = "nutrition/diet_detail/$planId"
    fun dietGenerateRoute(adaptive: Boolean = false, basePlanId: String? = null) =
        "nutrition/diet_generate?adaptive=$adaptive&basePlanId=${basePlanId ?: ""}"
    fun dietApprovalRoute(planId: String) = "nutrition/diet/approval/$planId"
    fun shoppingDetailRoute(listId: String) = "nutrition/shopping_detail/$listId"
}

object CoachRoutes {
    const val GRAPH = "coach_graph"
    const val SESSION_LIST = "coach"
    const val CHAT = "coach/chat/{sessionId}"
    const val NEW_CHAT = "coach/new_chat"

    fun chatRoute(sessionId: String) = "coach/chat/$sessionId"
    fun newChatRoute() = NEW_CHAT
}

object ProfileRoutes {
    const val GRAPH = "profile_graph"
    const val HUB = "profile"
    const val EDIT = "profile/edit?mode={mode}"

    fun editRoute() = "profile/edit?mode=edit"
    const val DASHBOARD = "profile/dashboard"
    const val BODY_WEIGHT = "profile/body_weight"
    const val WEEKLY_SUMMARY = "profile/weekly_summary"
    const val METABOLIC = "profile/metabolic"
    const val EXPORT = "profile/export"
    const val GAMIFICATION = "profile/gamification?tab={tab}"
    const val GLOSSARY = "profile/glossary"

    fun gamificationRoute(tab: String = "ACHIEVEMENTS") =
        "profile/gamification?tab=$tab"
}

object MainRoutes {
    const val GRAPH = "main"
}


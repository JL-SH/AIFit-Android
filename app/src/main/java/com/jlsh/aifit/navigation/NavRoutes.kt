package com.jlsh.aifit.navigation

/** Authentication and onboarding graph routes.*/
object AuthRoutes {
    /** Root path of the auth subgraph.*/
    const val GRAPH = "auth"
    /** Login screen.*/
    const val LOGIN = "auth/login"
    /** Account registration screen.*/
    const val REGISTER = "auth/register"
    /** Creation or completion of the user profile.*/
    const val CREATE_PROFILE = "auth/create_profile"
    /** Generation of initial plans during onboarding.*/
    const val ONBOARDING_GENERATING = "auth/onboarding_generating"
    /** Approval of the training plan generated in onboarding.*/
    const val ONBOARDING_TRAINING_APPROVAL = "auth/onboarding_training_approval"
    /** Approval of the nutritional plan generated in onboarding.*/
    const val ONBOARDING_NUTRITION_APPROVAL = "auth/onboarding_nutrition_approval"
}

/** Home tab subgraph routes.*/
object HomeRoutes {
    /** Root path of the home subgraph.*/
    const val GRAPH = "home_graph"
    /** Dashboard principal. */
    const val HOME = "home"
    /** Weekly progress dashboard.*/
    const val DASHBOARD = "home/dashboard"
    /** History and record of body weight.*/
    const val BODY_WEIGHT = "home/body_weight"
    /** Detailed weekly summary.*/
    const val WEEKLY_SUMMARY = "home/weekly_summary"
    /** Metabolic analysis.*/
    const val METABOLIC_ANALYSIS = "home/metabolic_analysis"
}

/** Training tab subgraph routes.*/
object TrainingRoutes {
    /** Root path of the training subgraph.*/
    const val GRAPH = "training_graph"
    /** Hub de planes de entrenamiento. */
    const val HUB = "training"
    /** Detail of a plan; requires `planId` argument.*/
    const val DETAIL = "training/detail/{planId}"
    /** Plan generation; query `adaptive` and `basePlanId`.*/
    const val GENERATE = "training/generate?adaptive={adaptive}&basePlanId={basePlanId}"
    /** Approval of generated plan; requires `planId`.*/
    const val APPROVAL = "training/approval/{planId}"
    /** Training session in progress; requires `planId` and `dayId`.*/
    const val WORKOUT_SESSION = "training/session/{planId}/{dayId}"
    /** Manual training record; optional query `planId`.*/
    const val WORKOUT_LOG = "training/workout_log?planId={planId}"
    /** Detail of a training log; requires `logId`.*/
    const val WORKOUT_DETAIL = "training/workout_detail/{logId}"
    /** Training history.*/
    const val WORKOUT_HISTORY = "training/workout_history"

    /**
     * Specific route to the detail of a plan.
     *
     * @param planId Plan identifier.
     * @return Resolved route for navigation.
     */
    fun detailRoute(planId: String) = "training/detail/$planId"

    /**
     * Path to the plan generation flow.
     *
     * @param adaptive Whether it is an adaptive plan over an existing one.
     * @param basePlanId Base plan for adaptive mode; empty if not applicable.
     * @return Route with encoded query params.
     */
    fun generateRoute(adaptive: Boolean = false, basePlanId: String? = null) =
        "training/generate?adaptive=$adaptive&basePlanId=${basePlanId ?: ""}"

    /**
     * Path to the approval screen after generating a plan.
     *
     * @param planId Identifier of the generated plan.
     * @return Resolved route.
     */
    fun approvalRoute(planId: String) = "training/approval/$planId"

    /**
     * Route to the active training session.
     *
     * @param planId Plan identifier.
     * @param dayId Identifier of the training day.
     * @return Resolved route.
     */
    fun workoutSessionRoute(planId: String, dayId: String) =
        "training/session/$planId/$dayId"

    /**
     * Path to the manual training record.
     *
     * @param planId Associated plan, or null for registration without a plan.
     * @return Route with query `planId`.
     */
    fun workoutLogRoute(planId: String? = null) =
        "training/workout_log?planId=${planId ?: ""}"

    /**
     * Route to the detail of a completed training log.
     *
     * @param logId Log identifier.
     * @return Resolved route.
     */
    fun workoutDetailRoute(logId: String) = "training/workout_detail/$logId"
}

/** Nutrition tab subgraph routes.*/
object NutritionRoutes {
    /** Root path of the nutrition subgraph.*/
    const val GRAPH = "nutrition_graph"
    /** Nutrition and diet plans hub.*/
    const val HUB = "nutrition"
    /** Food record; query `mode` and `prefilled`.*/
    const val TRACK_MEAL = "nutrition/track_meal?mode={mode}&prefilled={prefilled}"
    /** Food recognition by camera.*/
    const val FOOD_VISION = "nutrition/food_vision"
    /** Nutritional goals of the user.*/
    const val TARGET = "nutrition/target"
    /** Diet plan details; requires `planId`.*/
    const val DIET_DETAIL = "nutrition/diet_detail/{planId}"
    /** Diet plan generation; query `adaptive` and `basePlanId`.*/
    const val DIET_GENERATE = "nutrition/diet_generate?adaptive={adaptive}&basePlanId={basePlanId}"
    /** Approval of generated dietary plan; requires `planId`.*/
    const val DIET_APPROVAL = "nutrition/diet/approval/{planId}"
    /** Shopping list details; requires `listId`.*/
    const val SHOPPING_DETAIL = "nutrition/shopping_detail/{listId}"

    /**
     * Route to food registration.
     *
     * @param mode Entry mode (e.g. manual).
     * @param prefilled Data prefilled from vision, encoded in the query.
     * @return Resolved route.
     */
    fun trackMealRoute(mode: String = "", prefilled: String = "") =
        "nutrition/track_meal?mode=$mode&prefilled=$prefilled"

    /**
     * Route to the details of a dietary plan.
     *
     * @param planId Plan identifier.
     * @return Resolved route.
     */
    fun dietDetailRoute(planId: String) = "nutrition/diet_detail/$planId"

    /**
     * Path to the diet plan generation flow.
     *
     * @param adaptive Adaptive plan over an existing one.
     * @param basePlanId Base Plan; empty if not applicable.
     * @return Route with query params.
     */
    fun dietGenerateRoute(adaptive: Boolean = false, basePlanId: String? = null) =
        "nutrition/diet_generate?adaptive=$adaptive&basePlanId=${basePlanId ?: ""}"

    /**
     * Route to approval of a generated dietary plan.
     *
     * @param planId Plan identifier.
     * @return Resolved route.
     */
    fun dietApprovalRoute(planId: String) = "nutrition/diet/approval/$planId"

    /**
     * Route to the detail of a shopping list.
     *
     * @param listId Identifier of the list.
     * @return Resolved route.
     */
    fun shoppingDetailRoute(listId: String) = "nutrition/shopping_detail/$listId"
}

/** Coach (chat) tab subgraph routes.*/
object CoachRoutes {
    /** Root path of the coach subgraph.*/
    const val GRAPH = "coach_graph"
    /** List of chat sessions.*/
    const val SESSION_LIST = "coach"
    /** Chat with existing session; requires `sessionId`.*/
    const val CHAT = "coach/chat/{sessionId}"
    /** New chat without previous session.*/
    const val NEW_CHAT = "coach/new_chat"

    /**
     * Route to an existing chat.
     *
     * @param sessionId Session identifier.
     * @return Resolved route.
     */
    fun chatRoute(sessionId: String) = "coach/chat/$sessionId"

    /**
     * Route to start a new chat.
     *
     * @return [NEW_CHAT].
     */
    fun newChatRoute() = NEW_CHAT
}

/** Profile tab subgraph routes.*/
object ProfileRoutes {
    /** Root path of the profile subgraph.*/
    const val GRAPH = "profile_graph"
    /** Profile hub and quick access.*/
    const val HUB = "profile"
    /** Profile editing; query `mode`.*/
    const val EDIT = "profile/edit?mode={mode}"

    /**
     * Path to profile editing in editing mode.
     *
     * @return Route with `mode=edit`.
     */
    fun editRoute() = "profile/edit?mode=edit"

    /** Progress panel from profile.*/
    const val DASHBOARD = "profile/dashboard"
    /** Body weight from profile.*/
    const val BODY_WEIGHT = "profile/body_weight"
    /** Weekly summary from profile.*/
    const val WEEKLY_SUMMARY = "profile/weekly_summary"
    /** Metabolic analysis from profile.*/
    const val METABOLIC = "profile/metabolic"
    /** Export progress data.*/
    const val EXPORT = "profile/export"
    /** Gamification; query `tab` for the initial tab.*/
    const val GAMIFICATION = "profile/gamification?tab={tab}"
    /** Glossary of fitness terms.*/
    const val GLOSSARY = "profile/glossary"

    /**
     * Route to gamification with initial tab.
     *
     * @param tab Tab identifier (e.g. `"ACHIEVEMENTS"`).
     * @return Resolved route.
     */
    fun gamificationRoute(tab: String = "ACHIEVEMENTS") =
        "profile/gamification?tab=$tab"
}

/** Root route of the main graph (app authenticated with bottom nav).*/
object MainRoutes {
    /** Entry to the shell with Home, Training, Nutrition, Coach and Profile tabs.*/
    const val GRAPH = "main"
}

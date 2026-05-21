package com.jlsh.aifit.navigation

/** Rutas del grafo de autenticación y onboarding. */
object AuthRoutes {
    /** Ruta raíz del subgrafo de auth. */
    const val GRAPH = "auth"
    /** Pantalla de inicio de sesión. */
    const val LOGIN = "auth/login"
    /** Pantalla de registro de cuenta. */
    const val REGISTER = "auth/register"
    /** Creación o completado del perfil de usuario. */
    const val CREATE_PROFILE = "auth/create_profile"
    /** Generación de planes iniciales durante el onboarding. */
    const val ONBOARDING_GENERATING = "auth/onboarding_generating"
    /** Aprobación del plan de entrenamiento generado en onboarding. */
    const val ONBOARDING_TRAINING_APPROVAL = "auth/onboarding_training_approval"
    /** Aprobación del plan nutricional generado en onboarding. */
    const val ONBOARDING_NUTRITION_APPROVAL = "auth/onboarding_nutrition_approval"
}

/** Rutas del subgrafo de la pestaña Inicio. */
object HomeRoutes {
    /** Ruta raíz del subgrafo home. */
    const val GRAPH = "home_graph"
    /** Dashboard principal. */
    const val HOME = "home"
    /** Panel de progreso semanal. */
    const val DASHBOARD = "home/dashboard"
    /** Historial y registro de peso corporal. */
    const val BODY_WEIGHT = "home/body_weight"
    /** Resumen semanal detallado. */
    const val WEEKLY_SUMMARY = "home/weekly_summary"
    /** Análisis metabólico. */
    const val METABOLIC_ANALYSIS = "home/metabolic_analysis"
}

/** Rutas del subgrafo de la pestaña Entrenamiento. */
object TrainingRoutes {
    /** Ruta raíz del subgrafo training. */
    const val GRAPH = "training_graph"
    /** Hub de planes de entrenamiento. */
    const val HUB = "training"
    /** Detalle de un plan; requiere argumento `planId`. */
    const val DETAIL = "training/detail/{planId}"
    /** Generación de plan; query `adaptive` y `basePlanId`. */
    const val GENERATE = "training/generate?adaptive={adaptive}&basePlanId={basePlanId}"
    /** Aprobación de plan generado; requiere `planId`. */
    const val APPROVAL = "training/approval/{planId}"
    /** Sesión de entreno en curso; requiere `planId` y `dayId`. */
    const val WORKOUT_SESSION = "training/session/{planId}/{dayId}"
    /** Registro manual de entreno; query opcional `planId`. */
    const val WORKOUT_LOG = "training/workout_log?planId={planId}"
    /** Detalle de un log de entreno; requiere `logId`. */
    const val WORKOUT_DETAIL = "training/workout_detail/{logId}"
    /** Historial de entrenamientos. */
    const val WORKOUT_HISTORY = "training/workout_history"

    /**
     * Ruta concreta al detalle de un plan.
     *
     * @param planId Identificador del plan.
     * @return Ruta resuelta para navegación.
     */
    fun detailRoute(planId: String) = "training/detail/$planId"

    /**
     * Ruta al flujo de generación de plan.
     *
     * @param adaptive Si es un plan adaptativo sobre uno existente.
     * @param basePlanId Plan base para modo adaptativo; vacío si no aplica.
     * @return Ruta con query params codificados.
     */
    fun generateRoute(adaptive: Boolean = false, basePlanId: String? = null) =
        "training/generate?adaptive=$adaptive&basePlanId=${basePlanId ?: ""}"

    /**
     * Ruta a la pantalla de aprobación tras generar un plan.
     *
     * @param planId Identificador del plan generado.
     * @return Ruta resuelta.
     */
    fun approvalRoute(planId: String) = "training/approval/$planId"

    /**
     * Ruta a la sesión de entreno activa.
     *
     * @param planId Identificador del plan.
     * @param dayId Identificador del día de entrenamiento.
     * @return Ruta resuelta.
     */
    fun workoutSessionRoute(planId: String, dayId: String) =
        "training/session/$planId/$dayId"

    /**
     * Ruta al registro manual de entreno.
     *
     * @param planId Plan asociado, o null para registro sin plan.
     * @return Ruta con query `planId`.
     */
    fun workoutLogRoute(planId: String? = null) =
        "training/workout_log?planId=${planId ?: ""}"

    /**
     * Ruta al detalle de un log de entreno completado.
     *
     * @param logId Identificador del log.
     * @return Ruta resuelta.
     */
    fun workoutDetailRoute(logId: String) = "training/workout_detail/$logId"
}

/** Rutas del subgrafo de la pestaña Nutrición. */
object NutritionRoutes {
    /** Ruta raíz del subgrafo nutrition. */
    const val GRAPH = "nutrition_graph"
    /** Hub de nutrición y planes dietéticos. */
    const val HUB = "nutrition"
    /** Registro de comida; query `mode` y `prefilled`. */
    const val TRACK_MEAL = "nutrition/track_meal?mode={mode}&prefilled={prefilled}"
    /** Reconocimiento de alimento por cámara. */
    const val FOOD_VISION = "nutrition/food_vision"
    /** Objetivos nutricionales del usuario. */
    const val TARGET = "nutrition/target"
    /** Detalle de plan dietético; requiere `planId`. */
    const val DIET_DETAIL = "nutrition/diet_detail/{planId}"
    /** Generación de plan dietético; query `adaptive` y `basePlanId`. */
    const val DIET_GENERATE = "nutrition/diet_generate?adaptive={adaptive}&basePlanId={basePlanId}"
    /** Aprobación de plan dietético generado; requiere `planId`. */
    const val DIET_APPROVAL = "nutrition/diet/approval/{planId}"
    /** Detalle de lista de la compra; requiere `listId`. */
    const val SHOPPING_DETAIL = "nutrition/shopping_detail/{listId}"

    /**
     * Ruta al registro de comida.
     *
     * @param mode Modo de entrada (p. ej. manual).
     * @param prefilled Datos precargados desde visión, codificados en la query.
     * @return Ruta resuelta.
     */
    fun trackMealRoute(mode: String = "", prefilled: String = "") =
        "nutrition/track_meal?mode=$mode&prefilled=$prefilled"

    /**
     * Ruta al detalle de un plan dietético.
     *
     * @param planId Identificador del plan.
     * @return Ruta resuelta.
     */
    fun dietDetailRoute(planId: String) = "nutrition/diet_detail/$planId"

    /**
     * Ruta al flujo de generación de plan dietético.
     *
     * @param adaptive Plan adaptativo sobre uno existente.
     * @param basePlanId Plan base; vacío si no aplica.
     * @return Ruta con query params.
     */
    fun dietGenerateRoute(adaptive: Boolean = false, basePlanId: String? = null) =
        "nutrition/diet_generate?adaptive=$adaptive&basePlanId=${basePlanId ?: ""}"

    /**
     * Ruta a la aprobación de un plan dietético generado.
     *
     * @param planId Identificador del plan.
     * @return Ruta resuelta.
     */
    fun dietApprovalRoute(planId: String) = "nutrition/diet/approval/$planId"

    /**
     * Ruta al detalle de una lista de la compra.
     *
     * @param listId Identificador de la lista.
     * @return Ruta resuelta.
     */
    fun shoppingDetailRoute(listId: String) = "nutrition/shopping_detail/$listId"
}

/** Rutas del subgrafo de la pestaña Coach (chat). */
object CoachRoutes {
    /** Ruta raíz del subgrafo coach. */
    const val GRAPH = "coach_graph"
    /** Listado de sesiones de chat. */
    const val SESSION_LIST = "coach"
    /** Chat con sesión existente; requiere `sessionId`. */
    const val CHAT = "coach/chat/{sessionId}"
    /** Nuevo chat sin sesión previa. */
    const val NEW_CHAT = "coach/new_chat"

    /**
     * Ruta a un chat existente.
     *
     * @param sessionId Identificador de la sesión.
     * @return Ruta resuelta.
     */
    fun chatRoute(sessionId: String) = "coach/chat/$sessionId"

    /**
     * Ruta para iniciar un chat nuevo.
     *
     * @return [NEW_CHAT].
     */
    fun newChatRoute() = NEW_CHAT
}

/** Rutas del subgrafo de la pestaña Perfil. */
object ProfileRoutes {
    /** Ruta raíz del subgrafo profile. */
    const val GRAPH = "profile_graph"
    /** Hub del perfil y accesos rápidos. */
    const val HUB = "profile"
    /** Edición de perfil; query `mode`. */
    const val EDIT = "profile/edit?mode={mode}"

    /**
     * Ruta a la edición del perfil en modo edición.
     *
     * @return Ruta con `mode=edit`.
     */
    fun editRoute() = "profile/edit?mode=edit"

    /** Panel de progreso desde perfil. */
    const val DASHBOARD = "profile/dashboard"
    /** Peso corporal desde perfil. */
    const val BODY_WEIGHT = "profile/body_weight"
    /** Resumen semanal desde perfil. */
    const val WEEKLY_SUMMARY = "profile/weekly_summary"
    /** Análisis metabólico desde perfil. */
    const val METABOLIC = "profile/metabolic"
    /** Exportación de datos de progreso. */
    const val EXPORT = "profile/export"
    /** Gamificación; query `tab` para la pestaña inicial. */
    const val GAMIFICATION = "profile/gamification?tab={tab}"
    /** Glosario de términos de fitness. */
    const val GLOSSARY = "profile/glossary"

    /**
     * Ruta a gamificación con pestaña inicial.
     *
     * @param tab Identificador de pestaña (p. ej. `"ACHIEVEMENTS"`).
     * @return Ruta resuelta.
     */
    fun gamificationRoute(tab: String = "ACHIEVEMENTS") =
        "profile/gamification?tab=$tab"
}

/** Ruta raíz del grafo principal (app autenticada con bottom nav). */
object MainRoutes {
    /** Entrada al shell con pestañas Home, Training, Nutrition, Coach y Profile. */
    const val GRAPH = "main"
}

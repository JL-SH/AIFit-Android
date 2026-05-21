package com.jlsh.aifit.feature.training.domain.model

/**
 * Estados posibles de un plan de entrenamiento en el ciclo de vida de la aplicación.
 */
enum class PlanStatus {
    /** Plan actualmente en uso por el usuario. */
    ACTIVE,
    /** Plan finalizado según su duración o uso. */
    COMPLETED,
    /** Plan pausado; no es el plan activo pero sigue disponible. */
    PAUSED,
    /** Plan en borrador, pendiente de aprobación o activación. */
    DRAFT,
    /** Plan eliminado (lógicamente o en servidor). */
    DELETED,
    /** Estado no reconocido o valor desconocido del backend. */
    UNKNOWN;

    companion object {
        /**
         * Convierte una cadena del API o caché local al valor del enum correspondiente.
         *
         * @param value Texto del estado; se compara sin distinguir mayúsculas/minúsculas.
         * @return [UNKNOWN] si [value] es nulo, vacío o no coincide con ningún miembro del enum.
         */
        fun fromString(value: String?): PlanStatus =
            value?.let { runCatching { valueOf(it.uppercase()) }.getOrDefault(UNKNOWN) } ?: UNKNOWN
    }
}

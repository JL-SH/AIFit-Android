package com.jlsh.aifit.feature.training.domain.model

/**
 * Possible states of a training plan in the application life cycle.
 */
enum class PlanStatus {
    /** Plan currently in use by the user.*/
    ACTIVE,
    /** Finalized plan based on duration or use.*/
    COMPLETED,
    /** Paused plan; It is not the active plan but it is still available.*/
    PAUSED,
    /** Draft plan, pending approval or activation.*/
    DRAFT,
    /** Deleted plan (logically or on server).*/
    DELETED,
    /** Unrecognized state or unknown value of the backend.*/
    UNKNOWN;

    companion object {
        /**
         * Converts a string from the API or local cache to the value of the corresponding enum.
         *
         * @param value State text; is compared case-insensitive.
         * @return [UNKNOWN] if [value] is null, empty, or does not match any member of the enum.
         */
        fun fromString(value: String?): PlanStatus =
            value?.let { runCatching { valueOf(it.uppercase()) }.getOrDefault(UNKNOWN) } ?: UNKNOWN
    }
}

package com.jlsh.aifit.core.common

/**
 * Discriminated union that represents the outcome of any asynchronous
 * operation in the application.
 *
 * ViewModels and repositories use [Result] to communicate success, in-progress,
 * and error states to the UI without propagating raw exceptions.
 *
 * @param T The type of the successful payload.
 */
sealed class Result<out T> {

    /**
     * The operation completed successfully.
     *
     * @property data The payload produced by the operation.
     */
    data class Success<T>(val data: T) : Result<T>()

    /**
     * The operation failed.
     *
     * @property exception A typed [AppException] describing the cause of failure.
     */
    data class Error(val exception: AppException) : Result<Nothing>()

    /**
     * The operation is currently in progress. No payload is available yet.
     */
    data object Loading : Result<Nothing>()
}

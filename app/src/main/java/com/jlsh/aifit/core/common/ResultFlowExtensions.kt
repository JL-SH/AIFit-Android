package com.jlsh.aifit.core.common

import kotlinx.coroutines.flow.Flow

/**
 * Collects all emissions and returns the data from the last [Result.Success],
 * or null if the flow never emitted success.
 *
 * Use for cache-then-network flows that emit multiple [Result.Success] values
 * (e.g. Room snapshot followed by API refresh).
 */
suspend fun <T> Flow<Result<T>>.lastSuccessOrNull(): T? {
    var latest: T? = null
    collect { if (it is Result.Success) latest = it.data }
    return latest
}

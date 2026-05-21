package com.jlsh.aifit.core.network

import android.util.Log
import com.jlsh.aifit.core.session.SessionManager
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject
import javax.inject.Singleton

/**
 * OkHttp [Authenticator] that handles HTTP 401 Unauthorized responses.
 *
 * Because the backend has no refresh-token endpoint, any 401 means the JWT
 * has expired irrecoverably. This authenticator delegates to [SessionManager]
 * to clear all local data and emit a logout event with a human-readable
 * message, then returns `null` so OkHttp does **not** retry the request.
 */
@Singleton
class TokenAuthenticator @Inject constructor(
    private val sessionManager: SessionManager,
) : Authenticator {

    /**
     * Invoked automatically by OkHttp on every 401 response.
     *
     * Logs the offending URL, triggers [SessionManager.invalidateSession],
     * and returns `null` to cancel any retry attempt.
     *
     * @param route The route to the origin server, or `null` if unknown.
     * @param response The 401 HTTP response received from the server.
     * @return Always `null`, instructing OkHttp not to retry the request.
     */
    override fun authenticate(route: Route?, response: Response): Request? {
        // The backend has no refresh-token endpoint.
        // A 401 means the JWT has expired irrecoverably.
        // Invalidate the session so the user is redirected to the login screen
        // with a human-readable message instead of a cryptic error in Home.
        Log.w("AIFIT", "TokenAuthenticator — 401 received for ${response.request.url}. Invalidating session.")
        sessionManager.invalidateSession()
        // Return null so OkHttp does NOT retry the request.
        return null
    }
}

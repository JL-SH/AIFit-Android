package com.jlsh.aifit.core.network

import android.util.Log
import com.jlsh.aifit.core.session.SessionManager
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenAuthenticator @Inject constructor(
    private val sessionManager: SessionManager,
) : Authenticator {

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


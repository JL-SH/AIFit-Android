package com.jlsh.aifit.core.network

import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenAuthenticator @Inject constructor() : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        // Do NOT call sessionManager.logout() here.
        // Aggressively clearing the token and local data on any 401 causes:
        //   1. Data loss when the backend restarts (token expires during downtime)
        //   2. Subsequent requests go without a token → 403 "no tienes permisos"
        //   3. Retry becomes impossible without a full re-login
        //
        // Instead, return null so the 401 propagates to the ViewModel layer as
        // UnauthorizedException, which is shown as a retryable/actionable error.
        return null
    }
}


package com.jlsh.aifit.core.network

import com.jlsh.aifit.core.session.SessionManager
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenAuthenticator @Inject constructor(
    private val sessionManager: SessionManager
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        val previousRequest = response.request
        if (previousRequest.header("Authorization") != null) {
            sessionManager.logout()
        }
        return null
    }
}


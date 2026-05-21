package com.jlsh.aifit.core.network

import com.jlsh.aifit.core.datastore.AuthDataStore
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

/**
 * OkHttp [Interceptor] that attaches a Bearer JWT to every outgoing HTTP
 * request when the user is authenticated.
 *
 * When no token is present in [AuthDataStore] the request is forwarded
 * unchanged, allowing public endpoints (login, register) to work without
 * authentication headers.
 */
@Singleton
class AuthInterceptor @Inject constructor(
    private val authDataStore: AuthDataStore
) : Interceptor {

    /**
     * Reads the current JWT from [AuthDataStore] and, when present, injects
     * it as an `Authorization: Bearer <token>` header before delegating to
     * the next interceptor in the chain.
     *
     * @param chain The OkHttp interceptor chain providing the original request.
     * @return The [Response] produced after the (possibly modified) request
     *   is executed by the network.
     */
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = authDataStore.getToken()
        val request = if (token != null) {
            chain.request().newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        } else {
            chain.request()
        }
        return chain.proceed(request)
    }
}

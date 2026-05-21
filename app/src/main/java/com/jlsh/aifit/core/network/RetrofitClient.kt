package com.jlsh.aifit.core.network

import com.jlsh.aifit.BuildConfig
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Singleton factory that assembles the [OkHttpClient] and [Retrofit] instances
 * used throughout the application's network layer.
 *
 * Configuration highlights:
 * - **Serialization**: kotlinx-serialization with lenient JSON parsing
 *   (unknown keys ignored, null values coerced).
 * - **Auth**: [AuthInterceptor] injects the Bearer token; [TokenAuthenticator]
 *   handles 401 responses by invalidating the session.
 * - **Logging**: full BODY-level logging in DEBUG builds, silent in RELEASE.
 * - **Timeouts**: 180 s on connect / read / write to support long-running
 *   AI generation endpoints.
 */
object RetrofitClient {

    /** Lenient [Json] instance used by the Retrofit converter factory. */
    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        isLenient = true
    }

    /**
     * Creates an [OkHttpClient] configured with authentication, logging, and
     * automatic session invalidation on 401 responses.
     *
     * @param authInterceptor Injects the Bearer token into every request.
     * @param tokenAuthenticator Handles 401 by invalidating the local session.
     * @return A fully configured [OkHttpClient].
     */
    fun buildOkHttpClient(
        authInterceptor: AuthInterceptor,
        tokenAuthenticator: TokenAuthenticator
    ): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }

        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .authenticator(tokenAuthenticator)
            .connectTimeout(180, TimeUnit.SECONDS)
            .readTimeout(180, TimeUnit.SECONDS)
            .writeTimeout(180, TimeUnit.SECONDS)
            .build()
    }

    /**
     * Creates a [Retrofit] instance pointed at [BuildConfig.API_BASE_URL]
     * using kotlinx-serialization as the JSON converter factory.
     *
     * @param okHttpClient The pre-configured HTTP client to attach.
     * @return A [Retrofit] ready for use by network service interfaces.
     */
    fun buildRetrofit(okHttpClient: OkHttpClient): Retrofit {
        val contentType = "application/json".toMediaType()
        return Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
    }
}

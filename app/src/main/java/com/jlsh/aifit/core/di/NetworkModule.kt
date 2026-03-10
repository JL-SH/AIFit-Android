package com.jlsh.aifit.core.di

import com.jlsh.aifit.core.network.AuthInterceptor
import com.jlsh.aifit.core.network.RetrofitClient
import com.jlsh.aifit.core.network.TokenAuthenticator
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(
        authInterceptor: AuthInterceptor,
        tokenAuthenticator: TokenAuthenticator
    ): OkHttpClient = RetrofitClient.buildOkHttpClient(authInterceptor, tokenAuthenticator)

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit =
        RetrofitClient.buildRetrofit(okHttpClient)
}


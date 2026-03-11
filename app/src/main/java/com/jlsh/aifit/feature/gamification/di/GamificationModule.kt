package com.jlsh.aifit.feature.gamification.di

import com.jlsh.aifit.feature.gamification.data.api.GamificationApiService
import com.jlsh.aifit.feature.gamification.data.repository.GamificationRepositoryImpl
import com.jlsh.aifit.feature.gamification.domain.repository.GamificationRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class GamificationModule {

    @Binds
    @Singleton
    abstract fun bindGamificationRepository(impl: GamificationRepositoryImpl): GamificationRepository

    companion object {

        @Provides
        @Singleton
        fun provideGamificationApiService(retrofit: Retrofit): GamificationApiService =
            retrofit.create(GamificationApiService::class.java)
    }
}


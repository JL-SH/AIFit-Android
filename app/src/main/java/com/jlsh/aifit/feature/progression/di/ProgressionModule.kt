package com.jlsh.aifit.feature.progression.di

import com.jlsh.aifit.feature.progression.data.api.ProgressionApiService
import com.jlsh.aifit.feature.progression.data.repository.ProgressionRepositoryImpl
import com.jlsh.aifit.feature.progression.domain.repository.ProgressionRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ProgressionModule {

    @Binds
    @Singleton
    abstract fun bindProgressionRepository(impl: ProgressionRepositoryImpl): ProgressionRepository

    companion object {

        @Provides
        @Singleton
        fun provideProgressionApiService(retrofit: Retrofit): ProgressionApiService =
            retrofit.create(ProgressionApiService::class.java)
    }
}


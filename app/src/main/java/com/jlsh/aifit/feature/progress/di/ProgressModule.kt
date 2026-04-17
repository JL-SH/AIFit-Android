package com.jlsh.aifit.feature.progress.di

import com.jlsh.aifit.core.local.AiFitDatabase
import com.jlsh.aifit.feature.progress.data.api.BodyWeightApiService
import com.jlsh.aifit.feature.progress.data.api.ProgressDashboardApiService
import com.jlsh.aifit.feature.progress.data.local.BodyWeightDao
import com.jlsh.aifit.feature.progress.data.repository.BodyWeightRepositoryImpl
import com.jlsh.aifit.feature.progress.data.repository.ProgressDashboardRepositoryImpl
import com.jlsh.aifit.feature.progress.domain.repository.BodyWeightRepository
import com.jlsh.aifit.feature.progress.domain.repository.ProgressDashboardRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ProgressModule {

    @Binds
    @Singleton
    abstract fun bindProgressDashboardRepository(
        impl: ProgressDashboardRepositoryImpl,
    ): ProgressDashboardRepository

    @Binds
    @Singleton
    abstract fun bindBodyWeightRepository(
        impl: BodyWeightRepositoryImpl,
    ): BodyWeightRepository

    companion object {

        @Provides
        @Singleton
        fun provideProgressDashboardApiService(retrofit: Retrofit): ProgressDashboardApiService =
            retrofit.create(ProgressDashboardApiService::class.java)

        @Provides
        @Singleton
        fun provideBodyWeightApiService(retrofit: Retrofit): BodyWeightApiService =
            retrofit.create(BodyWeightApiService::class.java)

        @Provides
        @Singleton
        fun provideBodyWeightDao(database: AiFitDatabase): BodyWeightDao =
            database.bodyWeightDao()
    }
}


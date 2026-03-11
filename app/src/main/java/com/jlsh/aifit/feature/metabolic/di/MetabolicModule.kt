package com.jlsh.aifit.feature.metabolic.di

import com.jlsh.aifit.feature.metabolic.data.api.MetabolicApiService
import com.jlsh.aifit.feature.metabolic.data.repository.MetabolicRepositoryImpl
import com.jlsh.aifit.feature.metabolic.domain.repository.MetabolicRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class MetabolicModule {

    @Binds
    @Singleton
    abstract fun bindMetabolicRepository(impl: MetabolicRepositoryImpl): MetabolicRepository

    companion object {

        @Provides
        @Singleton
        fun provideMetabolicApiService(retrofit: Retrofit): MetabolicApiService =
            retrofit.create(MetabolicApiService::class.java)
    }
}


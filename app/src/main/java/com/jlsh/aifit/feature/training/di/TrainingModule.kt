package com.jlsh.aifit.feature.training.di

import com.jlsh.aifit.core.local.AiFitDatabase
import com.jlsh.aifit.feature.training.data.api.TrainingApiService
import com.jlsh.aifit.feature.training.data.local.TrainingPlanDao
import com.jlsh.aifit.feature.training.data.repository.TrainingRepositoryImpl
import com.jlsh.aifit.feature.training.domain.repository.TrainingRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class TrainingModule {

    @Binds
    @Singleton
    abstract fun bindTrainingRepository(impl: TrainingRepositoryImpl): TrainingRepository

    companion object {

        @Provides
        @Singleton
        fun provideTrainingApiService(retrofit: Retrofit): TrainingApiService =
            retrofit.create(TrainingApiService::class.java)

        @Provides
        fun provideTrainingPlanDao(database: AiFitDatabase): TrainingPlanDao =
            database.trainingPlanDao()
    }
}


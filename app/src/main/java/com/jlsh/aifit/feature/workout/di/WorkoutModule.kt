package com.jlsh.aifit.feature.workout.di

import com.jlsh.aifit.core.local.AiFitDatabase
import com.jlsh.aifit.feature.workout.data.api.WorkoutApiService
import com.jlsh.aifit.feature.workout.data.local.WorkoutLogDao
import com.jlsh.aifit.feature.workout.data.repository.WorkoutRepositoryImpl
import com.jlsh.aifit.feature.workout.domain.repository.WorkoutRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class WorkoutModule {

    @Binds
    @Singleton
    abstract fun bindWorkoutRepository(impl: WorkoutRepositoryImpl): WorkoutRepository

    companion object {

        @Provides
        @Singleton
        fun provideWorkoutApiService(retrofit: Retrofit): WorkoutApiService =
            retrofit.create(WorkoutApiService::class.java)

        @Provides
        fun provideWorkoutLogDao(database: AiFitDatabase): WorkoutLogDao =
            database.workoutLogDao()
    }
}


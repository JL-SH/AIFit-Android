package com.jlsh.aifit.feature.nutrition.di

import com.jlsh.aifit.core.local.AiFitDatabase
import com.jlsh.aifit.feature.nutrition.data.api.NutritionLogApiService
import com.jlsh.aifit.feature.nutrition.data.api.NutritionTargetApiService
import com.jlsh.aifit.feature.nutrition.data.local.NutritionLogDao
import com.jlsh.aifit.feature.nutrition.data.repository.NutritionLogRepositoryImpl
import com.jlsh.aifit.feature.nutrition.data.repository.NutritionTargetRepositoryImpl
import com.jlsh.aifit.feature.nutrition.domain.repository.NutritionLogRepository
import com.jlsh.aifit.feature.nutrition.domain.repository.NutritionTargetRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class NutritionModule {

    @Binds
    @Singleton
    abstract fun bindNutritionLogRepository(impl: NutritionLogRepositoryImpl): NutritionLogRepository

    @Binds
    @Singleton
    abstract fun bindNutritionTargetRepository(impl: NutritionTargetRepositoryImpl): NutritionTargetRepository

    companion object {

        @Provides
        @Singleton
        fun provideNutritionLogApiService(retrofit: Retrofit): NutritionLogApiService =
            retrofit.create(NutritionLogApiService::class.java)

        @Provides
        @Singleton
        fun provideNutritionTargetApiService(retrofit: Retrofit): NutritionTargetApiService =
            retrofit.create(NutritionTargetApiService::class.java)

        @Provides
        fun provideNutritionLogDao(database: AiFitDatabase): NutritionLogDao =
            database.nutritionLogDao()
    }
}


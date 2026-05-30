package com.jlsh.aifit.feature.diet.di

import com.jlsh.aifit.core.local.AiFitDatabase
import com.jlsh.aifit.feature.diet.data.api.DietApiService
import com.jlsh.aifit.feature.diet.data.local.DietPlanDao
import com.jlsh.aifit.feature.diet.data.local.DietPlanDetailCacheDao
import com.jlsh.aifit.feature.diet.data.repository.DietRepositoryImpl
import com.jlsh.aifit.feature.diet.domain.repository.DietRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DietModule {

    @Binds
    @Singleton
    abstract fun bindDietRepository(impl: DietRepositoryImpl): DietRepository

    companion object {

        @Provides
        @Singleton
        fun provideDietApiService(retrofit: Retrofit): DietApiService =
            retrofit.create(DietApiService::class.java)

        @Provides
        @Singleton
        fun provideDietPlanDao(database: AiFitDatabase): DietPlanDao =
            database.dietPlanDao()

        @Provides
        @Singleton
        fun provideDietPlanDetailCacheDao(database: AiFitDatabase): DietPlanDetailCacheDao =
            database.dietPlanDetailCacheDao()
    }
}


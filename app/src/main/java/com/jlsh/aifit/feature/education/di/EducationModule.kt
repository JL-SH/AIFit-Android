package com.jlsh.aifit.feature.education.di

import com.jlsh.aifit.feature.education.data.api.EducationApiService
import com.jlsh.aifit.feature.education.data.repository.EducationRepositoryImpl
import com.jlsh.aifit.feature.education.domain.repository.EducationRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class EducationModule {

    @Binds
    @Singleton
    abstract fun bindEducationRepository(impl: EducationRepositoryImpl): EducationRepository

    companion object {

        @Provides
        @Singleton
        fun provideEducationApiService(retrofit: Retrofit): EducationApiService =
            retrofit.create(EducationApiService::class.java)
    }
}


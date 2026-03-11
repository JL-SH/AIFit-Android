package com.jlsh.aifit.feature.vision.di

import com.jlsh.aifit.feature.vision.data.api.VisionApiService
import com.jlsh.aifit.feature.vision.data.repository.VisionRepositoryImpl
import com.jlsh.aifit.feature.vision.domain.repository.VisionRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class VisionModule {

    @Binds
    @Singleton
    abstract fun bindVisionRepository(impl: VisionRepositoryImpl): VisionRepository

    companion object {

        @Provides
        @Singleton
        fun provideVisionApiService(retrofit: Retrofit): VisionApiService =
            retrofit.create(VisionApiService::class.java)
    }
}


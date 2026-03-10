package com.jlsh.aifit.feature.user.di

import com.jlsh.aifit.core.local.AiFitDatabase
import com.jlsh.aifit.feature.user.data.api.UserApiService
import com.jlsh.aifit.feature.user.data.local.UserProfileDao
import com.jlsh.aifit.feature.user.data.repository.UserRepositoryImpl
import com.jlsh.aifit.feature.user.domain.repository.UserRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class UserModule {

    @Binds
    @Singleton
    abstract fun bindUserRepository(impl: UserRepositoryImpl): UserRepository

    companion object {

        @Provides
        @Singleton
        fun provideUserApiService(retrofit: Retrofit): UserApiService =
            retrofit.create(UserApiService::class.java)

        @Provides
        fun provideUserProfileDao(database: AiFitDatabase): UserProfileDao =
            database.userProfileDao()
    }
}


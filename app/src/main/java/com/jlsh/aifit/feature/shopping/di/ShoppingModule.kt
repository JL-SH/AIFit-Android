package com.jlsh.aifit.feature.shopping.di

import com.jlsh.aifit.core.local.AiFitDatabase
import com.jlsh.aifit.feature.shopping.data.api.ShoppingApiService
import com.jlsh.aifit.feature.shopping.data.local.ShoppingDao
import com.jlsh.aifit.feature.shopping.data.repository.ShoppingRepositoryImpl
import com.jlsh.aifit.feature.shopping.domain.repository.ShoppingRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ShoppingModule {

    @Binds
    @Singleton
    abstract fun bindShoppingRepository(impl: ShoppingRepositoryImpl): ShoppingRepository

    companion object {

        @Provides
        @Singleton
        fun provideShoppingApiService(retrofit: Retrofit): ShoppingApiService =
            retrofit.create(ShoppingApiService::class.java)

        @Provides
        @Singleton
        fun provideShoppingDao(database: AiFitDatabase): ShoppingDao =
            database.shoppingDao()
    }
}


package com.jlsh.aifit.feature.chat.di

import com.jlsh.aifit.core.local.AiFitDatabase
import com.jlsh.aifit.feature.chat.data.api.ChatApiService
import com.jlsh.aifit.feature.chat.data.local.ChatDao
import com.jlsh.aifit.feature.chat.data.repository.ChatRepositoryImpl
import com.jlsh.aifit.feature.chat.domain.repository.ChatRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ChatModule {

    @Binds
    @Singleton
    abstract fun bindChatRepository(impl: ChatRepositoryImpl): ChatRepository

    companion object {

        @Provides
        @Singleton
        fun provideChatApiService(retrofit: Retrofit): ChatApiService =
            retrofit.create(ChatApiService::class.java)

        @Provides
        @Singleton
        fun provideChatDao(database: AiFitDatabase): ChatDao =
            database.chatDao()
    }
}


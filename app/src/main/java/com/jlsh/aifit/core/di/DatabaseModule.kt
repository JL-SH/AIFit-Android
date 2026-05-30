package com.jlsh.aifit.core.di

import android.content.Context
import androidx.room.Room
import com.jlsh.aifit.core.local.AiFitDatabase
import com.jlsh.aifit.core.local.MIGRATION_15_16
import com.jlsh.aifit.core.local.MIGRATION_16_17
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AiFitDatabase =
        Room.databaseBuilder(
            context,
            AiFitDatabase::class.java,
            "aifit_database"
        )
            .addMigrations(MIGRATION_15_16, MIGRATION_16_17)
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()
}


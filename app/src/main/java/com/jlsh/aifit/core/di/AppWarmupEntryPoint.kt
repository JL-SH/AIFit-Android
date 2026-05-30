package com.jlsh.aifit.core.di

import com.jlsh.aifit.core.datastore.AuthDataStore
import com.jlsh.aifit.core.local.AiFitDatabase
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface AppWarmupEntryPoint {
    fun database(): AiFitDatabase
    fun authDataStore(): AuthDataStore
}

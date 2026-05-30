package com.jlsh.aifit.feature.home.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "home_bootstrap_cache")
data class HomeBootstrapCacheEntity(
    @PrimaryKey val userId: String,
    val bootstrapJson: String,
    val cachedAt: Long,
)

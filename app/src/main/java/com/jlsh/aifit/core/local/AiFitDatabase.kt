package com.jlsh.aifit.core.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.jlsh.aifit.feature.user.data.local.UserProfileDao
import com.jlsh.aifit.feature.user.data.local.UserProfileEntity

@Database(
    entities = [UserProfileEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AiFitDatabase : RoomDatabase() {
    abstract fun userProfileDao(): UserProfileDao
}

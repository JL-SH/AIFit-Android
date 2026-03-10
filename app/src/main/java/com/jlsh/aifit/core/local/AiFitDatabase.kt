package com.jlsh.aifit.core.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AiFitDatabase : RoomDatabase()


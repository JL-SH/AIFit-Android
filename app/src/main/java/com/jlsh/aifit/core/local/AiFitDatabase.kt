package com.jlsh.aifit.core.local

import androidx.room.Database
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

/**
 * Placeholder entity to satisfy Room's requirement of at least one entity.
 * Will be removed when real entities are added in later sprints.
 */
@Entity(tableName = "_placeholder")
internal data class PlaceholderEntity(
    @PrimaryKey val id: Int = 0
)

@Database(
    entities = [PlaceholderEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AiFitDatabase : RoomDatabase()


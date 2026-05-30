package com.jlsh.aifit.core.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_15_16 = object : Migration(15, 16) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS home_bootstrap_cache (
                userId TEXT NOT NULL PRIMARY KEY,
                bootstrapJson TEXT NOT NULL,
                cachedAt INTEGER NOT NULL
            )
            """.trimIndent(),
        )
    }
}

val MIGRATION_16_17 = object : Migration(16, 17) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "ALTER TABLE nutrition_logs ADD COLUMN mealsJson TEXT",
        )
    }
}

package com.d2d.racecomputer.core.data.db

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object RaceDatabaseMigrations {
    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE lap_records ADD COLUMN maxSpeedMps REAL NOT NULL DEFAULT 0")
        }
    }
}

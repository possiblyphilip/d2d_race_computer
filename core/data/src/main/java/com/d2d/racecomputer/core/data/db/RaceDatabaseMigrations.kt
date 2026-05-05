package com.d2d.racecomputer.core.data.db

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object RaceDatabaseMigrations {
    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE lap_records ADD COLUMN maxSpeedMps REAL NOT NULL DEFAULT 0")
        }
    }

    val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `pit_stops` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `raceId` INTEGER NOT NULL,
                    `pitNumber` INTEGER NOT NULL,
                    `durationMillis` INTEGER NOT NULL,
                    `completedAtMillis` INTEGER NOT NULL,
                    FOREIGN KEY(`raceId`) REFERENCES `race_sessions`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                )
                """.trimIndent(),
            )
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_pit_stops_raceId` ON `pit_stops` (`raceId`)")
            db.execSQL("ALTER TABLE lap_records ADD COLUMN completedAtMillis INTEGER NOT NULL DEFAULT 0")
        }
    }
}

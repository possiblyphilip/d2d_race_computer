package com.d2d.racecomputer.core.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.d2d.racecomputer.core.data.db.entities.AppSettingsEntity
import com.d2d.racecomputer.core.data.db.entities.GpsPointEntity
import com.d2d.racecomputer.core.data.db.entities.LapRecordEntity
import com.d2d.racecomputer.core.data.db.entities.PitNoteEntity
import com.d2d.racecomputer.core.data.db.entities.RaceSessionEntity
import com.d2d.racecomputer.core.data.db.entities.StopEventEntity

@Database(
    entities = [
        RaceSessionEntity::class,
        LapRecordEntity::class,
        GpsPointEntity::class,
        StopEventEntity::class,
        PitNoteEntity::class,
        AppSettingsEntity::class,
    ],
    version = 2,
)
abstract class RaceDatabase : RoomDatabase() {
    abstract fun raceDao(): RaceDao
}

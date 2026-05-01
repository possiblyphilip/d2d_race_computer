package com.d2d.racecomputer.data

import android.content.Context
import androidx.room.Room
import com.d2d.racecomputer.core.data.db.RaceDatabase
import com.d2d.racecomputer.core.data.db.RaceDatabaseMigrations
import com.d2d.racecomputer.core.data.repo.RaceRepository
import com.d2d.racecomputer.core.domain.model.RaceSettings
import com.d2d.racecomputer.core.location.RaceRuntime

object AppContainer {
    private var initialized = false
    lateinit var raceRepository: RaceRepository
        private set

    fun initialize(context: Context) {
        if (initialized) return
        val db = Room.databaseBuilder(
            context.applicationContext,
            RaceDatabase::class.java,
            "d2d_race.db",
        )
            .addMigrations(RaceDatabaseMigrations.MIGRATION_1_2)
            .build()
        raceRepository = RaceRepository(db.raceDao())
        RaceRuntime.initialize(
            raceRepository,
            RaceSettings(startLatitude = 0.0, startLongitude = 0.0),
        )
        initialized = true
    }
}

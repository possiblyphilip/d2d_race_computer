package com.d2d.racecomputer.core.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.d2d.racecomputer.core.data.db.entities.AppSettingsEntity
import com.d2d.racecomputer.core.data.db.entities.GpsPointEntity
import com.d2d.racecomputer.core.data.db.entities.LapRecordEntity
import com.d2d.racecomputer.core.data.db.entities.PitNoteEntity
import com.d2d.racecomputer.core.data.db.entities.RaceSessionEntity
import com.d2d.racecomputer.core.data.db.entities.StopEventEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RaceDao {
    @Insert
    suspend fun insertRaceSession(race: RaceSessionEntity): Long

    @Query("UPDATE race_sessions SET endedAtMillis = :endedAt WHERE id = :raceId")
    suspend fun endRace(raceId: Long, endedAt: Long)

    @Insert
    suspend fun insertLap(lap: LapRecordEntity)

    @Query("DELETE FROM lap_records WHERE id = (SELECT id FROM lap_records WHERE raceId = :raceId ORDER BY lapNumber DESC LIMIT 1)")
    suspend fun undoLastLap(raceId: Long)

    @Insert
    suspend fun insertGpsPoint(point: GpsPointEntity)

    @Insert
    suspend fun insertGpsBatch(points: List<GpsPointEntity>)

    @Insert
    suspend fun insertStop(stop: StopEventEntity)

    @Insert
    suspend fun insertPitNote(note: PitNoteEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSettings(settings: AppSettingsEntity)

    @Query("SELECT * FROM app_settings WHERE id = 1")
    fun observeSettings(): Flow<AppSettingsEntity?>
}

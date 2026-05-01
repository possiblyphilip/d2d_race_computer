package com.d2d.racecomputer.core.data.repo

import com.d2d.racecomputer.core.data.db.RaceDao
import com.d2d.racecomputer.core.data.db.entities.GpsPointEntity
import com.d2d.racecomputer.core.data.db.entities.LapRecordEntity
import com.d2d.racecomputer.core.data.db.entities.PitNoteEntity
import com.d2d.racecomputer.core.data.db.entities.RaceSessionEntity
import com.d2d.racecomputer.core.domain.model.GpsSample
import com.d2d.racecomputer.core.domain.model.LapRecord
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class RaceRepository(private val raceDao: RaceDao) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val gpsBatch = mutableListOf<GpsPointEntity>()
    private val _activeRaceId = MutableStateFlow<Long?>(null)
    val activeRaceId: StateFlow<Long?> = _activeRaceId.asStateFlow()

    init {
        scope.launch {
            while (isActive) {
                flushGpsBatch()
                delay(2000L)
            }
        }
    }

    suspend fun startRace(startedAtMillis: Long): Long {
        val id = raceDao.insertRaceSession(RaceSessionEntity(startedAtMillis = startedAtMillis))
        _activeRaceId.value = id
        return id
    }

    suspend fun endRace(endedAtMillis: Long) {
        val raceId = _activeRaceId.value ?: return
        flushGpsBatch()
        raceDao.endRace(raceId, endedAtMillis)
        _activeRaceId.value = null
    }

    suspend fun saveLap(lap: LapRecord) {
        val raceId = _activeRaceId.value ?: return
        raceDao.insertLap(
            LapRecordEntity(
                raceId = raceId,
                lapNumber = lap.lapNumber,
                lapTimeMillis = lap.lapTimeMillis,
                lapDistanceMeters = lap.lapDistanceMeters,
                movingTimeMillis = lap.movingTimeMillis,
                stoppedTimeMillis = lap.stoppedTimeMillis,
            ),
        )
    }

    suspend fun undoLap() {
        val raceId = _activeRaceId.value ?: return
        raceDao.undoLastLap(raceId)
    }

    fun queueGps(sample: GpsSample) {
        val raceId = _activeRaceId.value ?: return
        gpsBatch += GpsPointEntity(
            raceId = raceId,
            latitude = sample.latitude,
            longitude = sample.longitude,
            speedMps = sample.speedMps,
            accuracyMeters = sample.accuracyMeters,
            timestampMillis = sample.timestampMillis,
        )
    }

    suspend fun savePitNote(note: String, timestampMillis: Long) {
        val raceId = _activeRaceId.value ?: return
        raceDao.insertPitNote(PitNoteEntity(raceId = raceId, timestampMillis = timestampMillis, note = note))
    }

    private suspend fun flushGpsBatch() {
        if (gpsBatch.isEmpty()) return
        val toWrite = gpsBatch.toList()
        gpsBatch.clear()
        raceDao.insertGpsBatch(toWrite)
    }
}

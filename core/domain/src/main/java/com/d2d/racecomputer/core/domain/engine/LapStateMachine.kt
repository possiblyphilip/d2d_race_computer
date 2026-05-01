package com.d2d.racecomputer.core.domain.engine

import com.d2d.racecomputer.core.domain.geo.GeoMath
import com.d2d.racecomputer.core.domain.model.GpsSample
import com.d2d.racecomputer.core.domain.model.LapRecord
import com.d2d.racecomputer.core.domain.model.RaceSettings
import com.d2d.racecomputer.core.domain.model.RaceSnapshot

class LapStateMachine(private val settings: RaceSettings) {
    private var startedAtMillis: Long? = null
    private var previousSample: GpsSample? = null
    private var insideZone = true
    private var leftZoneSinceLastLap = false
    private var lastLapCrossingMillis: Long? = null
    private var lapStartMillis: Long? = null
    private var totalDistanceMeters = 0.0
    private var currentLapDistanceMeters = 0.0
    private var movingCurrentLapMillis = 0L
    private var stoppedCurrentLapMillis = 0L
    private var laps = mutableListOf<LapRecord>()

    fun start(startMillis: Long) {
        startedAtMillis = startMillis
        lapStartMillis = startMillis
    }

    fun onSample(sample: GpsSample, isStopped: Boolean): RaceSnapshot {
        val start = startedAtMillis ?: sample.timestampMillis.also { start(it) }
        if (sample.accuracyMeters > settings.maxGpsAccuracyMeters) {
            return snapshot(sample.timestampMillis - start)
        }
        val prev = previousSample
        if (prev != null && sample.timestampMillis > prev.timestampMillis) {
            val segment = GeoMath.distanceMeters(prev.latitude, prev.longitude, sample.latitude, sample.longitude)
            if (segment <= 200.0) {
                totalDistanceMeters += segment
                currentLapDistanceMeters += segment
            }
            val dt = sample.timestampMillis - prev.timestampMillis
            if (isStopped) stoppedCurrentLapMillis += dt else movingCurrentLapMillis += dt
        }

        val distanceToStart = GeoMath.distanceMeters(
            sample.latitude,
            sample.longitude,
            settings.startLatitude,
            settings.startLongitude,
        )

        val nowInside = when {
            insideZone && distanceToStart <= settings.lapExitRadiusMeters -> true
            !insideZone && distanceToStart <= settings.lapEnterRadiusMeters -> true
            else -> false
        }

        if (insideZone && !nowInside) {
            leftZoneSinceLastLap = true
        }

        if (!insideZone && nowInside) {
            maybeCountLap(sample.timestampMillis)
        }

        insideZone = nowInside
        previousSample = sample
        return snapshot(sample.timestampMillis - start)
    }

    private fun maybeCountLap(nowMillis: Long) {
        val lapStart = lapStartMillis ?: return
        val lapTime = nowMillis - lapStart
        if (!leftZoneSinceLastLap) return
        if (lapTime < settings.minLapTimeMillis) return
        if (currentLapDistanceMeters < settings.minLapDistanceMeters) return
        val lastCrossing = lastLapCrossingMillis
        if (lastCrossing != null && nowMillis - lastCrossing < 20_000L) return

        laps += LapRecord(
            lapNumber = laps.size + 1,
            lapTimeMillis = lapTime,
            lapDistanceMeters = currentLapDistanceMeters,
            movingTimeMillis = movingCurrentLapMillis,
            stoppedTimeMillis = stoppedCurrentLapMillis,
        )
        lapStartMillis = nowMillis
        lastLapCrossingMillis = nowMillis
        currentLapDistanceMeters = 0.0
        movingCurrentLapMillis = 0L
        stoppedCurrentLapMillis = 0L
        leftZoneSinceLastLap = false
    }

    fun addManualLap(nowMillis: Long) {
        val lapStart = lapStartMillis ?: nowMillis
        val lapTime = nowMillis - lapStart
        laps += LapRecord(
            lapNumber = laps.size + 1,
            lapTimeMillis = lapTime,
            lapDistanceMeters = currentLapDistanceMeters,
            movingTimeMillis = movingCurrentLapMillis,
            stoppedTimeMillis = stoppedCurrentLapMillis,
        )
        lapStartMillis = nowMillis
        currentLapDistanceMeters = 0.0
        movingCurrentLapMillis = 0L
        stoppedCurrentLapMillis = 0L
        leftZoneSinceLastLap = false
        lastLapCrossingMillis = nowMillis
    }

    fun undoLastLap() {
        if (laps.isNotEmpty()) {
            laps.removeLast()
        }
    }

    private fun snapshot(elapsed: Long): RaceSnapshot {
        val last = laps.lastOrNull()
        val best = laps.minByOrNull { it.lapTimeMillis }
        val avg = if (laps.isEmpty()) null else laps.map { it.lapTimeMillis }.average().toLong()
        return RaceSnapshot(
            elapsedMillis = elapsed,
            laps = laps.toList(),
            lapNumber = laps.size + 1,
            totalDistanceMeters = totalDistanceMeters,
            currentLapDistanceMeters = currentLapDistanceMeters,
            currentLapTimeMillis = elapsed - ((lapStartMillis ?: 0L) - (startedAtMillis ?: 0L)),
            movingTimeCurrentLapMillis = movingCurrentLapMillis,
            stoppedTimeCurrentLapMillis = stoppedCurrentLapMillis,
            lastLapTimeMillis = last?.lapTimeMillis,
            bestLapTimeMillis = best?.lapTimeMillis,
            averageLapTimeMillis = avg,
        )
    }
}

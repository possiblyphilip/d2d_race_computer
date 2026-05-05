package com.d2d.racecomputer.core.domain.engine

import com.d2d.racecomputer.core.domain.geo.GeoMath
import com.d2d.racecomputer.core.domain.model.GpsSample
import com.d2d.racecomputer.core.domain.model.LapRecord
import com.d2d.racecomputer.core.domain.model.PitStopRecord
import com.d2d.racecomputer.core.domain.model.RaceSettings
import com.d2d.racecomputer.core.domain.model.RaceSnapshot
import kotlin.math.abs

class LapStateMachine(private val settings: RaceSettings) {
    private var startedAtMillis: Long? = null
    private var previousSample: GpsSample? = null
    private var insideZone = true
    private var leftZoneSinceLastLap = false
    private var lastLapCrossingMillis: Long? = null
    private var lapStartMillis: Long? = null
    private var lastZoneExitMillis: Long? = null
    private var totalDistanceMeters = 0.0
    private var currentLapDistanceMeters = 0.0
    private var movingCurrentLapMillis = 0L
    private var stoppedCurrentLapMillis = 0L
    private var maxSpeedCurrentLapMps = 0.0
    private var laps = mutableListOf<LapRecord>()
    private var pitStops = mutableListOf<PitStopRecord>()

    fun start(startMillis: Long) {
        startedAtMillis = startMillis
        lapStartMillis = startMillis
    }

    fun onSample(sample: GpsSample, isStopped: Boolean): RaceSnapshot {
        val start = startedAtMillis ?: sample.timestampMillis.also { start(it) }
        if (sample.accuracyMeters > settings.maxGpsAccuracyMeters) {
            return snapshot(sample.timestampMillis - start)
        }
        val spd = abs(sample.speedMps).coerceAtLeast(0.0)
        if (spd > maxSpeedCurrentLapMps) maxSpeedCurrentLapMps = spd

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
            lastZoneExitMillis = sample.timestampMillis
        }

        if (!insideZone && nowInside) {
            when {
                canCountLap(sample.timestampMillis) -> countLap(sample.timestampMillis)
                shouldRecordPit(sample.timestampMillis) -> recordPit(sample.timestampMillis)
            }
        }

        insideZone = nowInside
        previousSample = sample
        return snapshot(sample.timestampMillis - start)
    }

    private fun crossingDebounceOk(nowMillis: Long): Boolean {
        val lastCrossing = lastLapCrossingMillis
        return lastCrossing == null || nowMillis - lastCrossing >= 20_000L
    }

    private fun canCountLap(nowMillis: Long): Boolean {
        val lapStart = lapStartMillis ?: return false
        val lapTime = nowMillis - lapStart
        if (!leftZoneSinceLastLap) return false
        if (lapTime < settings.minLapTimeMillis) return false
        if (currentLapDistanceMeters < settings.minLapDistanceMeters) return false
        if (!crossingDebounceOk(nowMillis)) return false
        return true
    }

    /**
     * Re-entered the zone after leaving, but did not accumulate a full lap distance — treat as a pit
     * (time is from leaving the zone until this re-entry).
     */
    private fun shouldRecordPit(nowMillis: Long): Boolean {
        if (!leftZoneSinceLastLap) return false
        if (lastZoneExitMillis == null) return false
        if (currentLapDistanceMeters >= settings.minLapDistanceMeters) return false
        if (!crossingDebounceOk(nowMillis)) return false
        return true
    }

    private fun countLap(nowMillis: Long) {
        val lapStart = lapStartMillis ?: return
        val lapTime = nowMillis - lapStart
        laps += LapRecord(
            lapNumber = laps.size + 1,
            lapTimeMillis = lapTime,
            lapDistanceMeters = currentLapDistanceMeters,
            movingTimeMillis = movingCurrentLapMillis,
            stoppedTimeMillis = stoppedCurrentLapMillis,
            maxSpeedMps = maxSpeedCurrentLapMps,
            completedAtMillis = nowMillis,
        )
        resetAfterZoneCrossing(nowMillis)
    }

    private fun recordPit(nowMillis: Long) {
        val exit = lastZoneExitMillis ?: return
        val duration = (nowMillis - exit).coerceAtLeast(0L)
        if (duration <= 0L) return
        pitStops += PitStopRecord(
            pitNumber = pitStops.size + 1,
            durationMillis = duration,
            completedAtMillis = nowMillis,
        )
        resetAfterZoneCrossing(nowMillis)
    }

    private fun resetAfterZoneCrossing(nowMillis: Long) {
        lapStartMillis = nowMillis
        lastLapCrossingMillis = nowMillis
        currentLapDistanceMeters = 0.0
        movingCurrentLapMillis = 0L
        stoppedCurrentLapMillis = 0L
        maxSpeedCurrentLapMps = 0.0
        leftZoneSinceLastLap = false
        lastZoneExitMillis = null
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
            maxSpeedMps = maxSpeedCurrentLapMps,
            completedAtMillis = nowMillis,
        )
        lapStartMillis = nowMillis
        currentLapDistanceMeters = 0.0
        movingCurrentLapMillis = 0L
        stoppedCurrentLapMillis = 0L
        maxSpeedCurrentLapMps = 0.0
        leftZoneSinceLastLap = false
        lastLapCrossingMillis = nowMillis
        lastZoneExitMillis = null
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
            pitStops = pitStops.toList(),
            lapNumber = laps.size + 1,
            totalDistanceMeters = totalDistanceMeters,
            currentLapDistanceMeters = currentLapDistanceMeters,
            currentLapTimeMillis = elapsed - ((lapStartMillis ?: 0L) - (startedAtMillis ?: 0L)),
            movingTimeCurrentLapMillis = movingCurrentLapMillis,
            stoppedTimeCurrentLapMillis = stoppedCurrentLapMillis,
            lastLapTimeMillis = last?.lapTimeMillis,
            bestLapTimeMillis = best?.lapTimeMillis,
            averageLapTimeMillis = avg,
            isInStartFinishZone = insideZone,
        )
    }
}

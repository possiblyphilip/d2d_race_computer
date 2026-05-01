package com.d2d.racecomputer.core.domain.engine

import com.d2d.racecomputer.core.domain.model.GpsSample
import com.d2d.racecomputer.core.domain.model.RaceSettings
import com.d2d.racecomputer.core.domain.model.StopStats

class StopDetector(private val settings: RaceSettings) {
    private var stopStartMillis: Long? = null
    private var isInStop = false
    private var totalStopped = 0L
    private var stopCount = 0
    private var longestStop = 0L

    fun onSample(sample: GpsSample): Boolean {
        if (sample.speedMps <= settings.stopSpeedThresholdMps) {
            if (stopStartMillis == null) {
                stopStartMillis = sample.timestampMillis
            }
            val candidate = sample.timestampMillis - (stopStartMillis ?: sample.timestampMillis)
            if (!isInStop && candidate >= settings.stopSustainMillis) {
                isInStop = true
                stopCount++
            }
        } else {
            if (isInStop && stopStartMillis != null) {
                val duration = sample.timestampMillis - (stopStartMillis ?: sample.timestampMillis)
                totalStopped += duration
                longestStop = maxOf(longestStop, duration)
            }
            stopStartMillis = null
            isInStop = false
        }
        return isInStop
    }

    fun finalizeStop(nowMillis: Long) {
        if (isInStop && stopStartMillis != null) {
            val duration = nowMillis - (stopStartMillis ?: nowMillis)
            totalStopped += duration
            longestStop = maxOf(longestStop, duration)
            isInStop = false
            stopStartMillis = null
        }
    }

    fun snapshot(): StopStats = StopStats(
        totalStoppedMillis = totalStopped,
        stopCount = stopCount,
        longestStopMillis = longestStop,
    )

    fun isCurrentlyStopped(): Boolean = isInStop

    fun currentStopDurationMillis(nowMillis: Long): Long {
        if (!isInStop) return 0L
        val activeStopStart = stopStartMillis ?: return 0L
        return (nowMillis - activeStopStart).coerceAtLeast(0L)
    }
}

package com.d2d.racecomputer.core.domain.model

data class RaceSettings(
    val startLatitude: Double,
    val startLongitude: Double,
    val lapEnterRadiusMeters: Float = 35f,
    val lapExitRadiusMeters: Float = 60f,
    val minLapTimeMillis: Long = 10 * 60 * 1000L,
    val minLapDistanceMeters: Double = 2000.0,
    val stopSpeedThresholdMps: Double = 0.9,
    val stopSustainMillis: Long = 7000L,
    val maxGpsAccuracyMeters: Float = 35f,
    val gpsUpdateMs: Long = 1000L,
    val raceDurationMillis: Long = 12 * 60 * 60 * 1000L,
)

data class GpsSample(
    val latitude: Double,
    val longitude: Double,
    val speedMps: Double,
    val accuracyMeters: Float,
    val timestampMillis: Long,
)

data class LapRecord(
    val lapNumber: Int,
    val lapTimeMillis: Long,
    val lapDistanceMeters: Double,
    val movingTimeMillis: Long,
    val stoppedTimeMillis: Long,
    /** Peak reported GPS speed during the lap (m/s); coerced ≥ 0. */
    val maxSpeedMps: Double = 0.0,
) {
    /** Average speed over wall-clock lap time (km/h); null if lap time is zero. */
    fun averageSpeedKmh(): Double? {
        if (lapTimeMillis <= 0L) return null
        val mps = lapDistanceMeters / (lapTimeMillis / 1000.0)
        return mps * 3.6
    }

    fun maxSpeedKmh(): Double = maxOf(0.0, maxSpeedMps) * 3.6
}

data class StopStats(
    val totalStoppedMillis: Long = 0L,
    val stopCount: Int = 0,
    val longestStopMillis: Long = 0L,
)

data class RaceSnapshot(
    val elapsedMillis: Long = 0L,
    val laps: List<LapRecord> = emptyList(),
    val lapNumber: Int = 0,
    val totalDistanceMeters: Double = 0.0,
    val currentLapDistanceMeters: Double = 0.0,
    val currentLapTimeMillis: Long = 0L,
    val movingTimeCurrentLapMillis: Long = 0L,
    val stoppedTimeCurrentLapMillis: Long = 0L,
    val stopStats: StopStats = StopStats(),
    val lastLapTimeMillis: Long? = null,
    val bestLapTimeMillis: Long? = null,
    val averageLapTimeMillis: Long? = null,
    val isCurrentlyStopped: Boolean = false,
    val currentStopDurationMillis: Long = 0L,
    /** Latest GPS-derived speed from the most recent fix (m/s); UI typically shows km/h. */
    val currentSpeedMps: Double = 0.0,
)

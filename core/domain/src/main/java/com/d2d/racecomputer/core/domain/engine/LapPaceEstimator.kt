package com.d2d.racecomputer.core.domain.engine

import com.d2d.racecomputer.core.domain.model.LapRecord

/**
 * Lap pace for projections: prefers [LapRecord.movingTimeMillis] so stationary time on a counted lap
 * (aid stops, etc.) does not inflate the pace. If moving time is missing/zero, falls back to wall
 * [LapRecord.lapTimeMillis]. Short-lap pit episodes are not part of completed laps.
 */
fun LapRecord.paceMillisIgnoringOnLapStops(): Long {
    val m = movingTimeMillis
    return if (m > 0L) m else lapTimeMillis
}

/**
 * Average pace (ms) over the last [recentLapCount] completed laps, or fewer when not enough history.
 */
fun averageRecentPaceMillisDouble(laps: List<LapRecord>, recentLapCount: Int = 3): Double? {
    if (laps.isEmpty()) return null
    val n = minOf(recentLapCount.coerceAtLeast(1), laps.size)
    val recent = laps.takeLast(n)
    val paces = recent.map { it.paceMillisIgnoringOnLapStops() }.filter { it > 0L }
    if (paces.isEmpty()) return null
    return paces.map { it.toDouble() }.average()
}

/**
 * Equivalent **full laps at recent pace** still achievable after the current lap, in a way that
 * stays **steady while you ride** (same wall clock burns [remainingMillis] and advances
 * [currentLapElapsedMillis] together).
 *
 * Uses `(remaining − pace + currentLapElapsed) / pace`, i.e. time left **after** finishing this lap
 * at nominal pace [pace], divided by pace. That is **not** `(remaining − currentLapElapsed) / pace`,
 * which would fall twice as fast as the clock.
 *
 * @param currentLapElapsedMillis wall-clock time so far on the lap in progress (see [RaceSnapshot.currentLapTimeMillis]).
 */
fun estimateLapsRemainingAtRecentPace(
    remainingMillis: Long,
    laps: List<LapRecord>,
    currentLapElapsedMillis: Long,
    recentLapCount: Int = 3,
): Double? {
    val pace = averageRecentPaceMillisDouble(laps, recentLapCount) ?: return null
    if (pace <= 0.0) return null
    val r = remainingMillis.coerceAtLeast(0L).toDouble()
    val t = currentLapElapsedMillis.coerceAtLeast(0L).toDouble()
    val afterThisLapAtPace = r - pace + t
    return (if (afterThisLapAtPace > 0.0) afterThisLapAtPace else 0.0) / pace
}

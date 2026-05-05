package com.d2d.racecomputer.core.domain.engine

import com.d2d.racecomputer.core.domain.model.LapRecord
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LapPaceEstimatorTest {

    private fun lap(
        n: Int,
        wall: Long,
        moving: Long,
        stopped: Long = wall - moving,
    ) = LapRecord(
        lapNumber = n,
        lapTimeMillis = wall,
        lapDistanceMeters = 1000.0,
        movingTimeMillis = moving,
        stoppedTimeMillis = stopped,
        maxSpeedMps = 0.0,
        completedAtMillis = 0L,
    )

    @Test
    fun noCompletedLaps_returnsNull() {
        assertNull(estimateLapsRemainingAtRecentPace(3_600_000L, emptyList(), currentLapElapsedMillis = 0L))
    }

    @Test
    fun oneLap_atLapStart_countsTimeAfterThisLap() {
        val laps = listOf(lap(1, wall = 2_000L, moving = 1_000L))
        val pace = 1_000.0
        // 3 s left, 1 s pace: naive 3; at lap start (t=0) expect (3000-1000+0)/1000 = 2
        assertEquals(2.0, estimateLapsRemainingAtRecentPace(3_000L, laps, currentLapElapsedMillis = 0L)!!, 1e-9)
        // Same instant as "just finished" nominal lap: (3000-1000+1000)/1000 = 3
        assertEquals(3.0, estimateLapsRemainingAtRecentPace(3_000L, laps, currentLapElapsedMillis = pace.toLong())!!, 1e-9)
    }

    @Test
    fun valueStableAsClockAndLapTimerAdvanceTogether() {
        val laps = listOf(lap(1, wall = 2_000L, moving = 1_000L))
        val est0 = estimateLapsRemainingAtRecentPace(3_000L, laps, currentLapElapsedMillis = 0L)!!
        val estMid = estimateLapsRemainingAtRecentPace(2_500L, laps, currentLapElapsedMillis = 500L)!!
        assertEquals(est0, estMid, 1e-9)
    }

    @Test
    fun movingZero_fallsBackToWallTime() {
        val laps = listOf(lap(1, wall = 1_500L, moving = 0L, stopped = 0L))
        assertEquals(1.0, estimateLapsRemainingAtRecentPace(3_000L, laps, currentLapElapsedMillis = 0L)!!, 1e-9)
        assertEquals(2.0, estimateLapsRemainingAtRecentPace(3_000L, laps, currentLapElapsedMillis = 1_500L)!!, 1e-9)
    }

    @Test
    fun usesLastThreeLaps_notOlderOnes() {
        val laps = listOf(
            lap(1, wall = 100L, moving = 100L),
            lap(2, wall = 100L, moving = 100L),
            lap(3, wall = 60 * 60_000L, moving = 60 * 60_000L),
            lap(4, wall = 60 * 60_000L, moving = 60 * 60_000L),
            lap(5, wall = 60 * 60_000L, moving = 60 * 60_000L),
        )
        val paceMs = 60 * 60_000L
        val remaining = 3 * paceMs
        assertEquals(2.0, estimateLapsRemainingAtRecentPace(remaining, laps, currentLapElapsedMillis = 0L)!!, 0.05)
        assertEquals(3.0, estimateLapsRemainingAtRecentPace(remaining, laps, currentLapElapsedMillis = paceMs)!!, 0.05)
    }

    @Test
    fun twoLaps_averagesBoth() {
        val laps = listOf(
            lap(1, wall = 1_000L, moving = 600L),
            lap(2, wall = 1_000L, moving = 400L),
        )
        val avgPace = 500.0
        val remaining = (avgPace * 10).toLong()
        assertEquals(9.0, estimateLapsRemainingAtRecentPace(remaining, laps, currentLapElapsedMillis = 0L)!!, 1e-9)
        assertEquals(10.0, estimateLapsRemainingAtRecentPace(remaining, laps, currentLapElapsedMillis = 500L)!!, 1e-9)
    }

    @Test
    fun clampsToZeroWhenLessThanOneLapLeftAtPace() {
        val laps = listOf(lap(1, wall = 1_000L, moving = 1_000L))
        assertEquals(0.0, estimateLapsRemainingAtRecentPace(500L, laps, currentLapElapsedMillis = 0L)!!, 1e-9)
    }
}

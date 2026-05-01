package com.d2d.racecomputer.core.domain

import com.d2d.racecomputer.core.domain.engine.LapStateMachine
import com.d2d.racecomputer.core.domain.model.GpsSample
import com.d2d.racecomputer.core.domain.model.RaceSettings
import org.junit.Assert.assertEquals
import org.junit.Test

class LapStateMachineTest {
    @Test
    fun countsLapAfterLeavingAndReEnteringZoneWithMinGuards() {
        val settings = RaceSettings(
            startLatitude = 0.0,
            startLongitude = 0.0,
            minLapTimeMillis = 60_000L,
            minLapDistanceMeters = 500.0,
            lapEnterRadiusMeters = 30f,
            lapExitRadiusMeters = 40f,
        )
        val machine = LapStateMachine(settings)
        machine.start(0L)
        machine.onSample(GpsSample(0.0, 0.0, 0.0, 5f, 0L), isStopped = false)
        machine.onSample(GpsSample(0.01, 0.01, 12.0, 5f, 70_000L), isStopped = false)
        val result = machine.onSample(GpsSample(0.0, 0.0, 3.0, 5f, 75_000L), isStopped = false)
        assertEquals(1, result.laps.size)
    }

    @Test
    fun ignoresLapWhenMinTimeNotMet() {
        val settings = RaceSettings(
            startLatitude = 0.0,
            startLongitude = 0.0,
            minLapTimeMillis = 120_000L,
            minLapDistanceMeters = 500.0,
        )
        val machine = LapStateMachine(settings)
        machine.start(0L)
        machine.onSample(GpsSample(0.0, 0.0, 0.0, 5f, 0L), isStopped = false)
        machine.onSample(GpsSample(0.01, 0.01, 12.0, 5f, 30_000L), isStopped = false)
        val result = machine.onSample(GpsSample(0.0, 0.0, 3.0, 5f, 40_000L), isStopped = false)
        assertEquals(0, result.laps.size)
    }
}

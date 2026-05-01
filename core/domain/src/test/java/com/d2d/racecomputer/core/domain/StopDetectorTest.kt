package com.d2d.racecomputer.core.domain

import com.d2d.racecomputer.core.domain.engine.StopDetector
import com.d2d.racecomputer.core.domain.model.GpsSample
import com.d2d.racecomputer.core.domain.model.RaceSettings
import org.junit.Assert.assertEquals
import org.junit.Test

class StopDetectorTest {
    @Test
    fun registersStopAfterSustainWindow() {
        val detector = StopDetector(
            RaceSettings(
                startLatitude = 0.0,
                startLongitude = 0.0,
                stopSpeedThresholdMps = 1.0,
                stopSustainMillis = 5000L,
            ),
        )
        detector.onSample(GpsSample(0.0, 0.0, 0.8, 4f, 0L))
        val stopped = detector.onSample(GpsSample(0.0, 0.0, 0.7, 4f, 6000L))
        assertEquals(true, stopped)
    }

    @Test
    fun tracksLongestStop() {
        val detector = StopDetector(RaceSettings(startLatitude = 0.0, startLongitude = 0.0))
        detector.onSample(GpsSample(0.0, 0.0, 0.0, 4f, 0L))
        detector.onSample(GpsSample(0.0, 0.0, 0.0, 4f, 8000L))
        detector.onSample(GpsSample(0.0, 0.0, 5.0, 4f, 10_000L))
        assertEquals(1, detector.snapshot().stopCount)
        assertEquals(true, detector.snapshot().longestStopMillis >= 8000L)
    }
}

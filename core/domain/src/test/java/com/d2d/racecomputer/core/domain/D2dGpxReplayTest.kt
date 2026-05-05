package com.d2d.racecomputer.core.domain

import com.d2d.racecomputer.core.domain.engine.LapStateMachine
import com.d2d.racecomputer.core.domain.engine.StopDetector
import com.d2d.racecomputer.core.domain.gpx.GpxTrackParser
import com.d2d.racecomputer.core.domain.model.RaceSettings
import com.d2d.racecomputer.core.domain.model.RaceSnapshot
import org.junit.Assert.assertTrue
import org.junit.Test

/** Matches `Formatters.kt` / `RaceScreen` spacing so GPX replay output mirrors the race log. */
private fun Long.toClock(): String {
    val totalSeconds = this / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return "%02d:%02d:%02d".format(hours, minutes, seconds)
}

private fun Double.metersToKmRaceLog(): String = "%.2f km".format(this / 1000.0)

/** Same chronological merge and strings as UI `RaceLogUi` / `RaceScreen`. */
private fun RaceSnapshot.linesMatchingRaceScreenLog(): List<String> {
    val tagged = mutableListOf<Pair<Long, String>>()
    laps.forEach { lap ->
        val sortKey = if (lap.completedAtMillis > 0L) {
            lap.completedAtMillis
        } else {
            lap.lapNumber.toLong() * 1_000_000_000L
        }
        tagged += sortKey to
            "Lap ${lap.lapNumber}  |  ${lap.lapTimeMillis.toClock()}  |  ${lap.lapDistanceMeters.metersToKmRaceLog()}  |  Stopped ${lap.stoppedTimeMillis.toClock()}"
    }
    pitStops.forEach { pit ->
        tagged += pit.completedAtMillis to
            "Pit ${pit.pitNumber}  |  outside start zone ${pit.durationMillis.toClock()}"
    }
    tagged.sortBy { it.first }
    return tagged.map { it.second }
}

/**
 * Replays [d2d.gpx] through the lap + stop engines using the user-provided start line and 30 m zone.
 * Zone radii match app setup: enter = 30 m, exit = 30 × 1.7 (capped same as [SetupScreen]).
 */
class D2dGpxReplayTest {

    @Test
    fun d2dGpxReplay_countsLapsAndOptionallyPits() {
        val samples = GpxTrackParser.parsePoints(
            requireNotNull(javaClass.getResourceAsStream("/d2d.gpx")) { "Place d2d.gpx in src/test/resources/" },
        )

        val enterM = 30f
        val settings = RaceSettings(
            startLatitude = 45.594639,
            startLongitude = -123.337494,
            lapEnterRadiusMeters = enterM,
            lapExitRadiusMeters = (enterM * 1.7f).coerceAtLeast(25f),
            minLapTimeMillis = 10 * 60 * 1000L,
            minLapDistanceMeters = 2000.0,
            maxGpsAccuracyMeters = 50f,
        )

        val machine = LapStateMachine(settings)
        val stopDetector = StopDetector(settings)
        machine.start(samples.first().timestampMillis)

        var snapshot = machine.onSample(samples.first(), stopDetector.onSample(samples.first()))
        for (i in 1 until samples.size) {
            val s = samples[i]
            val stopped = stopDetector.onSample(s)
            snapshot = machine.onSample(s, stopped)
        }

        val laps = snapshot.laps.size
        val pits = snapshot.pitStops.size

        println("Race log (${laps} lap(s), ${pits} pit(s)) — matches Race screen:")
        snapshot.linesMatchingRaceScreenLog().forEach { println(it) }
        println(
            "Lap ${snapshot.lapNumber} (in progress)  |  ${snapshot.currentLapTimeMillis.toClock()}  |  " +
                "${snapshot.currentLapDistanceMeters.metersToKmRaceLog()}  |  Stopped ${snapshot.stoppedTimeCurrentLapMillis.toClock()}",
        )

        assertTrue(
            "GPX replay: expected at least one completed lap (got $laps laps, $pits pits). " +
                "Check start line coordinates, zone size, or min lap rules vs this trace.",
            laps >= 1,
        )
    }
}

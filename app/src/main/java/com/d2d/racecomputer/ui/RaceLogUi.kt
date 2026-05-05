package com.d2d.racecomputer.ui

import com.d2d.racecomputer.core.domain.model.LapRecord
import com.d2d.racecomputer.core.domain.model.PitStopRecord
import com.d2d.racecomputer.core.domain.model.RaceSnapshot

/** Rows for lap log UI: laps and pit stops in chronological order. */
sealed class RaceLogListRow {
    data class LapRow(val lap: LapRecord) : RaceLogListRow()
    data class PitRow(val pit: PitStopRecord) : RaceLogListRow()
}

fun RaceSnapshot.chronologicalRaceLog(): List<RaceLogListRow> {
    val tagged = mutableListOf<Pair<Long, RaceLogListRow>>()
    laps.forEach { lap ->
        val sortKey = if (lap.completedAtMillis > 0L) {
            lap.completedAtMillis
        } else {
            lap.lapNumber.toLong() * 1_000_000_000L
        }
        tagged += sortKey to RaceLogListRow.LapRow(lap)
    }
    pitStops.forEach { pit ->
        tagged += pit.completedAtMillis to RaceLogListRow.PitRow(pit)
    }
    tagged.sortBy { it.first }
    return tagged.map { it.second }
}

package com.d2d.racecomputer.ui.summary

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.d2d.racecomputer.core.domain.model.LapRecord
import com.d2d.racecomputer.ui.RaceViewModel
import com.d2d.racecomputer.ui.formatSpeedKmhOneDecimal
import com.d2d.racecomputer.ui.metersToKm
import com.d2d.racecomputer.ui.metersToKmPlain
import com.d2d.racecomputer.ui.toClock

@Composable
fun SummaryScreen(
    onBackToSetup: () -> Unit,
    vm: RaceViewModel = viewModel(),
) {
    val snapshot = vm.snapshot.collectAsStateWithLifecycle().value
    val moving = (snapshot.elapsedMillis - snapshot.stopStats.totalStoppedMillis).coerceAtLeast(0L)
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Race Summary", style = MaterialTheme.typography.headlineSmall)
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text("Totals", style = MaterialTheme.typography.titleMedium)
                Text("Total Race Time: ${snapshot.elapsedMillis.toClock()}")
                Text("Total Laps: ${snapshot.laps.size}")
                Text("Total Distance: ${snapshot.totalDistanceMeters.metersToKm()}")
                Text("Total Stopped: ${snapshot.stopStats.totalStoppedMillis.toClock()}")
                Text("Total Moving: ${moving.toClock()}")
                Text("Average Lap: ${(snapshot.averageLapTimeMillis ?: 0L).toClock()}")
                Text("Fastest Lap: ${(snapshot.bestLapTimeMillis ?: 0L).toClock()}")
                Text(
                    "Slowest Lap: ${(snapshot.laps.maxByOrNull { it.lapTimeMillis }?.lapTimeMillis ?: 0L).toClock()}",
                )
                Text("Stop Count: ${snapshot.stopStats.stopCount}")
            }
        }

        Card(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
        ) {
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(bottom = 8.dp),
            ) {
                Text(
                    "Laps",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 4.dp),
                )
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                ) {
                    item {
                        LapTableHeaderRow()
                        Text(
                            text = "Distances km; speeds km/h. Avg = distance ÷ lap time; max = fastest GPS fix in the lap.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 2.dp, bottom = 8.dp),
                        )
                    }
                    items(snapshot.laps) { lap -> LapTableDataRow(lap) }
                }
            }
        }

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = onBackToSetup,
        ) {
            Text("Back to Setup")
        }
    }
}

@Composable
private fun LapTableHeaderRow() {
    val labelStyle = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text("Lap", style = labelStyle, modifier = Modifier.weight(LapCol.Lap.weight), textAlign = TextAlign.End, maxLines = 2, overflow = TextOverflow.Ellipsis)
        Text("Time", style = labelStyle, modifier = Modifier.weight(LapCol.Time.weight), textAlign = TextAlign.Center, maxLines = 2, overflow = TextOverflow.Ellipsis)
        Text("Moving", style = labelStyle, modifier = Modifier.weight(LapCol.Moving.weight), textAlign = TextAlign.Center, maxLines = 2, overflow = TextOverflow.Ellipsis)
        Text("Stopped", style = labelStyle, modifier = Modifier.weight(LapCol.Stopped.weight), textAlign = TextAlign.Center, maxLines = 2, overflow = TextOverflow.Ellipsis)
        Text("Dist", style = labelStyle, modifier = Modifier.weight(LapCol.Dist.weight), textAlign = TextAlign.End, maxLines = 2, overflow = TextOverflow.Ellipsis)
        Text("Avg", style = labelStyle, modifier = Modifier.weight(LapCol.Avg.weight), textAlign = TextAlign.End, maxLines = 2, overflow = TextOverflow.Ellipsis)
        Text("Max", style = labelStyle, modifier = Modifier.weight(LapCol.Max.weight), textAlign = TextAlign.End, maxLines = 2, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun LapTableDataRow(lap: LapRecord) {
    val body = MaterialTheme.typography.bodySmall
    val avgKmh = lap.averageSpeedKmh()
    val maxKmh = lap.maxSpeedKmh()
    val maxText =
        if (lap.maxSpeedMps > 1e-3) formatSpeedKmhOneDecimal(maxKmh) else "—"
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            lap.lapNumber.toString(),
            style = body,
            modifier = Modifier.weight(LapCol.Lap.weight),
            textAlign = TextAlign.End,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            lap.lapTimeMillis.toClock(),
            style = body,
            modifier = Modifier.weight(LapCol.Time.weight),
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Clip,
        )
        Text(
            lap.movingTimeMillis.toClock(),
            style = body,
            modifier = Modifier.weight(LapCol.Moving.weight),
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Clip,
        )
        Text(
            lap.stoppedTimeMillis.toClock(),
            style = body,
            modifier = Modifier.weight(LapCol.Stopped.weight),
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Clip,
        )
        Text(
            lap.lapDistanceMeters.metersToKmPlain(),
            style = body,
            modifier = Modifier.weight(LapCol.Dist.weight),
            textAlign = TextAlign.End,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            if (avgKmh != null) formatSpeedKmhOneDecimal(avgKmh) else "—",
            style = body,
            modifier = Modifier.weight(LapCol.Avg.weight),
            textAlign = TextAlign.End,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            maxText,
            style = body,
            modifier = Modifier.weight(LapCol.Max.weight),
            textAlign = TextAlign.End,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

private enum class LapCol(val weight: Float) {
    Lap(0.42f),
    Time(1.05f),
    Moving(1.05f),
    Stopped(1.05f),
    Dist(0.82f),
    Avg(0.72f),
    Max(0.72f),
}

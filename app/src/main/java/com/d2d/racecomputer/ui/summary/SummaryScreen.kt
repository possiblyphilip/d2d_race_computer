package com.d2d.racecomputer.ui.summary

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.d2d.racecomputer.ui.RaceViewModel
import com.d2d.racecomputer.ui.metersToKm
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
            Column(Modifier.fillMaxSize()) {
                Text(
                    "Laps",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp),
                )
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    contentPadding = PaddingValues(16.dp),
                ) {
                    items(snapshot.laps) { lap ->
                        Text(
                            "Lap ${lap.lapNumber}: ${lap.lapTimeMillis.toClock()} | Moving: ${lap.movingTimeMillis.toClock()} | Stopped: ${lap.stoppedTimeMillis.toClock()}",
                        )
                    }
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

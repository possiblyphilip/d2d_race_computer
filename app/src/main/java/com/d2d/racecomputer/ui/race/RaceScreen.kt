package com.d2d.racecomputer.ui.race

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.d2d.racecomputer.ui.RaceViewModel
import com.d2d.racecomputer.ui.formatSpeedKmhOneDecimal
import com.d2d.racecomputer.ui.metersToKm
import com.d2d.racecomputer.ui.toClock
import kotlinx.coroutines.delay

@Composable
fun RaceScreen(
    onFinishRace: () -> Unit,
    vm: RaceViewModel = viewModel(),
) {
    val snapshot by vm.snapshot.collectAsStateWithLifecycle()
    val estimatedLapsRemaining = vm.estimateLapsRemaining()

    val tickNowMillis = remember { mutableLongStateOf(System.currentTimeMillis()) }
    val remainingBaseMillis = remember { mutableLongStateOf(vm.raceTimeRemainingMillis()) }
    val remainingBaseTimeMillis = remember { mutableLongStateOf(tickNowMillis.longValue) }
    val currentLapBaseMillis = remember { mutableLongStateOf(snapshot.currentLapTimeMillis) }
    val currentLapBaseTimeMillis = remember { mutableLongStateOf(tickNowMillis.longValue) }
    val currentStopBaseMillis = remember { mutableLongStateOf(snapshot.currentStopDurationMillis) }
    val currentStopBaseTimeMillis = remember { mutableLongStateOf(tickNowMillis.longValue) }

    LaunchedEffect(Unit) {
        while (true) {
            tickNowMillis.longValue = System.currentTimeMillis()
            delay(200L)
        }
    }

    LaunchedEffect(snapshot.elapsedMillis) {
        remainingBaseMillis.longValue = vm.raceTimeRemainingMillis()
        remainingBaseTimeMillis.longValue = tickNowMillis.longValue
    }

    LaunchedEffect(snapshot.currentLapTimeMillis) {
        currentLapBaseMillis.longValue = snapshot.currentLapTimeMillis
        currentLapBaseTimeMillis.longValue = tickNowMillis.longValue
    }

    LaunchedEffect(snapshot.currentStopDurationMillis, snapshot.isCurrentlyStopped) {
        currentStopBaseMillis.longValue = snapshot.currentStopDurationMillis
        currentStopBaseTimeMillis.longValue = tickNowMillis.longValue
    }

    val liveRemainingMillis = (
        remainingBaseMillis.longValue - (tickNowMillis.longValue - remainingBaseTimeMillis.longValue)
        ).coerceAtLeast(0L)
    val liveCurrentLapMillis = (
        currentLapBaseMillis.longValue + (tickNowMillis.longValue - currentLapBaseTimeMillis.longValue)
        ).coerceAtLeast(0L)
    val liveCurrentStopMillis = if (snapshot.isCurrentlyStopped) {
        (currentStopBaseMillis.longValue + (tickNowMillis.longValue - currentStopBaseTimeMillis.longValue)).coerceAtLeast(0L)
    } else {
        0L
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatBox(
                modifier = Modifier.weight(1f),
                label = "Time Remaining",
                value = liveRemainingMillis.toClock(),
            )
            StatBox(
                modifier = Modifier.weight(1f),
                label = "Laps Remaining",
                value = estimatedLapsRemaining.toString(),
            )
        }

        if (snapshot.isCurrentlyStopped) {
            StoppedTakeover(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                stopDuration = liveCurrentStopMillis.toClock(),
            )
        } else {
            Text("Lap Log", style = MaterialTheme.typography.titleLarge)
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                items(snapshot.laps) { lap ->
                    Text(
                        "Lap ${lap.lapNumber}  |  ${lap.lapTimeMillis.toClock()}  |  ${lap.lapDistanceMeters.metersToKm()}  |  Stopped ${lap.stoppedTimeMillis.toClock()}",
                    )
                }
                item {
                    Text(
                        "Lap ${snapshot.lapNumber} (in progress)  |  ${liveCurrentLapMillis.toClock()}  |  ${snapshot.currentLapDistanceMeters.metersToKm()}  |  Stopped ${snapshot.stoppedTimeCurrentLapMillis.toClock()}",
                    )
                }
            }
        }

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                vm.endRace()
                onFinishRace()
            },
        ) { Text("End Race") }

        if (!snapshot.isCurrentlyStopped) {
            StatBox(
                modifier = Modifier.fillMaxWidth(),
                label = "Speed",
                value = "${formatSpeedKmhOneDecimal(snapshot.currentSpeedMps * 3.6)} km/h",
            )
        }
    }
}

@Composable
private fun StoppedTakeover(
    modifier: Modifier = Modifier,
    stopDuration: String,
) {
    Card(
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "Stopped",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
            )
            Text(
                text = stopDuration,
                style = MaterialTheme.typography.displayLarge.copy(fontSize = 84.sp),
                modifier = Modifier
                    .padding(top = 12.dp)
                    .fillMaxWidth(),
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Clip,
            )
        }
    }
}

@Composable
private fun StatBox(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
) {
    Card(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(text = label, style = MaterialTheme.typography.titleMedium)
            Text(
                text = value,
                style = MaterialTheme.typography.displayLarge,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Clip,
            )
        }
    }
}

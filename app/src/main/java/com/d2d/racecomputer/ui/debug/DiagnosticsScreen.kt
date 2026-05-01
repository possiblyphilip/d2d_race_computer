package com.d2d.racecomputer.ui.debug

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.d2d.racecomputer.ui.RaceViewModel
import com.d2d.racecomputer.ui.metersToKm

@Composable
fun DiagnosticsScreen(vm: RaceViewModel = viewModel()) {
    val snap = vm.snapshot.collectAsStateWithLifecycle().value
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("Diagnostics")
        Text("Current lap number: ${snap.lapNumber}")
        Text("Points cadence target: 1 Hz")
        Text("Total distance tracked: ${snap.totalDistanceMeters.metersToKm()}")
        Text("Stop count: ${snap.stopStats.stopCount}")
    }
}

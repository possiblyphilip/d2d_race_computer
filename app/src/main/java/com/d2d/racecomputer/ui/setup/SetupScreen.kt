package com.d2d.racecomputer.ui.setup

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.interaction.FocusInteraction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.d2d.racecomputer.core.domain.model.RaceSettings
import com.d2d.racecomputer.ui.RaceViewModel
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.delay

@Composable
fun SetupScreen(
    onStartRace: () -> Unit,
    vm: RaceViewModel = viewModel(),
) {
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    var startLat by remember { mutableStateOf(TextFieldValue("0.0")) }
    var startLon by remember { mutableStateOf(TextFieldValue("0.0")) }
    var radius by remember { mutableStateOf(TextFieldValue("35")) }
    var minTimeMin by remember { mutableStateOf(TextFieldValue("10")) }
    var minDistM by remember { mutableStateOf(TextFieldValue("2000")) }
    var stopKmh by remember { mutableStateOf(TextFieldValue("3.2")) }
    var raceHours by remember { mutableStateOf(TextFieldValue("12")) }
    var updateSec by remember { mutableStateOf(TextFieldValue("1")) }
    var permissionMessage by remember { mutableStateOf<String?>(null) }
    var shouldSetStartFromCurrentLocation by remember { mutableStateOf(false) }

    fun startRaceSession() {
        val settings = RaceSettings(
            startLatitude = startLat.text.toDoubleOrNull() ?: 0.0,
            startLongitude = startLon.text.toDoubleOrNull() ?: 0.0,
            lapEnterRadiusMeters = (radius.text.toFloatOrNull() ?: 35f).coerceAtLeast(10f),
            lapExitRadiusMeters = ((radius.text.toFloatOrNull() ?: 35f) * 1.7f).coerceAtLeast(25f),
            minLapTimeMillis = ((minTimeMin.text.toLongOrNull() ?: 10L) * 60_000L),
            minLapDistanceMeters = minDistM.text.toDoubleOrNull() ?: 2000.0,
            stopSpeedThresholdMps = ((stopKmh.text.toDoubleOrNull() ?: 3.2) / 3.6),
            raceDurationMillis = (raceHours.text.toLongOrNull() ?: 12L) * 3_600_000L,
            gpsUpdateMs = (updateSec.text.toLongOrNull() ?: 1L) * 1000L,
        )
        vm.saveSettings(settings)
        vm.startRace()
        onStartRace()
    }

    fun setCurrentLocationAsStartFinish() {
        val cancellationTokenSource = CancellationTokenSource()
        fusedLocationClient.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            cancellationTokenSource.token,
        ).addOnSuccessListener { location ->
            if (location != null) {
                startLat = TextFieldValue(location.latitude.toString())
                startLon = TextFieldValue(location.longitude.toString())
                permissionMessage = null
            } else {
                permissionMessage = "Unable to get current location. Move to open sky and try again."
            }
        }.addOnFailureListener {
            permissionMessage = "Could not read location. Check GPS and try again."
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { grantMap ->
        val hasFineLocation = grantMap[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val hasCoarseLocation = grantMap[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (hasFineLocation || hasCoarseLocation) {
            permissionMessage = null
            if (shouldSetStartFromCurrentLocation) {
                shouldSetStartFromCurrentLocation = false
                setCurrentLocationAsStartFinish()
            } else {
                startRaceSession()
            }
        } else {
            permissionMessage = "Location permission is required to start race tracking."
            shouldSetStartFromCurrentLocation = false
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text("Dusk Till Dawn Setup")
        NumericSetupField(
            value = startLat,
            onValueChange = { startLat = it },
            label = "Start Latitude",
            keyboardType = KeyboardType.Decimal,
        )
        NumericSetupField(
            value = startLon,
            onValueChange = { startLon = it },
            label = "Start Longitude",
            keyboardType = KeyboardType.Decimal,
        )
        NumericSetupField(
            value = radius,
            onValueChange = { radius = it },
            label = "Lap Zone Radius (m)",
            keyboardType = KeyboardType.Decimal,
        )
        NumericSetupField(
            value = minTimeMin,
            onValueChange = { minTimeMin = it },
            label = "Minimum Lap Time (min)",
            keyboardType = KeyboardType.Number,
        )
        NumericSetupField(
            value = minDistM,
            onValueChange = { minDistM = it },
            label = "Minimum Lap Distance (m)",
            keyboardType = KeyboardType.Decimal,
        )
        NumericSetupField(
            value = stopKmh,
            onValueChange = { stopKmh = it },
            label = "Stopped Threshold (km/h)",
            keyboardType = KeyboardType.Decimal,
        )
        NumericSetupField(
            value = raceHours,
            onValueChange = { raceHours = it },
            label = "Race Duration (hours)",
            keyboardType = KeyboardType.Number,
        )
        NumericSetupField(
            value = updateSec,
            onValueChange = { updateSec = it },
            label = "GPS Update (sec)",
            keyboardType = KeyboardType.Number,
        )
        permissionMessage?.let { Text(it) }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = {
                    val hasFineLocation = ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                    ) == PackageManager.PERMISSION_GRANTED
                    val hasCoarseLocation = ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                    ) == PackageManager.PERMISSION_GRANTED

                    if (hasFineLocation || hasCoarseLocation) {
                        setCurrentLocationAsStartFinish()
                    } else {
                        shouldSetStartFromCurrentLocation = true
                        permissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION,
                            ),
                        )
                    }
                },
            ) { Text("Set Start/Finish Here") }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = {
                    val hasFineLocation = ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                    ) == PackageManager.PERMISSION_GRANTED
                    val hasCoarseLocation = ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                    ) == PackageManager.PERMISSION_GRANTED

                    if (hasFineLocation || hasCoarseLocation) {
                        permissionMessage = null
                        startRaceSession()
                    } else {
                        shouldSetStartFromCurrentLocation = false
                        permissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION,
                            ),
                        )
                    }
                },
            ) { Text("Start Race") }
        }
    }
}

@Composable
private fun NumericSetupField(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    label: String,
    keyboardType: KeyboardType,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val valueForFocus by rememberUpdatedState(value)
    LaunchedEffect(interactionSource) {
        interactionSource.interactions.collect { interaction ->
            if (interaction is FocusInteraction.Focus) {
                // Wait until after the next TextField layout so select-all survives recomposition.
                delay(16)
                onValueChange(
                    valueForFocus.copy(selection = TextRange(0, valueForFocus.text.length)),
                )
            }
        }
    }
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        interactionSource = interactionSource,
        modifier = Modifier.fillMaxWidth(),
    )
}

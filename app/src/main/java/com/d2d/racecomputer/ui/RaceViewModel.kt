package com.d2d.racecomputer.ui

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.d2d.racecomputer.core.domain.model.RaceSettings
import com.d2d.racecomputer.core.domain.model.RaceSnapshot
import com.d2d.racecomputer.core.location.RaceLocationService
import com.d2d.racecomputer.core.location.RaceRuntime
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class RaceViewModel(application: Application) : AndroidViewModel(application) {
    val snapshot: StateFlow<RaceSnapshot> = RaceRuntime.snapshot.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = RaceSnapshot(),
    )

    private var settings = RaceSettings(startLatitude = 0.0, startLongitude = 0.0)

    fun saveSettings(newSettings: RaceSettings) {
        settings = newSettings
        RaceRuntime.updateSettings(newSettings)
    }

    fun startRace() {
        val now = System.currentTimeMillis()
        RaceRuntime.startRace(now)
        val intent = Intent(getApplication(), RaceLocationService::class.java)
        runCatching {
            getApplication<Application>().startForegroundService(intent)
        }.onFailure {
            RaceRuntime.endRace(System.currentTimeMillis())
        }
    }

    fun pauseRace() {
        getApplication<Application>().stopService(Intent(getApplication(), RaceLocationService::class.java))
    }

    fun endRace() {
        RaceRuntime.endRace(System.currentTimeMillis())
        getApplication<Application>().stopService(Intent(getApplication(), RaceLocationService::class.java))
    }

    fun addManualLap() {
        RaceRuntime.addManualLap(System.currentTimeMillis())
    }

    fun undoLastLap() {
        RaceRuntime.undoLastLap()
    }

    fun addPitNote(note: String) {
        viewModelScope.launch {
            if (note.isNotBlank()) {
                RaceRuntime.repository.savePitNote(note, System.currentTimeMillis())
            }
        }
    }

    fun estimateLapsRemaining(): Int {
        val elapsed = snapshot.value.elapsedMillis
        val avg = snapshot.value.averageLapTimeMillis ?: return 0
        val remaining = (settings.raceDurationMillis - elapsed).coerceAtLeast(0L)
        return if (avg > 0) (remaining / avg).toInt() else 0
    }

    fun raceTimeRemainingMillis(): Long {
        val elapsed = snapshot.value.elapsedMillis
        return (settings.raceDurationMillis - elapsed).coerceAtLeast(0L)
    }
}

package com.d2d.racecomputer.core.location

import com.d2d.racecomputer.core.data.repo.RaceRepository
import com.d2d.racecomputer.core.domain.engine.LapStateMachine
import com.d2d.racecomputer.core.domain.engine.StopDetector
import com.d2d.racecomputer.core.domain.model.GpsSample
import com.d2d.racecomputer.core.domain.model.RaceSettings
import com.d2d.racecomputer.core.domain.model.RaceSnapshot
import kotlin.math.abs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

object RaceRuntime {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val _snapshot = MutableStateFlow(RaceSnapshot())
    val snapshot: StateFlow<RaceSnapshot> = _snapshot.asStateFlow()

    lateinit var repository: RaceRepository
        private set

    private var lapStateMachine: LapStateMachine? = null
    private var stopDetector: StopDetector? = null
    private var settings: RaceSettings = RaceSettings(0.0, 0.0)
    private var running = false
    private var lastSavedLapNumber = 0

    fun initialize(repo: RaceRepository, initialSettings: RaceSettings) {
        repository = repo
        settings = initialSettings
        lapStateMachine = LapStateMachine(settings)
        stopDetector = StopDetector(settings)
    }

    fun updateSettings(newSettings: RaceSettings) {
        settings = newSettings
        lapStateMachine = LapStateMachine(settings)
        stopDetector = StopDetector(settings)
    }

    fun isRunning(): Boolean = running

    fun startRace(nowMillis: Long) {
        val lapMachine = LapStateMachine(settings)
        lapMachine.start(nowMillis)
        lapStateMachine = lapMachine
        stopDetector = StopDetector(settings)
        running = true
        lastSavedLapNumber = 0
        scope.launch { repository.startRace(nowMillis) }
    }

    fun endRace(nowMillis: Long) {
        stopDetector?.finalizeStop(nowMillis)
        running = false
        scope.launch { repository.endRace(nowMillis) }
    }

    fun addManualLap(nowMillis: Long) {
        lapStateMachine?.addManualLap(nowMillis)
        _snapshot.value = lapStateMachine?.onSample(
            sample = GpsSample(settings.startLatitude, settings.startLongitude, 0.0, 5f, nowMillis),
            isStopped = true,
        ) ?: _snapshot.value
    }

    fun undoLastLap() {
        lapStateMachine?.undoLastLap()
        scope.launch { repository.undoLap() }
    }

    fun onSample(sample: GpsSample) {
        if (!running) return
        val stop = stopDetector?.onSample(sample) ?: false
        val snap = lapStateMachine?.onSample(sample, stop) ?: return
        val detector = stopDetector
        val speedMps = abs(sample.speedMps).coerceAtLeast(0.0)
        _snapshot.value = snap.copy(
            stopStats = detector?.snapshot() ?: snap.stopStats,
            isCurrentlyStopped = detector?.isCurrentlyStopped() == true,
            currentStopDurationMillis = detector?.currentStopDurationMillis(sample.timestampMillis) ?: 0L,
            currentSpeedMps = speedMps,
        )
        scope.launch {
            repository.queueGps(sample)
            val latestLap = snap.laps.lastOrNull()
            if (latestLap != null && latestLap.lapNumber > lastSavedLapNumber) {
                repository.saveLap(latestLap)
                lastSavedLapNumber = latestLap.lapNumber
            }
        }
    }
}

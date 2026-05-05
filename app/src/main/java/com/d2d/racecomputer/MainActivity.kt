package com.d2d.racecomputer

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Surface
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.d2d.racecomputer.core.location.RaceRuntime
import com.d2d.racecomputer.data.AppContainer
import com.d2d.racecomputer.ui.navigation.AppNavHost
import com.d2d.racecomputer.ui.theme.D2DRaceTheme
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private var hadScreenStayOnReason: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.statusBars() or WindowInsetsCompat.Type.navigationBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
        AppContainer.initialize(this)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                combine(
                    RaceRuntime.snapshot,
                    RaceRuntime.raceActive,
                ) { snap, active -> snap to active }
                    .collect { (snap, raceActive) ->
                        updateScreenWakePolicy(
                            raceActive = raceActive,
                            inStartZone = snap.isInStartFinishZone,
                            stopped = snap.isCurrentlyStopped,
                        )
                    }
            }
        }

        setContent {
            D2DRaceTheme {
                Surface {
                    AppNavHost()
                }
            }
        }
    }

    /**
     * While a race is active, keep the display on (and wake it) only near start/finish or when stopped.
     * Otherwise the device can sleep per the user's display timeout.
     */
    private fun updateScreenWakePolicy(raceActive: Boolean, inStartZone: Boolean, stopped: Boolean) {
        val stayOn = raceActive && (inStartZone || stopped)
        if (stayOn) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        if (stayOn && !hadScreenStayOnReason) {
            window.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)
        }
        hadScreenStayOnReason = stayOn
    }
}

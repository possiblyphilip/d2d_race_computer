package com.d2d.racecomputer.ui

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel

/** Same [RaceViewModel] for every screen (setup / race / summary); navigation entries each get their own store by default. */
@Composable
fun activityRaceViewModel(): RaceViewModel {
    val activity = LocalContext.current as ComponentActivity
    return viewModel(viewModelStoreOwner = activity)
}

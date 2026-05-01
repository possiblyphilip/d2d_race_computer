package com.d2d.racecomputer.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.d2d.racecomputer.ui.race.RaceScreen
import com.d2d.racecomputer.ui.setup.SetupScreen
import com.d2d.racecomputer.ui.summary.SummaryScreen

@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = "setup",
    ) {
        composable("setup") {
            SetupScreen(
                onStartRace = { navController.navigate("race") },
            )
        }
        composable("race") {
            RaceScreen(
                onFinishRace = { navController.navigate("summary") },
            )
        }
        composable("summary") { SummaryScreen(onBackToSetup = { navController.popBackStack("setup", false) }) }
    }
}

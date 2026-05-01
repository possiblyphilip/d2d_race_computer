package com.d2d.racecomputer.ui

fun Long.toClock(): String {
    val totalSeconds = this / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return "%02d:%02d:%02d".format(hours, minutes, seconds)
}

fun Double.metersToKm(): String = "%.2f km".format(this / 1000.0)

/** Distance only, two decimals — for table columns that already spell out units in the header. */
fun Double.metersToKmPlain(): String = "%.2f".format(this / 1000.0)

fun formatSpeedKmhOneDecimal(kmh: Double): String = "%.1f".format(kmh)

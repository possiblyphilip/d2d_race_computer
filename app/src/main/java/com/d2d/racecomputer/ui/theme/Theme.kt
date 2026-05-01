package com.d2d.racecomputer.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val NightRaceColorScheme: ColorScheme = darkColorScheme(
    primary = Color(0xFFFF3030),
    onPrimary = Color(0xFF000000),
    secondary = Color(0xFFFF3030),
    onSecondary = Color(0xFF000000),
    background = Color(0xFF050507),
    surface = Color(0xFF111217),
    outline = Color(0xFF9E1F1F),
    outlineVariant = Color(0xFF6B1414),
    onBackground = Color(0xFFFF3030),
    onSurface = Color(0xFFFF3030),
    onSurfaceVariant = Color(0xFFFF6B6B),
)

private val RedShiftColorScheme: ColorScheme = darkColorScheme(
    primary = Color(0xFFFF3030),
    onPrimary = Color(0xFF000000),
    secondary = Color(0xFFFF3030),
    onSecondary = Color(0xFF000000),
    background = Color(0xFF0A0000),
    surface = Color(0xFF160808),
    outline = Color(0xFF9E1F1F),
    outlineVariant = Color(0xFF6B1414),
    onBackground = Color(0xFFFF3030),
    onSurface = Color(0xFFFF3030),
    onSurfaceVariant = Color(0xFFFF6B6B),
)

@Composable
fun D2DRaceTheme(
    useRedShift: Boolean = false,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (useRedShift) RedShiftColorScheme else NightRaceColorScheme,
        content = content,
    )
}

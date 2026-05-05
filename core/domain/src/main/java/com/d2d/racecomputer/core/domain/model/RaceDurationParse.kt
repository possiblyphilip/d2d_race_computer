package com.d2d.racecomputer.core.domain.model

import kotlin.math.roundToLong

/**
 * Parses a race length in **hours** from setup text, including fractions (e.g. `0.166` ≈ 10 min).
 * Accepts a leading dot (`.1666` → 0.1666 h) and comma as decimal separator.
 * Invalid or blank text uses [defaultHours]. Result is at least 1 ms.
 */
fun raceDurationMillisFromHoursText(text: String, defaultHours: Double = 12.0): Long {
    val normalized = text.trim().replace(',', '.').let { t ->
        when {
            t.isEmpty() -> return (defaultHours * 3_600_000.0).roundToLong().coerceAtLeast(1L)
            t.startsWith('.') -> "0$t"
            else -> t
        }
    }
    val hours = normalized.toDoubleOrNull()?.coerceAtLeast(0.0) ?: defaultHours
    return (hours * 3_600_000.0).roundToLong().coerceAtLeast(1L)
}

package com.d2d.racecomputer.core.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class RaceDurationParseTest {

    @Test
    fun fractionalHours_tenMinutesApprox() {
        assertEquals(597_600L, raceDurationMillisFromHoursText("0.166"))
    }

    @Test
    fun integerHours_stillWorks() {
        assertEquals(12 * 3_600_000L, raceDurationMillisFromHoursText("12"))
    }

    @Test
    fun blankFallsBackToTwelveHours() {
        assertEquals(12 * 3_600_000L, raceDurationMillisFromHoursText(""))
    }

    @Test
    fun leadingDotParsesLikeZeroPoint() {
        val withZero = raceDurationMillisFromHoursText("0.1666")
        val leadingDot = raceDurationMillisFromHoursText(".1666")
        assertEquals(withZero, leadingDot)
        assertEquals(599_760L, leadingDot)
    }
}

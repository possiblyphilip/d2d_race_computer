package com.d2d.racecomputer.core.domain.gpx

import com.d2d.racecomputer.core.domain.geo.GeoMath
import com.d2d.racecomputer.core.domain.model.GpsSample
import java.io.InputStream
import java.time.Instant
import java.util.regex.Pattern

/**
 * Minimal GPX 1.1 track parser for tests — reads [trkpt] lat/lon and following [time].
 */
object GpxTrackParser {
    private val trkptBlock: Pattern = Pattern.compile(
        "<trkpt\\s+lat=\"([^\"]+)\"\\s+lon=\"([^\"]+)\">[\\s\\S]*?<time>([^<]+)</time>",
        Pattern.DOTALL,
    )

    fun parsePoints(xml: String): List<GpsSample> {
        val matcher = trkptBlock.matcher(xml)
        val raw = mutableListOf<Triple<Double, Double, Long>>()
        while (matcher.find()) {
            val lat = matcher.group(1)!!.toDouble()
            val lon = matcher.group(2)!!.toDouble()
            val t = Instant.parse(matcher.group(3)!!.trim()).toEpochMilli()
            raw += Triple(lat, lon, t)
        }
        if (raw.isEmpty()) {
            throw IllegalArgumentException("No trkpt/time pairs found in GPX")
        }
        return raw.mapIndexed { index, p ->
            val speedMps = if (index == 0) {
                0.0
            } else {
                val prev = raw[index - 1]
                val dtMillis = (p.third - prev.third).coerceAtLeast(1L)
                val meters = GeoMath.distanceMeters(prev.first, prev.second, p.first, p.second)
                meters / (dtMillis / 1000.0)
            }
            GpsSample(
                latitude = p.first,
                longitude = p.second,
                speedMps = speedMps,
                accuracyMeters = 8f,
                timestampMillis = p.third,
            )
        }
    }

    fun parsePoints(stream: InputStream): List<GpsSample> =
        parsePoints(stream.bufferedReader().readText())
}

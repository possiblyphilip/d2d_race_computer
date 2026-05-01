package com.d2d.racecomputer.core.data.db.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "race_sessions")
data class RaceSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val startedAtMillis: Long,
    val endedAtMillis: Long? = null,
    val isPaused: Boolean = false,
)

@Entity(
    tableName = "lap_records",
    foreignKeys = [
        ForeignKey(
            entity = RaceSessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["raceId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("raceId")],
)
data class LapRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val raceId: Long,
    val lapNumber: Int,
    val lapTimeMillis: Long,
    val lapDistanceMeters: Double,
    val movingTimeMillis: Long,
    val stoppedTimeMillis: Long,
    val maxSpeedMps: Double = 0.0,
)

@Entity(
    tableName = "gps_points",
    foreignKeys = [
        ForeignKey(
            entity = RaceSessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["raceId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("raceId"), Index("timestampMillis")],
)
data class GpsPointEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val raceId: Long,
    val latitude: Double,
    val longitude: Double,
    val speedMps: Double,
    val accuracyMeters: Float,
    val timestampMillis: Long,
)

@Entity(
    tableName = "stop_events",
    foreignKeys = [
        ForeignKey(
            entity = RaceSessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["raceId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("raceId")],
)
data class StopEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val raceId: Long,
    val startedAtMillis: Long,
    val endedAtMillis: Long,
    val reason: String? = null,
)

@Entity(
    tableName = "pit_notes",
    foreignKeys = [
        ForeignKey(
            entity = RaceSessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["raceId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("raceId")],
)
data class PitNoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val raceId: Long,
    val timestampMillis: Long,
    val note: String,
)

@Entity(tableName = "app_settings")
data class AppSettingsEntity(
    @PrimaryKey val id: Int = 1,
    val startLatitude: Double = 0.0,
    val startLongitude: Double = 0.0,
    val lapEnterRadiusMeters: Float = 35f,
    val lapExitRadiusMeters: Float = 60f,
    val minLapTimeMillis: Long = 10 * 60 * 1000L,
    val minLapDistanceMeters: Double = 2000.0,
    val stopSpeedThresholdMps: Double = 0.9,
    val stopSustainMillis: Long = 7000L,
    val raceDurationMillis: Long = 12 * 60 * 60 * 1000L,
    val gpsUpdateMillis: Long = 1000L,
)

package com.example.mobilebetriebsysteme_android_app.data.session

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Data class representing a walking session entity stored in the local Room database.
 */
@Entity(tableName = "sessions")
data class WalkingSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val durationSeconds: Int,            // Total duration of the walking session in seconds
    val distanceMeters: Float,           // Total distance walked during the session in meters
    val timestamp: Long = System.currentTimeMillis(), // Timestamp when the session was recorded
    val steps: Int = 0,                  // Total Steps in a session
    val isDualMode: Boolean = false       // DualMode Session or not?
)

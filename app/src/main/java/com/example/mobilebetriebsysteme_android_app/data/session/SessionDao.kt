package com.example.mobilebetriebsysteme_android_app.data.session

import androidx.room.*
import kotlinx.coroutines.flow.Flow



/**
 * Data Access Object (DAO) interface for accessing walking session data in the Room database.
 */
@Dao
interface SessionDao {

    /**
     * Inserts a new walking session into the database.
     * If a session with the same ID already exists, it will be replaced.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: WalkingSessionEntity)

    /**
     * Retrieves all walking sessions from the database, ordered by timestamp descending.
     * Returns a Flow for real-time updates.
     */
    @Query("SELECT * FROM sessions ORDER BY timestamp DESC")
    fun getAllSessions(): Flow<List<WalkingSessionEntity>>

    /**
     * Retrieves a single walking session by its unique ID.
     */
    @Query("SELECT * FROM sessions WHERE id = :sessionId")
    suspend fun getSessionById(sessionId: Int): WalkingSessionEntity?

    /**
     * Deletes a specific walking session from the database.
     */
    @Delete
    suspend fun deleteSession(session: WalkingSessionEntity)

    /**
     * Deletes all walking sessions from the database.
     */
    @Query("DELETE FROM sessions")
    suspend fun clearAllSessions()
}

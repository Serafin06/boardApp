package com.example.apprafal.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update

@Dao
interface GameSessionParticipantDao {
    @Insert
    suspend fun insertAll(participants: List<GameSessionParticipant>)

    @Insert
    suspend fun insert(participant: GameSessionParticipant)

    @Update
    suspend fun update(participant: GameSessionParticipant)

    // Pobierz uczestników sesji
    @Query("SELECT * FROM session_participants WHERE sessionId = :sessionId ORDER BY queuePosition ASC")
    suspend fun getParticipantsForSession(sessionId: String): List<GameSessionParticipant>

    // KOLEJKA - główne metody
    @Query("""
        SELECT * FROM session_participants 
        WHERE sessionId = :sessionId 
        AND isPresent = 1 
        AND canPickInSession = 1 
        AND isSkipped = 0
        ORDER BY queuePosition ASC
    """)
    suspend fun getActiveQueue(sessionId: String): List<GameSessionParticipant>

    @Query("""
        SELECT * FROM session_participants 
        WHERE sessionId = :sessionId 
        AND isPresent = 1 
        AND canPickInSession = 1 
        AND isSkipped = 0
        ORDER BY queuePosition ASC 
        LIMIT 1
    """)
    suspend fun getFirstAvailablePicker(sessionId: String): GameSessionParticipant?

    @Query("SELECT * FROM session_participants WHERE sessionId = :sessionId AND playerId = :playerId")
    suspend fun getParticipant(sessionId: String, playerId: Int): GameSessionParticipant?

    // ZARZĄDZANIE KOLEJKĄ
    @Query("SELECT MAX(queuePosition) FROM session_participants WHERE sessionId = :sessionId")
    suspend fun getMaxPosition(sessionId: String): Int?

    @Query("UPDATE session_participants SET queuePosition = :newPosition WHERE id = :participantId")
    suspend fun updateQueuePosition(participantId: String, newPosition: Int)

    @Query("UPDATE session_participants SET isSkipped = :skipped WHERE sessionId = :sessionId AND playerId = :playerId")
    suspend fun setSkipped(sessionId: String, playerId: Int, skipped: Boolean)

    @Query("""
        UPDATE session_participants 
        SET hasPickedInSession = :hasPicked, lastPickTimestamp = :timestamp 
        WHERE sessionId = :sessionId AND playerId = :playerId
    """)
    suspend fun markAsHasPicked(sessionId: String, playerId: Int, hasPicked: Boolean, timestamp: Long?)

    // PRZESUWANIE W KOLEJCE
    @Transaction
    suspend fun moveToEndOfQueue(sessionId: String, participantId: String) {
        val maxPos = getMaxPosition(sessionId) ?: 0
        updateQueuePosition(participantId, maxPos + 1)
    }

    // Pomocnicze query dla UI
    @Query("""
        SELECT sp.*, p.name as playerName
        FROM session_participants sp
        INNER JOIN players p ON sp.playerId = p.id
        WHERE sp.sessionId = :sessionId
        ORDER BY sp.queuePosition ASC
    """)
    suspend fun getParticipantsWithNames(sessionId: String): List<ParticipantWithName>
}
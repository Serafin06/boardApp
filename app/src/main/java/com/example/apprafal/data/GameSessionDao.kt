package com.example.apprafal.data


import androidx.room.*

@Dao
interface GameSessionDao {

    @Insert
    suspend fun insertSession(session: GameSession)

    @Insert
    suspend fun insertParticipants(participants: List<GameSessionParticipant>)

    @Transaction
    suspend fun createSessionWithParticipants(session: GameSession, participants: List<GameSessionParticipant>) {
        insertSession(session)
        insertParticipants(participants)
    }

    @Query("SELECT * FROM game_sessions ORDER BY date DESC")
    suspend fun getAllSessions(): List<GameSession>

    @Query("SELECT * FROM session_participants WHERE sessionId = :sessionId ORDER BY queuePosition ASC")
    suspend fun getParticipantsForSession(sessionId: String): List<GameSessionParticipant>
}


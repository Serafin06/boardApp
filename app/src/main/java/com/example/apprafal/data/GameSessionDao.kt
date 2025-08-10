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

    @Query("SELECT * FROM game_sessions WHERE id = :sessionId")
    suspend fun getSessionById(sessionId: String): GameSession?

    @Query("UPDATE game_sessions SET currentPickerId = :playerId WHERE id = :sessionId")
    suspend fun updateCurrentPicker(sessionId: String, playerId: Int?)

    @Query("UPDATE game_sessions SET isCompleted = :completed WHERE id = :sessionId")
    suspend fun markSessionCompleted(sessionId: String, completed: Boolean)
}


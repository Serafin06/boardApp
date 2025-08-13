package com.example.apprafal.data

import androidx.room.*

@Dao
interface GameSessionDao {

    // Podstawowe operacje na sesjach gier
    @Insert
    suspend fun insertSession(session: GameSession)

    @Insert
    suspend fun insertParticipants(participants: List<GameSessionParticipant>)


    @Transaction
    suspend fun createSessionWithParticipants(session: GameSession, participants: List<GameSessionParticipant>) {
        // Najpierw zapisz sesję
        insertSession(session)
        // Potem zapisz uczestników - jeśli sesja się nie zapisze, uczestnicy też nie
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


    @Query("SELECT * FROM game_sessions ORDER BY date DESC LIMIT 1")
    suspend fun getLatestSession(): GameSession?
}
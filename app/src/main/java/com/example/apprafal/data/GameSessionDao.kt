package com.example.apprafal.data

import androidx.room.*

@Dao
interface GameSessionDao {

    // Podstawowe operacje na sesjach gier
    @Insert
    suspend fun insertSession(session: GameSession)

    @Insert
    suspend fun insertParticipants(participants: List<GameSessionParticipant>)

    /**
     * Transakcja - tworzy sesję razem z uczestnikami w jednej operacji atomowej
     * Zapewnia że albo wszystko się powiedzie, albo nic się nie zapisze
     */
    @Transaction
    suspend fun createSessionWithParticipants(session: GameSession, participants: List<GameSessionParticipant>) {
        // Najpierw zapisz sesję
        insertSession(session)
        // Potem zapisz uczestników - jeśli sesja się nie zapisze, uczestnicy też nie
        insertParticipants(participants)
    }

    /**
     * Pobiera wszystkie sesje posortowane od najnowszej (DESC = od największej daty)
     */
    @Query("SELECT * FROM game_sessions ORDER BY date DESC")
    suspend fun getAllSessions(): List<GameSession>

    /**
     * Znajdź konkretną sesję po jej unikalnym ID
     */
    @Query("SELECT * FROM game_sessions WHERE id = :sessionId")
    suspend fun getSessionById(sessionId: String): GameSession?

    /**
     * Aktualizuje kto obecnie wybiera grę w danej sesji
     * Używane gdy gracz skończy wybierać i przejdziemy do następnego
     */
    @Query("UPDATE game_sessions SET currentPickerId = :playerId WHERE id = :sessionId")
    suspend fun updateCurrentPicker(sessionId: String, playerId: Int?)

    /**
     * Oznacza sesję jako zakończoną (gdy wszyscy już wybrali lub sesja się skończyła)
     */
    @Query("UPDATE game_sessions SET isCompleted = :completed WHERE id = :sessionId")
    suspend fun markSessionCompleted(sessionId: String, completed: Boolean)

    /**
     * NOWA METODA: Pobiera najnowszą sesję (ostatnio utworzoną)
     * Przydatne gdy chcemy wyświetlić aktualną kolejkę bez podawania sessionId
     */
    @Query("SELECT * FROM game_sessions ORDER BY date DESC LIMIT 1")
    suspend fun getLatestSession(): GameSession?
}
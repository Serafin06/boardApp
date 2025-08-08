package com.example.apprafal.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query


@Dao
interface GamePickDao {
    @Insert
    suspend fun insert(gamePick: GamePick)

    // Pobierz tylko 5 ostatnich wyborów (najnowsze pierwsze)
    @Query("SELECT * FROM GamePick WHERE sessionId = :sessionId ORDER BY timestamp DESC LIMIT 5")
    fun getPicksForSession(sessionId: String): LiveData<List<GamePick>>

    // Pobierz ostatni wybór dla usunięcia
    @Query("SELECT * FROM GamePick WHERE sessionId = :sessionId ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLastPick(sessionId: String): GamePick?

    // Usuń konkretny wybór
    @Delete
    suspend fun delete(gamePick: GamePick)

    // Pobierz wszystkie wybory (dla debugowania)
    @Query("SELECT * FROM GamePick WHERE sessionId = :sessionId ORDER BY timestamp DESC")
    suspend fun getAllPicksForSession(sessionId: String): List<GamePick>
}
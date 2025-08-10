package com.example.apprafal.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update


@Dao
interface GamePickDao {
    @Insert
    suspend fun insert(gamePick: GamePick)

    @Delete
    suspend fun delete(gamePick: GamePick)

    @Update
    suspend fun update(gamePick: GamePick)

    // LiveData dla UI
    @Query("SELECT * FROM game_picks WHERE sessionId = :sessionId ORDER BY pickOrder DESC, timestamp DESC")
    fun getPicksForSession(sessionId: String): LiveData<List<GamePick>>

    // Suspend functions dla logiki biznesowej
    @Query("SELECT * FROM game_picks WHERE sessionId = :sessionId ORDER BY pickOrder DESC, timestamp DESC")
    suspend fun getAllPicksForSession(sessionId: String): List<GamePick>

    @Query("SELECT * FROM game_picks WHERE sessionId = :sessionId ORDER BY pickOrder DESC, timestamp DESC LIMIT 1")
    suspend fun getLastPick(sessionId: String): GamePick?

    @Query("SELECT COUNT(*) FROM game_picks WHERE sessionId = :sessionId")
    suspend fun getPickCount(sessionId: String): Int

    @Query("SELECT COALESCE(MAX(pickOrder), 0) + 1 FROM game_picks WHERE sessionId = :sessionId")
    suspend fun getNextPickOrder(sessionId: String): Int

    // Dla cofania - pobierz wybory gracza
    @Query("SELECT * FROM game_picks WHERE sessionId = :sessionId AND playerId = :playerId ORDER BY timestamp DESC")
    suspend fun getPicksForPlayer(sessionId: String, playerId: Int): List<GamePick>
}
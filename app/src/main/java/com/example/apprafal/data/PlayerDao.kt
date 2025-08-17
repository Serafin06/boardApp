package com.example.apprafal.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface PlayerDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(player: Player)

    @Query("SELECT * FROM players")
    fun getAllPlayers(): LiveData<List<Player>>

    @Query("SELECT * FROM players WHERE canChooseGame = 1 ORDER BY queuePosition ASC")
    fun getQueue(): LiveData<List<Player>>

    @Query("SELECT count(*) FROM players where canChooseGame = 1")
    fun getQueueSize(): Int

    @Query("SELECT * FROM players WHERE id = :id")
    suspend fun getById(id: Int): Player

    @Query("Select queuePosition from players where id = :id")
    suspend fun getQueuePosition(id: Int): Int

    @Update
    suspend fun updatePlayer(player: Player)

    @Query("SELECT * FROM players WHERE id = :playerId")
    suspend fun getPlayerById(playerId: Int): Player?

    @Query("SELECT * FROM players where canChooseGame = 1 ORDER BY queuePosition desc")
    suspend fun getAllQueue(): List<Player>

}

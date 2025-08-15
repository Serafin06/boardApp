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

    @Query("SELECT * FROM players WHERE id = :id")
    suspend fun getById(id: Int): Player

    @Transaction
    suspend fun updateQueuePosition(playerId: Int, newPosition: Int) {
        val player = getById(playerId)
        player.queuePosition = newPosition
        insert(player)
    }


}

package com.example.apprafal.data

import androidx.lifecycle.LiveData


class PlayerRepo (private val dao: PlayerDao) {
    fun getAllPlayers() = dao.getAllPlayers()
    suspend fun insert(player: Player) = dao.insert(player)
    suspend fun getById(id: Int): Player = dao.getById(id)

    fun getQueue(): LiveData<List<Player>> = dao.getQueue()

    suspend fun updatePlayerQueuePosition(playerId: Int, newPosition: Int) {
        val player = dao.getPlayerById(playerId)
        if (player != null) {
            val updatedPlayer = player.copy(queuePosition = newPosition)
            dao.updatePlayer(updatedPlayer)
        }
    }
    suspend fun getPlayerById(playerId: Int): Player? = dao.getPlayerById(playerId)

    suspend fun getAllQueue(): List<Player> = dao.getAllQueue()

}
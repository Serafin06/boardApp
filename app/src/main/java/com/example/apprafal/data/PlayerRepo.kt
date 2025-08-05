package com.example.apprafal.data

import androidx.lifecycle.LiveData


class PlayerRepo (private val dao: PlayerDao) {
    fun getAllPlayers() = dao.getAllPlayers()
    suspend fun insert(player: Player) = dao.insert(player)

    fun getQueue(): LiveData<List<Player>> = dao.getQueue()

}
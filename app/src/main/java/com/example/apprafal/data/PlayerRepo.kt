package com.example.apprafal.data

import androidx.lifecycle.LiveData


class PlayerRepo (private val dao: PlayerDao) {
    fun getAllPlayers() = dao.getAllPlayers()
    suspend fun insert(player: Player) = dao.insert(player)
    suspend fun getById(id: Int): Player = dao.getById(id)


}
package com.example.apprafal.data

class PlayerRepo (private val dao: PlayerDao) {
    val allPlayers = dao.getAllPlayers()
    suspend fun insert(player: Player) = dao.insert(player)
}
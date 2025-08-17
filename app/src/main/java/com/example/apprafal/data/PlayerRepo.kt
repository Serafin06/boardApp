package com.example.apprafal.data

import androidx.lifecycle.LiveData


class PlayerRepo (private val dao: PlayerDao) {
    fun getAllPlayers() = dao.getAllPlayers()
    suspend fun insert(player: Player) = dao.insert(player)
    suspend fun getById(id: Int): Player = dao.getById(id)

    //Kolejka graczy ktozy moga wybierac
    fun getQueue(): LiveData<List<Player>> = dao.getQueue()

    //ilosc graczy ktora moze wybierac
    fun getQueueSize(): Int = dao.getQueueSize()

    //pobieranie pozycji gracza
    suspend fun getQueuePosition(id: Int): Int = dao.getQueuePosition(id)


    suspend fun updatePlayerQueuePosition(playerId: Int, newPosition: Int) {
        val player = dao.getPlayerById(playerId)
        if (player != null) {
            val updatedPlayer = player.copy(queuePosition = newPosition)
            dao.updatePlayer(updatedPlayer)
        }
    }

    //sprawdzanie pozycji, czy juz taka jest
    suspend fun isQueuePositionTaken(position: Int): Boolean {
        // Pozycja -1 oznacza wyłączenie z wybierania - może być użyta przez wielu graczy
        if (position == -1) return false

        // Pobierz wszystkich graczy i sprawdź czy ktoś ma już tę pozycję
        val allPlayers = dao.getAllQueue()
        return allPlayers.any { player ->
            player.queuePosition == position
        }
    }


    suspend fun getPlayerById(playerId: Int): Player? = dao.getPlayerById(playerId)

    suspend fun getAllQueue(): List<Player> = dao.getAllQueue()

}
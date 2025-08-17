package com.example.apprafal.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.apprafal.data.*
import android.util.Log


class GameSessionViewModel(
    private val sessionRepo: GameSessionRepo,
    private val playerRepo: PlayerRepo,
    private val gamePickRepo: GamePickRepo
) : ViewModel() {

    suspend fun createSessionAndReturnId(date: Long, selectedPlayers: List<Player>): String {

        return sessionRepo.createSessionWithParticipants(date, selectedPlayers)
    }

    suspend fun getAllSessions(): List<GameSession> {
        return sessionRepo.getAllSessions()
    }

    suspend fun getActiveQueue(sessionId: String): List<GameSessionParticipant> {

        val queue = sessionRepo.getActiveQueue(sessionId)
        return queue
    }

    suspend fun getFirstAvailablePicker(sessionId: String): GameSessionParticipant? {

        val picker = sessionRepo.getFirstAvailablePicker(sessionId)

        return picker
    }

    suspend fun skipPlayer(sessionId: String, playerId: Int) {

        sessionRepo.skipParticipant(sessionId, playerId)

    }

    suspend fun getParticipantsWithNames(sessionId: String): List<ParticipantWithName> {
        val participants = sessionRepo.getParticipantsWithNames(sessionId)
        return participants
    }

    suspend fun getSessionWithDetails(sessionId: String): SessionDetail? {
        val session = sessionRepo.getSessionById(sessionId) ?: return null
        val participants = sessionRepo.getParticipantsWithNames(sessionId)
        val picks = gamePickRepo.getAllPicksForSession(sessionId)

        return SessionDetail(
            session = session,
            participants = participants,
            picks = picks
        )
    }

    suspend fun changeQueue(playerId: Int) {
        //pobieram obecna kolejke gracza
        val position = playerRepo.getQueuePosition(playerId)

        val playersInQueue = playerRepo.getQueueSize()

        // Ustaw gracza na końcu kolejki
        playerRepo.updatePlayerQueuePosition(playerId, position + playersInQueue)

        // Pobierz wszystkich graczy posortowanych według kolejki
        val allPlayers = playerRepo.getAllQueue()

        // Znajdź min pozycję w kolejce
        val minPosition = allPlayers.mapNotNull { it.queuePosition }.min()

        if (minPosition > 20) {

            val modulo = 4 * playersInQueue

            allPlayers.forEach { player ->
                val currentPosition = player.queuePosition ?: 0
                val newPosition = currentPosition % modulo
                playerRepo.updatePlayerQueuePosition(player.id, newPosition)
            }
        }
    }

    suspend fun makeGamePick(sessionId: String, playerId: Int, gameName: String): Boolean {

            gamePickRepo.insertWithOrder(sessionId, playerId, gameName)
            return true
    }

    suspend fun undoLastPick(sessionId: String): Boolean {
        return gamePickRepo.undoLastPick(sessionId, sessionRepo)
    }
}


class GameSessionViewModelFactory(
    private val sessionRepo: GameSessionRepo,
    private val playerRepo: PlayerRepo,
    private val gamePickRepo: GamePickRepo
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GameSessionViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GameSessionViewModel(sessionRepo, playerRepo, gamePickRepo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

data class SessionDetail(
    val session: GameSession,
    val participants: List<ParticipantWithName>,
    val picks: List<GamePick>
)

data class GamePickDisplayItem(
    val playerName: String,
    val gameName: String,
    val timestamp: Long,
    val pickOrder: Int,
    val canUndo: Boolean = false
)
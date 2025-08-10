package com.example.apprafal.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.apprafal.data.*
import kotlin.collections.find



class SessionDetailViewModel(
    private val sessionRepo: GameSessionRepo,
    private val gamePickRepo: GamePickRepo
) : ViewModel() {

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

    suspend fun makeGamePick(sessionId: String, playerId: Int, gameName: String): Boolean {
        return try {
            // 1. Zapisz pick
            gamePickRepo.insertWithOrder(sessionId, playerId, gameName)

            // 2. Znajdź uczestnika i przesuń go
            val participant = sessionRepo.getParticipantsForSession(sessionId)
                .find { it.playerId == playerId }

            if (participant != null) {
                sessionRepo.moveParticipantToEndOfQueue(sessionId, participant)
            }

            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun undoLastPick(sessionId: String): Boolean {
        return gamePickRepo.undoLastPick(sessionId, sessionRepo)
    }
}

class SessionDetailViewModelFactory(
    private val sessionRepo: GameSessionRepo,
    private val gamePickRepo: GamePickRepo
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SessionDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SessionDetailViewModel(sessionRepo, gamePickRepo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

// Data classes dla UI
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
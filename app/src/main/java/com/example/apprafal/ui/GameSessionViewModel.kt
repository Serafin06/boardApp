package com.example.apprafal.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.apprafal.data.*

class GameSessionViewModel(
    private val sessionRepo: GameSessionRepo,
    private val queueRepo: GameQueueRepo
) : ViewModel() {

    suspend fun createSessionAndReturnId(date: Long, selectedPlayers: List<Player>): String {
        val session = GameSession(date = date)

        val participants = selectedPlayers.mapIndexed { index, player ->
            GameSessionParticipant(
                sessionId = session.id,
                playerId = player.id.toString(),
                isPresent = true,
                queuePosition = index
            )
        }

        val queueEntries = selectedPlayers.mapIndexed { index, player ->
            GameQueueEntry(
                sessionId = session.id,
                playerId = player.id,
                position = index
            )
        }

        sessionRepo.createSessionWithParticipants(session, participants)
        queueEntries.forEach { queueRepo.insert(it) } // <-- TO DODAJE KOLEJKĘ

        return session.id
    }
}

class GameSessionViewModelFactory(
    private val sessionRepo: GameSessionRepo,
    private val queueRepo: GameQueueRepo
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return GameSessionViewModel(sessionRepo, queueRepo) as T
    }
}


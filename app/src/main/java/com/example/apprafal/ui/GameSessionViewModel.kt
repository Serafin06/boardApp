package com.example.apprafal.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.apprafal.data.*
import android.util.Log

class GameSessionViewModel(
    private val sessionRepo: GameSessionRepo,
    private val playerRepo: PlayerRepo
) : ViewModel() {

    suspend fun createSession(date: Long): String {
        return sessionRepo.createSession(date)
    }

    suspend fun createSessionAndReturnId(date: Long, selectedPlayers: List<Player>): String {
        return sessionRepo.createSessionWithParticipants(date, selectedPlayers)
    }

    suspend fun getAllSessions(): List<GameSession> = sessionRepo.getAllSessions()

    // METODY KOLEJKI - przeniesione z GameQueueViewModel
    suspend fun getActiveQueue(sessionId: String): List<GameSessionParticipant> {
        return sessionRepo.getActiveQueue(sessionId)
    }

    suspend fun getFirstAvailablePicker(sessionId: String): GameSessionParticipant? {
        return sessionRepo.getFirstAvailablePicker(sessionId)
    }

    suspend fun movePlayerToEndOfQueue(sessionId: String, participant: GameSessionParticipant) {
        sessionRepo.moveParticipantToEndOfQueue(sessionId, participant)
    }

    suspend fun skipPlayer(sessionId: String, playerId: Int) {
        sessionRepo.skipParticipant(sessionId, playerId)
    }

    // Dla UI
    suspend fun getParticipantsWithNames(sessionId: String): List<ParticipantWithName> {
        return sessionRepo.getParticipantsWithNames(sessionId)
    }

    suspend fun getCurrentPicker(sessionId: String): GameSessionParticipant? {
        return sessionRepo.getFirstAvailablePicker(sessionId)
    }
}

// Zaktualizowany Factory
class GameSessionViewModelFactory(
    private val sessionRepo: GameSessionRepo,
    private val playerRepo: PlayerRepo
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GameSessionViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GameSessionViewModel(sessionRepo, playerRepo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
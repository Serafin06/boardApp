package com.example.apprafal.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.apprafal.data.*
import android.util.Log


class GameSessionViewModel(
    private val sessionRepo: GameSessionRepo,
    private val playerRepo: PlayerRepo
) : ViewModel() {

    suspend fun createSessionAndReturnId(date: Long, selectedPlayers: List<Player>): String {
        Log.d("SESSION_VM", "🎯 Tworzenie sesji z ${selectedPlayers.size} graczami")
        Log.d("SESSION_VM", "📋 Gracze: ${selectedPlayers.map { "${it.name} (canChoose: ${it.canChooseGame}, queuePos: ${it.queuePosition})" }}")

        return sessionRepo.createSessionWithParticipants(date, selectedPlayers)
    }

    suspend fun getAllSessions(): List<GameSession> {
        Log.d("SESSION_VM", "📋 Pobieranie wszystkich sesji")
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

    /**
     * Przesuwa gracza na koniec kolejki po tym jak wybierze grę
     * @param sessionId - ID sesji
     * @param participant - gracz do przesunięcia
     */
    suspend fun movePlayerToEndOfQueue(sessionId: String, participant: GameSessionParticipant) {
        Log.d("SESSION_VM", "🔄 Przesuwanie gracza ${participant.playerId} na koniec kolejki")
        sessionRepo.moveParticipantToEndOfQueue(sessionId, participant)
        Log.d("SESSION_VM", "✅ Gracz przesunięty na koniec kolejki")
    }

    /**
     * Pomija gracza w kolejce (np. gdy jest nieobecny)
     * @param sessionId - ID sesji
     * @param playerId - ID gracza do pominięcia
     */
    suspend fun skipPlayer(sessionId: String, playerId: Int) {
        Log.d("SESSION_VM", "⏭️ Pomijanie gracza $playerId w sesji $sessionId")
        sessionRepo.skipParticipant(sessionId, playerId)
        Log.d("SESSION_VM", "✅ Gracz pominięty")
    }

    // ========== METODY DLA UI ==========
    // Te metody dostarczają dane w formacie przyjaznym dla interfejsu użytkownika

    /**
     * Pobiera uczestników sesji wraz z ich nazwami (nie tylko ID)
     * Używane gdy potrzebujemy wyświetlić imiona graczy, nie tylko ID
     * @param sessionId - ID sesji
     * @return Lista uczestników z wypełnionymi nazwami
     */
    suspend fun getParticipantsWithNames(sessionId: String): List<ParticipantWithName> {
        Log.d("SESSION_VM", "📋 Pobieranie uczestników z nazwami dla sesji: $sessionId")
        val participants = sessionRepo.getParticipantsWithNames(sessionId)
        Log.d("SESSION_VM", "✅ Pobrano ${participants.size} uczestników z nazwami")
        return participants
    }

    /**
     * Alias dla getFirstAvailablePicker - zwraca aktualnego gracza który wybiera
     * @param sessionId - ID sesji
     * @return Aktualny gracz wybierający lub null
     */
    suspend fun getCurrentPicker(sessionId: String): GameSessionParticipant? {
        Log.d("SESSION_VM", "🎯 Pobieranie aktualnego gracza wybierającego")
        return sessionRepo.getFirstAvailablePicker(sessionId)
    }
}

/**
 * Factory do tworzenia GameSessionViewModel z wymaganymi zależnościami
 * Android wymaga Factory gdy ViewModel ma parametry konstruktora
 */
class GameSessionViewModelFactory(
    private val sessionRepo: GameSessionRepo,
    private val playerRepo: PlayerRepo
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GameSessionViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GameSessionViewModel(sessionRepo, playerRepo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
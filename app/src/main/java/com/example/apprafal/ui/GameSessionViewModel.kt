package com.example.apprafal.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.apprafal.data.*
import android.util.Log

/**
 * ViewModel odpowiedzialny za zarządzanie sesjami gier i kolejkami graczy
 * Pośredniczy między UI a warstwą danych (Repository)
 */
class GameSessionViewModel(
    private val sessionRepo: GameSessionRepo,
    private val playerRepo: PlayerRepo
) : ViewModel() {

    /**
     * Tworzy prostą sesję z samą datą (bez uczestników)
     * @param date - timestamp kiedy sesja została utworzona
     * @return ID utworzonej sesji
     */
    suspend fun createSession(date: Long): String {
        Log.d("SESSION_VM", "🎯 Tworzenie prostej sesji z datą: $date")
        return sessionRepo.createSession(date)
    }

    /**
     * Tworzy sesję wraz z uczestnikami - główna metoda używana w CreateSessionActivity
     * @param date - timestamp sesji
     * @param selectedPlayers - lista graczy wybranych do sesji
     * @return ID utworzonej sesji
     */
    suspend fun createSessionAndReturnId(date: Long, selectedPlayers: List<Player>): String {
        Log.d("SESSION_VM", "🎯 Tworzenie sesji z ${selectedPlayers.size} graczami")
        Log.d("SESSION_VM", "📋 Gracze: ${selectedPlayers.map { "${it.name} (canChoose: ${it.canChooseGame}, queuePos: ${it.queuePosition})" }}")

        return sessionRepo.createSessionWithParticipants(date, selectedPlayers)
    }

    /**
     * Pobiera wszystkie sesje z bazy danych
     */
    suspend fun getAllSessions(): List<GameSession> {
        Log.d("SESSION_VM", "📋 Pobieranie wszystkich sesji")
        return sessionRepo.getAllSessions()
    }

    // ========== METODY KOLEJKI ==========
    // Te metody zarządzają kolejką graczy w sesji

    /**
     * Pobiera aktualną kolejkę graczy dla danej sesji
     * Zwraca tylko graczy którzy mogą wybierać (canPickInSession = true)
     * @param sessionId - ID sesji
     * @return Lista uczestników posortowana po pozycji w kolejce (najniższa waga pierwsza)
     */
    suspend fun getActiveQueue(sessionId: String): List<GameSessionParticipant> {
        Log.d("SESSION_VM", "🎯 Pobieranie aktywnej kolejki dla sesji: $sessionId")
        val queue = sessionRepo.getActiveQueue(sessionId)
        Log.d("SESSION_VM", "📋 Znaleziono ${queue.size} aktywnych graczy w kolejce")
        return queue
    }

    /**
     * KLUCZOWA METODA: Znajduje gracza który powinien teraz wybierać grę
     * Wybiera gracza z NAJNIŻSZĄ wagą (queuePosition) spośród dostępnych
     * @param sessionId - ID sesji
     * @return Gracz z najniższą wagą lub null jeśli nikt nie może wybierać
     */
    suspend fun getFirstAvailablePicker(sessionId: String): GameSessionParticipant? {
        Log.d("SESSION_VM", "🎯 Szukam pierwszego dostępnego gracza do wybierania w sesji: $sessionId")
        val picker = sessionRepo.getFirstAvailablePicker(sessionId)

        if (picker != null) {
            Log.d("SESSION_VM", "✅ Znaleziono gracza: ID=${picker.playerId}, pozycja=${picker.queuePosition}")
        } else {
            Log.d("SESSION_VM", "❌ Brak dostępnych graczy do wybierania!")
        }

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
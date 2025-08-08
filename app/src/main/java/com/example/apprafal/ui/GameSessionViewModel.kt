package com.example.apprafal.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.apprafal.data.*
import android.util.Log

class GameSessionViewModel(
    private val sessionRepo: GameSessionRepo,
    private val queueRepo: GameQueueRepo,
    private val playerRepo: PlayerRepo // Dodajemy repo graczy
) : ViewModel() {

    suspend fun createSessionAndReturnId(date: Long, selectedPlayers: List<Player>): String {
        Log.d("SESSION_CREATE", "🏗️ Tworzenie sesji z ${selectedPlayers.size} graczami")

        val session = GameSession(date = date)

        // 1. Uczestnicy sesji (w kolejności dodania - dla uczestnictwa)
        val participants = selectedPlayers.mapIndexed { index, player ->
            GameSessionParticipant(
                sessionId = session.id,
                playerId = player.id.toString(),
                isPresent = true,
                queuePosition = index
            )
        }

        // 2. ⚠️ KLUCZOWA ZMIANA: Kolejka gry według WAG, nie kolejności!
        val eligiblePlayers = selectedPlayers.filter { it.canChooseGame }
        Log.d("SESSION_CREATE", "🎯 Uprawnieni gracze: ${eligiblePlayers.map { "${it.name}(waga:${it.queuePosition})" }}")

        // Sortuj według wag (niższa waga = wyższa pozycja w kolejce)
        val sortedByWeight = eligiblePlayers.sortedBy { it.queuePosition ?: Int.MAX_VALUE }
        Log.d("SESSION_CREATE", "📊 Posortowani według wag: ${sortedByWeight.map { "${it.name}(${it.queuePosition})" }}")

        val queueEntries = sortedByWeight.mapIndexed { index, player ->
            Log.d("SESSION_CREATE", "📝 Pozycja $index: ${player.name} (ID:${player.id}, waga:${player.queuePosition})")
            GameQueueEntry(
                sessionId = session.id,
                playerId = player.id,
                position = index, // Pozycja według wag!
                isSkipped = false
            )
        }

        Log.d("SESSION_CREATE", "✅ Utworzono ${queueEntries.size} wpisów kolejki")

        // Zapisz do bazy
        sessionRepo.createSessionWithParticipants(session, participants)
        queueEntries.forEach { queueRepo.insert(it) }

        return session.id
    }
}

class GameSessionViewModelFactory(
    private val sessionRepo: GameSessionRepo,
    private val queueRepo: GameQueueRepo,
    private val playerRepo: PlayerRepo // Dodajemy PlayerRepo
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return GameSessionViewModel(sessionRepo, queueRepo, playerRepo) as T
    }
}
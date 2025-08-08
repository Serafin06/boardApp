package com.example.apprafal.data

import androidx.lifecycle.LiveData
import android.util.Log

class GamePickRepo(private val dao: GamePickDao) {

    fun getPicksForSession(sessionId: String): LiveData<List<GamePick>> =
        dao.getPicksForSession(sessionId)

    suspend fun insert(gamePick: GamePick) = dao.insert(gamePick)

    // Nowa funkcja: usuń ostatni wybór i przywróć gracza na początek kolejki
    suspend fun undoLastPick(sessionId: String, queueRepo: GameQueueRepo): Boolean {
        Log.d("UNDO_DEBUG", "🔄 Rozpoczęcie cofania ostatniego wyboru...")

        try {
            // 1. Pobierz ostatni wybór
            val lastPick = dao.getLastPick(sessionId)
            if (lastPick == null) {
                Log.w("UNDO_DEBUG", "⚠️ Brak wyborów do cofnięcia")
                return false
            }

            Log.d("UNDO_DEBUG", "🎯 Ostatni wybór: ${lastPick.playerId} -> ${lastPick.gameName}")

            // 2. Usuń wybór z bazy
            dao.delete(lastPick)
            Log.d("UNDO_DEBUG", "🗑️ Usunięto wybór z historii")

            // 3. Znajdź gracza w kolejce i przenieś na początek
            val playerId = lastPick.playerId.toIntOrNull() ?: return false
            val queue = queueRepo.getActiveQueue(sessionId)
            val playerEntry = queue.find { it.playerId == playerId }

            if (playerEntry != null) {
                // Znajdź minimalną pozycję i ustaw gracza przed nią
                val minPosition = queue.minOfOrNull { it.position } ?: 1
                val newEntry = playerEntry.copy(position = minPosition - 1)

                queueRepo.updateEntry(newEntry)
                Log.d("UNDO_DEBUG", "🔄 Gracz ${playerId} przeniesiony na pozycję ${newEntry.position}")
            }

            Log.d("UNDO_DEBUG", "✅ Cofanie zakończone pomyślnie")
            return true

        } catch (e: Exception) {
            Log.e("UNDO_DEBUG", "❌ Błąd podczas cofania: ${e.message}", e)
            return false
        }
    }

    suspend fun getLastPick(sessionId: String): GamePick? = dao.getLastPick(sessionId)
}
package com.example.apprafal.data

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import android.util.Log

class GameQueueRepo(private val dao: GameQueueDao) {

    suspend fun getQueue(sessionId: String): List<GameQueueEntry> = dao.getQueue(sessionId)

    // Nowa metoda - zwraca tylko aktywnych graczy (nie pominięte)
    suspend fun getActiveQueue(sessionId: String): List<GameQueueEntry> =
        dao.getActiveQueue(sessionId)

    suspend fun skipPlayer(sessionId: String, playerId: Int) = dao.skipPlayer(sessionId, playerId)

    suspend fun getFirstInQueue(sessionId: String): GameQueueEntry? =
        dao.getFirstInQueue(sessionId)

    suspend fun moveToEnd(sessionId: String, entry: GameQueueEntry) {
        Log.d("QUEUE_DEBUG", "🔄 moveToEnd - wejście: entry=$entry")

        val maxPos = dao.getMaxPosition(sessionId) ?: 0
        Log.d("QUEUE_DEBUG", "📊 Maksymalna pozycja: $maxPos")

        // ⚠️ KRYTYCZNA POPRAWKA: było =+ teraz jest =
        val newPosition = maxPos + 1
        val updated = entry.copy(position = newPosition)

        Log.d("QUEUE_DEBUG", "📝 Nowa pozycja: $newPosition")
        Log.d("QUEUE_DEBUG", "📄 Zaktualizowany wpis: $updated")

        dao.updateEntry(updated)
        Log.d("QUEUE_DEBUG", "✅ moveToEnd zakończone")
    }

    suspend fun insert(entry: GameQueueEntry) {
        dao.insert(entry)
    }

    // Dodaj metodę do aktualizacji wpisu
    suspend fun updateEntry(entry: GameQueueEntry) {
        dao.updateEntry(entry)
        Log.d("QUEUE_DEBUG", "📝 Zaktualizowano wpis: $entry")
    }
}
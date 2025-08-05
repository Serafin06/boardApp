package com.example.apprafal.data

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class GameQueueRepo(private val dao: GameQueueDao) {


    suspend fun getQueue(sessionId: String): List<GameQueueEntry> = dao.getQueue(sessionId)

    suspend fun skipPlayer(sessionId: String, playerId: Int) = dao.skipPlayer(sessionId, playerId)

    suspend fun getFirstInQueue(sessionId: String): GameQueueEntry? =
        dao.getFirstInQueue(sessionId)

    suspend fun moveToEnd(sessionId: String, entry: GameQueueEntry) {
        val maxPos = dao.getMaxPosition(sessionId) ?: 0
        val updated = entry.copy(position = maxPos + 1)
        dao.updateEntry(updated)
    }

    suspend fun insert(entry: GameQueueEntry) {
        dao.insert(entry)
    }

}

class GameQueueViewModel(private val repo: GameQueueRepo) : ViewModel() {
    val queue = MutableLiveData<List<GameQueueEntry>>()

    fun loadQueue(sessionId: String) {
        viewModelScope.launch {
            queue.postValue(repo.getQueue(sessionId))
        }
    }

    fun skip(sessionId: String, playerId: Int) {
        viewModelScope.launch {
            repo.skipPlayer(sessionId, playerId)
            loadQueue(sessionId)
        }
    }
}
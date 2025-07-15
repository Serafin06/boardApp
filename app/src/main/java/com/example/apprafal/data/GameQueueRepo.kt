package com.example.apprafal.data

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class GameQueueRepo(private val dao: GameQueueDao) {

    suspend fun createQueue(sessionId: String, playerIds: List<Int>) {
        val entries = playerIds.mapIndexed { index, playerId ->
            GameQueueEntry(
                sessionId = sessionId,
                playerId = playerId,
                position = index
            )
        }
        dao.clearQueue(sessionId)
        dao.insertAll(entries)
    }

    suspend fun getQueue(sessionId: String): List<GameQueueEntry> = dao.getQueue(sessionId)

    suspend fun skipPlayer(sessionId: String, playerId: Int) = dao.skipPlayer(sessionId, playerId)
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
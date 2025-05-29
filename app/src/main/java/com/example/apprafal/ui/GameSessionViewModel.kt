package com.example.apprafal.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import com.example.apprafal.data.*
import java.util.LinkedList

class GameSessionViewModel(private val repository: GameSessionRepo) : ViewModel() {

    private val _sessions = MutableLiveData<List<GameSession>>()
    val sessions: LiveData<List<GameSession>> = _sessions

    fun createSession(date: Long, selectedPlayers: List<Player>) {
        viewModelScope.launch {
            val session = GameSession(date = date)
            val participants = selectedPlayers.mapIndexed { index, player ->
                GameSessionParticipant(
                    sessionId = session.id,
                    playerId = player.id,
                    isPresent = true,
                    queuePosition = index // na razie kolejność wg kliknięcia
                )
            }
            repository.createSessionWithParticipants(session, participants)
            loadSessions()
        }
    }

    fun loadSessions() {
        viewModelScope.launch {
            _sessions.postValue(repository.getAllSessions())
        }
    }
    fun generatePlayerQueue(selectedPlayers: List<Player>): List<Player>{
        val queue = LinkedList<Player>()
        queue.addAll(selectedPlayers.sortedBy{ it.name })
        return queue
    }
}

class GameSessionViewModelFactory(
    private val repository: GameSessionRepo
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GameSessionViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GameSessionViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}


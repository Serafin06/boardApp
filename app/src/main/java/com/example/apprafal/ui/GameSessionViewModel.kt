package com.example.apprafal.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import com.example.apprafal.data.*

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
}

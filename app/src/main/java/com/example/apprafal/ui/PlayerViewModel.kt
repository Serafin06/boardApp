package com.example.apprafal.ui

import androidx.lifecycle.*
import com.example.apprafal.data.Player
import com.example.apprafal.data.PlayerRepo
import kotlinx.coroutines.launch
import java.util.*

class PlayerViewModel(private val repository: PlayerRepo) : ViewModel() {
    val players: LiveData<List<Player>> = repository.allPlayers

    fun addPlayer(name: String) {
        val newPlayer = Player(UUID.randomUUID().toString(), name)
        viewModelScope.launch {
            repository.insert(newPlayer)
        }
    }
}

class PlayerViewModelFactory(private val repository: PlayerRepo) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return PlayerViewModel(repository) as T
    }
}
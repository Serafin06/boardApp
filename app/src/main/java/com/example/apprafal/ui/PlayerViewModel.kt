package com.example.apprafal.ui

import androidx.lifecycle.*
import com.example.apprafal.data.Player
import com.example.apprafal.data.PlayerRepo
import kotlinx.coroutines.launch
import java.util.*

class PlayerViewModel(private val repository: PlayerRepo) : ViewModel() {

    val allPlayers: LiveData<List<Player>> = repository.getAllPlayers()

    fun addPlayer(name: String, queuePosition: Int, canChooseGame: Boolean) {
        viewModelScope.launch {
            val player = Player(name = name, queuePosition = queuePosition, canChooseGame = canChooseGame)
            repository.insert(player)
        }
    }

}

class PlayerViewModelFactory(private val repository: PlayerRepo) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return PlayerViewModel(repository) as T
    }
}


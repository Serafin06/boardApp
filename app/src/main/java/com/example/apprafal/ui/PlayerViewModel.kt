package com.example.apprafal.ui

import androidx.lifecycle.*
import com.example.apprafal.data.Player
import com.example.apprafal.data.PlayerRepo
import kotlinx.coroutines.launch
import java.util.*

class PlayerViewModel(private val playerRepo: PlayerRepo) : ViewModel() {

    val allPlayers = playerRepo.getAllPlayers()

    fun insert(player: Player) = viewModelScope.launch {
        playerRepo.insert(player)
    }
    fun addPlayer(name: String, queuePosition: Int, canChooseGame: Boolean) = viewModelScope.launch {
        val player = Player(
            name = name,
            canChooseGame = canChooseGame,
            queuePosition = if (queuePosition == -1) null else queuePosition
        )
        playerRepo.insert(player)
    }
}

class PlayerViewModelFactory(private val playerRepo: PlayerRepo) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PlayerViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PlayerViewModel(playerRepo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}




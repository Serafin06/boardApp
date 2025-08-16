package com.example.apprafal.ui

import androidx.lifecycle.*
import com.example.apprafal.data.Player
import com.example.apprafal.data.PlayerRepo
import kotlinx.coroutines.launch
import java.util.*

class PlayerViewModel(private val playerRepo: PlayerRepo) : ViewModel() {

    private val _toastMessage = MutableLiveData<String>()
    val toastMessage: LiveData<String> = _toastMessage

    private val _playerAddSuccess = MutableLiveData<Boolean>()
    val playerAddSuccess: LiveData<Boolean> = _playerAddSuccess

    val allPlayers = playerRepo.getAllPlayers()

    fun insert(player: Player) = viewModelScope.launch {
        playerRepo.insert(player)
    }
    fun addPlayer(name: String, queuePosition: Int, canChooseGame: Boolean) = viewModelScope.launch {
        if (playerRepo.isQueuePositionTaken(queuePosition)) {
            _toastMessage.postValue("Pozycja $queuePosition jest już zajęta. Wybierz inną pozycję.")
            _playerAddSuccess.postValue(false)
            return@launch
        }

        val player = Player(
            name = name,
            canChooseGame = canChooseGame,
            queuePosition = queuePosition
        )
        playerRepo.insert(player)
        _playerAddSuccess.postValue(true)
    }
    val gameQueue: LiveData<List<Player>> = playerRepo.getQueue()

    fun clearToastMessage() {
        _toastMessage.value = ""
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




package com.example.apprafal.ui

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.apprafal.data.GamePick
import com.example.apprafal.data.GamePickRepo
import kotlinx.coroutines.launch

class GamePickViewModel(private val repo: GamePickRepo) : ViewModel() {
    val allPicks = MutableLiveData<List<GamePick>>()

    init {
        viewModelScope.launch {
            allPicks.postValue(repo.getPicksForSession("yourSessionIdHere").value ?: emptyList())
        }
    }
}

class GamePickViewModelFactory(private val repo: GamePickRepo) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return GamePickViewModel(repo) as T
    }
}
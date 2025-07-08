package com.example.apprafal.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query


@Dao
interface GamePickDao {
    @Insert
    suspend fun insert(pick: GamePick)

    @Query("SELECT * FROM GamePick WHERE sessionId = :sessionId")
    fun getPicksForSession(sessionId: String): LiveData<List<GamePick>>
}
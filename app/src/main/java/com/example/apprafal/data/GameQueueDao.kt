package com.example.apprafal.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface GameQueueDao {
    @Insert
    suspend fun insertAll(entries: List<GameQueueEntry>)

    @Query("SELECT * FROM GameQueueEntry WHERE sessionId = :sessionId ORDER BY position ASC")
    suspend fun getQueue(sessionId: String): List<GameQueueEntry>

    @Query("UPDATE GameQueueEntry SET isSkipped = 1 WHERE sessionId = :sessionId AND playerId = :playerId")
    suspend fun skipPlayer(sessionId: String, playerId: Int)

    @Query("DELETE FROM GameQueueEntry WHERE sessionId = :sessionId")
    suspend fun clearQueue(sessionId: String)
}
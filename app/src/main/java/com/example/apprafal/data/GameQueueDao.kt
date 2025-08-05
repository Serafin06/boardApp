package com.example.apprafal.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface GameQueueDao {
    @Insert
    suspend fun insertAll(entries: List<GameQueueEntry>)

    @Insert
    suspend fun insert(entry: GameQueueEntry)

    @Query("SELECT * FROM GameQueueEntry WHERE sessionId = :sessionId ORDER BY position ASC")
    suspend fun getQueue(sessionId: String): List<GameQueueEntry>

    @Query("UPDATE GameQueueEntry SET isSkipped = 1 WHERE sessionId = :sessionId AND playerId = :playerId")
    suspend fun skipPlayer(sessionId: String, playerId: Int)

    @Query("DELETE FROM GameQueueEntry WHERE sessionId = :sessionId")
    suspend fun clearQueue(sessionId: String)

    @Query("SELECT * FROM GameQueueEntry WHERE sessionId = :sessionId AND isSkipped = 0 ORDER BY position ASC LIMIT 1")
    suspend fun getFirstInQueue(sessionId: String): GameQueueEntry?

    @Query("SELECT MAX(position) FROM GameQueueEntry WHERE sessionId = :sessionId")
    suspend fun getMaxPosition(sessionId: String): Int?

    @Update
    suspend fun updateEntry(entry: GameQueueEntry)
}
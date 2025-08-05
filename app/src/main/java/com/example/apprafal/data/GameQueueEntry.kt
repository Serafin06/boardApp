package com.example.apprafal.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.android.identity.util.UUID

@Entity
data class GameQueueEntry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val sessionId: String,
    val playerId: Int,
    val position: Int,
    val isSkipped: Boolean = false
)

@Entity
data class GamePick(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val sessionId: String,
    val playerId: String,
    val gameName: String,
    val timestamp: Long = System.currentTimeMillis()
)

class GamePickRepo(private val dao: GamePickDao) {
    suspend fun insert(pick: GamePick) = dao.insert(pick)
    fun getPicksForSession(sessionId: String) = dao.getPicksForSession(sessionId)


}
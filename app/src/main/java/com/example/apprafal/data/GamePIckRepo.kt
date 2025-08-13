package com.example.apprafal.data

import androidx.lifecycle.LiveData
import android.util.Log

class GamePickRepo(private val gamePickDao: GamePickDao) {

    fun getPicksForSession(sessionId: String): LiveData<List<GamePick>> {
        return gamePickDao.getPicksForSession(sessionId)
    }

    suspend fun insert(gamePick: GamePick) {
        gamePickDao.insert(gamePick)
    }

    suspend fun insertWithOrder(sessionId: String, playerId: Int, gameName: String): GamePick {
        val pickOrder = gamePickDao.getNextPickOrder(sessionId)
        val gamePick = GamePick(
            sessionId = sessionId,
            playerId = playerId, // już Int!
            gameName = gameName,
            pickOrder = pickOrder
        )
        gamePickDao.insert(gamePick)
        return gamePick
    }

    suspend fun getLastPick(sessionId: String): GamePick? {
        return gamePickDao.getLastPick(sessionId)
    }

    suspend fun getAllPicksForSession(sessionId: String): List<GamePick> {
        return gamePickDao.getAllPicksForSession(sessionId)
    }

    suspend fun getPickCount(sessionId: String): Int {
        return gamePickDao.getPickCount(sessionId)
    }

    // ZMIENIONA logika cofania - teraz współpracuje z GameSessionRepo
    suspend fun undoLastPick(
        sessionId: String,
        sessionRepo: GameSessionRepo
    ): Boolean {
        return try {
            val lastPick = getLastPick(sessionId) ?: return false

            // 1. Usuń pick
            gamePickDao.delete(lastPick)


            // 4. Ustaw go jako current picker
            sessionRepo.updateCurrentPicker(sessionId, lastPick.playerId)

            true
        } catch (e: Exception) {
            false
        }
    }
}


// Extension dla łatwiejszego dostępu w GamePickRepo
val GameSessionRepo.participantDao: GameSessionParticipantDao
    get() = this.participantDao // To będzie wymagało refactoringu konstruktora}
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
            playerId = playerId,
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

    // ULEPSZONA logika cofania - wykorzystuje GameSessionParticipant do przywracania kolejki
    suspend fun undoLastPick(
        sessionId: String,
        sessionRepo: GameSessionRepo,
        playerRepo: PlayerRepo
    ): Boolean {
        return try {
            val lastPick = getLastPick(sessionId) ?: return false

            Log.d("GAMEPICK_REPO", "🔄 Cofanie wyboru: ${lastPick.gameName} gracza ${lastPick.playerId}")

            // 1. PRZYWRÓĆ KOLEJKĘ Z GameSessionParticipant (stan z początku sesji)
            Log.d("GAMEPICK_REPO", "🔄 Przywracanie kolejki z GameSessionParticipant...")

            val participants = sessionRepo.getAllParticipants(sessionId)
            participants.forEach { participant ->
                Log.d("GAMEPICK_REPO", "↻ Przywracanie gracza ${participant.playerId} na pozycję ${participant.queuePosition}")

                // Przywróć pozycję z GameSessionParticipant do Player.queuePosition
                playerRepo.updatePlayerQueuePosition(participant.playerId, participant.queuePosition)
            }

            // 2. Usuń ostatni pick
            gamePickDao.delete(lastPick)

            // 3. Ustaw gracza jako current picker
            sessionRepo.updateCurrentPicker(sessionId, lastPick.playerId)

            Log.d("GAMEPICK_REPO", "✅ Pomyślnie cofnięto wybór i przywrócono kolejkę")
            true

        } catch (e: Exception) {
            Log.e("GAMEPICK_REPO", "❌ Błąd podczas cofania: ${e.message}", e)
            false
        }
    }
}


// Extension dla łatwiejszego dostępu w GamePickRepo
val GameSessionRepo.participantDao: GameSessionParticipantDao
    get() = this.participantDao // To będzie wymagało refactoringu konstruktora}
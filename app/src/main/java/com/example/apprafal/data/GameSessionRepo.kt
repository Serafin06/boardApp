package com.example.apprafal.data

import androidx.lifecycle.LiveData


class GameSessionRepo(
    private val sessionDao: GameSessionDao,
    private val participantDao: GameSessionParticipantDao
) {

    suspend fun createSession(date: Long): String {
        val session = GameSession(date = date)
        sessionDao.insertSession(session)
        return session.id
    }

    suspend fun createSessionWithParticipants(
        date: Long,
        selectedPlayers: List<Player>
    ): String {
        val session = GameSession(date = date)

        // Tworzenie uczestników z kolejką na podstawie Player.canChooseGame
        val participants = selectedPlayers.mapIndexed { index, player ->
            GameSessionParticipant(
                sessionId = session.id,
                playerId = player.id,
                isPresent = true,
                canPickInSession = player.canChooseGame, // używamy wagi z Player
                queuePosition = index + 1,
                isSkipped = false,
                hasPickedInSession = false
            )
        }

        sessionDao.createSessionWithParticipants(session, participants)
        return session.id
    }

    suspend fun getAllSessions(): List<GameSession> = sessionDao.getAllSessions()

    suspend fun getSessionById(sessionId: String): GameSession? =
        sessionDao.getSessionById(sessionId)

    suspend fun updateCurrentPicker(sessionId: String, playerId: Int?) {
        sessionDao.updateCurrentPicker(sessionId, playerId)
    }

    // METODY KOLEJKI - przeniesione z GameQueueRepo
    suspend fun getActiveQueue(sessionId: String): List<GameSessionParticipant> {
        return participantDao.getActiveQueue(sessionId)
    }

    suspend fun getFirstAvailablePicker(sessionId: String): GameSessionParticipant? {
        return participantDao.getFirstAvailablePicker(sessionId)
    }

    suspend fun getParticipantsForSession(sessionId: String): List<GameSessionParticipant> {
        return participantDao.getParticipantsForSession(sessionId)
    }

    suspend fun moveParticipantToEndOfQueue(sessionId: String, participant: GameSessionParticipant) {
        // Oznacz że już wybierał
        participantDao.markAsHasPicked(
            sessionId = sessionId,
            playerId = participant.playerId,
            hasPicked = true,
            timestamp = System.currentTimeMillis()
        )

        // Przesuń na koniec kolejki
        participantDao.moveToEndOfQueue(sessionId, participant.id)

        // Ustaw następnego pickera
        val nextPicker = participantDao.getFirstAvailablePicker(sessionId)
        updateCurrentPicker(sessionId, nextPicker?.playerId)
    }

    suspend fun skipParticipant(sessionId: String, playerId: Int) {
        participantDao.setSkipped(sessionId, playerId, true)

        // Ustaw następnego pickera
        val nextPicker = participantDao.getFirstAvailablePicker(sessionId)
        updateCurrentPicker(sessionId, nextPicker?.playerId)
    }

    suspend fun resetParticipantSkip(sessionId: String, playerId: Int) {
        participantDao.setSkipped(sessionId, playerId, false)
    }

    // Dla UI - z nazwami graczy
    suspend fun getParticipantsWithNames(sessionId: String): List<ParticipantWithName> {
        return participantDao.getParticipantsWithNames(sessionId)
    }
}
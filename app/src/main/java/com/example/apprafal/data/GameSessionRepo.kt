package com.example.apprafal.data




class GameSessionRepo(private val dao: GameSessionDao) {

    suspend fun createSessionWithParticipants(session: GameSession, participants: List<GameSessionParticipant>) {
        dao.createSessionWithParticipants(session, participants)
    }

    suspend fun getAllSessions() = dao.getAllSessions()

    suspend fun getParticipants(sessionId: String) = dao.getParticipantsForSession(sessionId)
}



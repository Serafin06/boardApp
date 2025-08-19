package com.example.apprafal.data


import android.util.Log
import android.widget.Toast


class GameSessionRepo(
    private val sessionDao: GameSessionDao,
    private val participantDao: GameSessionParticipantDao
) {
    suspend fun createSessionWithParticipants(
        date: Long,
        selectedPlayers: List<Player>
    ): String {
                // 1. Utwórz obiekt sesji z unikalnym ID
        val session = GameSession(date = date)
        Log.d("SESSION_REPO", "📝 Utworzono sesję z ID: ${session.id}")

        // 2. Przekształć graczy na uczestników sesji
        val participants = selectedPlayers.mapIndexed { index, player ->
            Log.d("SESSION_REPO", "👤 Dodaję gracza: ${player.name} (canChoose: ${player.canChooseGame}, originalPos: ${player.queuePosition})")

            GameSessionParticipant(
                sessionId = session.id,
                playerId = player.id,
                isPresent = true,
                canPickInSession = player.canChooseGame,
                queuePosition = player.queuePosition ?: (index + 1),

            )
        }

        Log.d("SESSION_REPO", "📋 Utworzono ${participants.size} uczestników:")
        participants.forEach { p ->
            Log.d("SESSION_REPO", "  - PlayerID: ${p.playerId}, canPick: ${p.canPickInSession}, pos: ${p.queuePosition}")
        }

        // 3. Zapisz sesję i uczestników w jednej transakcji
        sessionDao.createSessionWithParticipants(session, participants)
        Log.d("SESSION_REPO", "✅ Sesja i uczestnicy zapisani pomyślnie!")

        return session.id
    }


    suspend fun getAllSessions(): List<GameSession> {
        Log.d("SESSION_REPO", "📋 Pobieranie wszystkich sesji...")
        return sessionDao.getAllSessions()
    }

    suspend fun getSessionById(sessionId: String): GameSession? {
        Log.d("SESSION_REPO", "🔍 Szukam sesji: $sessionId")
        return sessionDao.getSessionById(sessionId)
    }


    suspend fun getLatestSession(): GameSession? {
        Log.d("SESSION_REPO", "🔍 Pobieranie najnowszej sesji...")
        val session = sessionDao.getLatestSession()
        if (session != null) {
            Log.d("SESSION_REPO", "✅ Znaleziono najnowszą sesję: ${session.id}")
        } else {
            Log.d("SESSION_REPO", "❌ Brak sesji w bazie danych")
        }
        return session
    }

    suspend fun updateCurrentPicker(sessionId: String, playerId: Int?) {
        Log.d("SESSION_REPO", "🎯 Ustawianie aktualnego pickera: $playerId w sesji $sessionId")
        sessionDao.updateCurrentPicker(sessionId, playerId)
    }

    suspend fun getActiveQueue(sessionId: String): List<GameSessionParticipant> {

        // Pobierz wszystkich uczestników sesji
        val allParticipants = participantDao.getParticipantsForSession(sessionId)

        // Filtruj tylko tych którzy mogą wybierać
        val activeParticipants = allParticipants.filter { it.canPickInSession }

        // Posortuj po pozycji w kolejce (najniższa waga pierwsza)
        val sortedQueue = activeParticipants.sortedBy { it.queuePosition }


        sortedQueue.forEach { p ->
            Log.d("SESSION_REPO", "  - PlayerID: ${p.playerId}, pozycja: ${p.queuePosition}")
        }

        return sortedQueue
    }


    suspend fun getFirstAvailablePicker(sessionId: String): GameSessionParticipant? {
        Log.d("SESSION_REPO", "🎯 Szukam pierwszego dostępnego gracza do wybierania...")

        val activeQueue = getActiveQueue(sessionId)

        val availablePicker = activeQueue.firstOrNull()

        return availablePicker
    }


    suspend fun skipParticipant(sessionId: String, playerId: Int) {
        Log.d("SESSION_REPO", "⏭️ Pomijanie gracza $playerId...")

       // Znajdź następnego gracza
        val nextPicker = getFirstAvailablePicker(sessionId)
        updateCurrentPicker(sessionId, nextPicker?.playerId)

        if (nextPicker != null) {
            Log.d("SESSION_REPO", "✅ Następny picker po pominięciu: ${nextPicker.playerId}")
        }
    }
    suspend fun getParticipantsWithNames(sessionId: String): List<ParticipantWithName> {
        Log.d("SESSION_REPO", "📋 Pobieranie uczestników z nazwami...")
        val participants = participantDao.getParticipantsWithNames(sessionId)
        Log.d("SESSION_REPO", "✅ Pobrano ${participants.size} uczestników z nazwami")
        return participants
    }

    suspend fun getAllParticipants(sessionId: String): List<GameSessionParticipant> {
        return participantDao.getAllParticipants(sessionId)
    }

    suspend fun getLast5Sessions(): List<GameSession> {
        return sessionDao.getLast5Sessions()
    }


    suspend fun updateGameName(sessionId: String, gameName: String) {
        sessionDao.updateGameName(sessionId, gameName)
    }

    suspend fun deleteByID(sessionId: String) {
        sessionDao.deleteSessionById(sessionId)
    }
}

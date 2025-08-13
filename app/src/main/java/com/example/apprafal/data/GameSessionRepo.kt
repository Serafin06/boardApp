package com.example.apprafal.data


import android.util.Log


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
                sessionId = session.id,                    // Powiąż z sesją
                playerId = player.id,                      // ID gracza
                isPresent = true,                          // Domyślnie obecny
                canPickInSession = player.canChooseGame,   // Czy może wybierać - KLUCZOWE!
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
        Log.d("SESSION_REPO", "🎯 Pobieranie aktywnej kolejki dla sesji: $sessionId")

        // Pobierz wszystkich uczestników sesji
        val allParticipants = participantDao.getParticipantsForSession(sessionId)
        Log.d("SESSION_REPO", "📋 Wszyscy uczestnicy: ${allParticipants.size}")

        // Filtruj tylko tych którzy mogą wybierać
        val activeParticipants = allParticipants.filter { it.canPickInSession }
        Log.d("SESSION_REPO", "🎯 Aktywni uczestnicy: ${activeParticipants.size}")

        // Posortuj po pozycji w kolejce (najniższa waga pierwsza)
        val sortedQueue = activeParticipants.sortedBy { it.queuePosition }

        Log.d("SESSION_REPO", "✅ Aktywna kolejka (posortowana):")
        sortedQueue.forEach { p ->
            Log.d("SESSION_REPO", "  - PlayerID: ${p.playerId}, pozycja: ${p.queuePosition}")
        }

        return sortedQueue
    }


    suspend fun getFirstAvailablePicker(sessionId: String): GameSessionParticipant? {
        Log.d("SESSION_REPO", "🎯 Szukam pierwszego dostępnego gracza do wybierania...")

        // Pobierz aktywną kolejkę
        val activeQueue = getActiveQueue(sessionId)

        // Znajdź pierwszego gracza który:
        // 1. Może wybierać (canPickInSession = true) - już przefiltrowane w getActiveQueue
        // 2. Nie jest pominięty (isSkipped = false)
        // 3. Ma najniższą wagę (queuePosition)
        val availablePicker = activeQueue
            .filter { !it.isPresent }  // Usuń pominiętych
            .minByOrNull { it.queuePosition }  // Znajdź z najniższą wagą

        if (availablePicker != null) {
            Log.d("SESSION_REPO", "✅ Znaleziono pierwszego dostępnego gracza:")
            Log.d("SESSION_REPO", "  - PlayerID: ${availablePicker.playerId}")
            Log.d("SESSION_REPO", "  - Pozycja w kolejce: ${availablePicker.queuePosition}")
            Log.d("SESSION_REPO", "  - Może wybierać: ${availablePicker.canPickInSession}")
            Log.d("SESSION_REPO", "  - Pominięty: ${availablePicker.isPresent}")
        } else {
            Log.d("SESSION_REPO", "❌ Brak dostępnych graczy do wybierania!")
        }

        return availablePicker
    }


    suspend fun getParticipantsForSession(sessionId: String): List<GameSessionParticipant> {
        Log.d("SESSION_REPO", "📋 Pobieranie wszystkich uczestników sesji: $sessionId")
        return participantDao.getParticipantsForSession(sessionId)
    }


    suspend fun moveParticipantToEndOfQueue(sessionId: String, participant: GameSessionParticipant) {
        Log.d("SESSION_REPO", "🔄 ROZPOCZYNAM przesuwanie gracza ${participant.playerId} na koniec kolejki...")
        Log.d("SESSION_REPO", "📋 Stan PRZED przesunięciem:")
        Log.d("SESSION_REPO", "  - PlayerID: ${participant.playerId}")
        Log.d("SESSION_REPO", "  - Obecna pozycja: ${participant.queuePosition}")
        Log.d("SESSION_REPO", "  - Participant ID: ${participant.id}")

        try {


            participantDao.moveToEndOfQueue(sessionId, participant.id)

            Log.d("SESSION_REPO", "✅ KROK 2 zakończony - gracz przesunięty")

            // 3. Sprawdź stan PO przesunięciu
            Log.d("SESSION_REPO", "🔍 SPRAWDZENIE: Pobieranie stanu kolejki PO przesunięciu...")
            val updatedQueue = getActiveQueue(sessionId)
            Log.d("SESSION_REPO", "📋 Stan kolejki PO przesunięciu:")
            updatedQueue.forEach { p ->
                Log.d("SESSION_REPO", "  - PlayerID: ${p.playerId}, pozycja: ${p.queuePosition}}")
            }

            // 4. Znajdź i ustaw następnego gracza jako aktualnego pickera
            Log.d("SESSION_REPO", "🎯 KROK 3: Szukam następnego pickera...")
            val nextPicker = getFirstAvailablePicker(sessionId)

            if (nextPicker != null) {
                Log.d("SESSION_REPO", "✅ Znaleziono następnego pickera: ${nextPicker.playerId} (pozycja: ${nextPicker.queuePosition})")
                updateCurrentPicker(sessionId, nextPicker.playerId)
                Log.d("SESSION_REPO", "✅ KROK 3 zakończony - następny picker ustawiony")
            } else {
                Log.d("SESSION_REPO", "⚠️ Brak następnego pickera - wszyscy wybrali lub są pomijani")
                updateCurrentPicker(sessionId, null)
            }

            Log.d("SESSION_REPO", "🎉 SUKCES: Przesunięcie gracza zakończone pomyślnie!")

        } catch (e: Exception) {
            Log.e("SESSION_REPO", "❌ BŁĄD podczas przesuwania gracza: ${e.message}", e)
            throw e
        }
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
}
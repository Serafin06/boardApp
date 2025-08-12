package com.example.apprafal.ui

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.apprafal.R
import com.example.apprafal.data.AppDatabase
import com.example.apprafal.data.GameSessionRepo
import com.example.apprafal.data.PlayerRepo
import com.example.apprafal.data.Player
import kotlinx.coroutines.launch

class QueueActivity : AppCompatActivity() {

    private lateinit var sessionViewModel: GameSessionViewModel
    private var sessionId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_queue)

        val recyclerView = findViewById<RecyclerView>(R.id.queueRecyclerView)
        val adapter = GameQueueAdapter()
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Sprawdź czy masz sessionId z intent
        sessionId = intent.getStringExtra("sessionId")
        Log.d("QUEUE_DEBUG", "🎯 QueueActivity started with sessionId: $sessionId")

        if (sessionId != null) {
            // Mamy konkretne sessionId - użyj go
            Log.d("QUEUE_DEBUG", "✅ Używam przekazanego sessionId: $sessionId")
            setupSessionQueue(adapter)
        } else {
            // Brak sessionId - znajdź najnowszą sesję automatycznie
            Log.d("QUEUE_DEBUG", "⚠️ Brak sessionId, szukam najnowszej sesji...")
            findLatestSessionAndSetupQueue(adapter)
        }
    }

    /**
     * Znajduje najnowszą sesję i ustawia kolejkę
     */
    private fun findLatestSessionAndSetupQueue(adapter: GameQueueAdapter) {
        lifecycleScope.launch {
            try {
                Log.d("QUEUE_DEBUG", "🔍 Szukam najnowszej sesji w bazie...")

                val database = AppDatabase.getDatabase(this@QueueActivity)
                val sessionDao = database.gameSessionDao()
                val participantDao = database.gameSessionParticipantDao()
                val sessionRepo = GameSessionRepo(sessionDao, participantDao)

                val latestSession = sessionRepo.getLatestSession()

                if (latestSession != null) {
                    sessionId = latestSession.id
                    Log.d("QUEUE_DEBUG", "✅ Znaleziono najnowszą sesję: $sessionId")

                    runOnUiThread {
                        setupSessionQueue(adapter)
                    }
                } else {
                    Log.e("QUEUE_DEBUG", "❌ Brak sesji w bazie danych!")
                    runOnUiThread {
                        // Możesz pokazać komunikat lub wrócić do głównego ekranu
                        Log.d("QUEUE_DEBUG", "ℹ️ Przekierowuję do MainActivity - brak sesji")
                        finish()
                    }
                }
            } catch (e: Exception) {
                Log.e("QUEUE_DEBUG", "❌ Błąd podczas szukania najnowszej sesji: ${e.message}", e)
                runOnUiThread {
                    finish()
                }
            }
        }
    }

    private fun setupSessionQueue(adapter: GameQueueAdapter) {
        Log.d("QUEUE_DEBUG", "🔧 Inicjalizacja setupSessionQueue...")

        // Inicjalizuj wszystkie potrzebne komponenty
        val database = AppDatabase.getDatabase(this)
        val sessionDao = database.gameSessionDao()
        val participantDao = database.gameSessionParticipantDao()
        val sessionRepo = GameSessionRepo(sessionDao, participantDao)
        val playerRepo = PlayerRepo(database.playerDao())

        val factory = GameSessionViewModelFactory(sessionRepo, playerRepo)
        sessionViewModel = ViewModelProvider(this, factory).get(GameSessionViewModel::class.java)
        Log.d("QUEUE_DEBUG", "✅ ViewModels zainicjalizowane")

        lifecycleScope.launch {
            try {
                Log.d("QUEUE_DEBUG", "🎯 Rozpoczynam pobieranie kolejki dla sesji: $sessionId")

                // KROK 1: Pobierz aktywną kolejkę z bazy danych
                val activeQueue = sessionViewModel.getActiveQueue(sessionId!!)
                Log.d("QUEUE_DEBUG", "📋 Pobrano ${activeQueue.size} uczestników z bazy")

                // KROK 2: FILTRUJ tylko graczy którzy mogą wybierać (aktualna kolejka)
                // To jest kluczowe - pokazujemy tylko tych co są w grze!
                val currentQueue = activeQueue.filter { it.canPickInSession }
                Log.d("QUEUE_DEBUG", "🎯 Po filtrowaniu aktywnych: ${currentQueue.size} graczy")
                Log.d("QUEUE_DEBUG", "📋 Aktywni gracze: ${currentQueue.map { "ID:${it.playerId}, pos:${it.queuePosition}" }}")

                // KROK 3: Mapuj GameSessionParticipant na obiekty Player (dla adaptera)
                val players = currentQueue.map { participant ->
                    // Pobierz pełne dane gracza z tabeli players
                    val player = playerRepo.getById(participant.playerId)
                    Log.d("QUEUE_DEBUG", "👤 Mapowanie: ${player.name} - pozycja: ${participant.queuePosition}")

                    // Utwórz obiekt Player z danymi z sesji
                    Player(
                        id = player.id,
                        name = player.name,
                        canChooseGame = participant.canPickInSession,  // Z sesji, nie z oryginalnego Player
                        queuePosition = participant.queuePosition      // Z sesji - aktualna pozycja
                    )
                }

                // KROK 4: SORTUJ po pozycji w kolejce (ROSNĄCO - najniższa waga = najwyższy priorytet)
                // To zapewnia że gracz z wagą 1 będzie pierwszy, potem 2, 3, itd.
                val sortedPlayers = players.sortedBy { it.queuePosition }
                Log.d("QUEUE_DEBUG", "✅ Kolejka posortowana (od najniższej wagi):")
                sortedPlayers.forEachIndexed { index, player ->
                    Log.d("QUEUE_DEBUG", "  ${index + 1}. ${player.name} (waga: ${player.queuePosition})")
                }

                // KROK 5: Przekaż dane do adaptera
                adapter.submitList(sortedPlayers)
                Log.d("QUEUE_DEBUG", "✅ Dane przekazane do adaptera RecyclerView")

            } catch (e: Exception) {
                Log.e("QUEUE_DEBUG", "❌ BŁĄD podczas ładowania kolejki: ${e.message}", e)
                // Możesz dodać Toast z błędem tutaj
            }
        }
    }
}
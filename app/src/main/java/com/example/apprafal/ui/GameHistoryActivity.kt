package com.example.apprafal.ui

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.apprafal.R
import com.example.apprafal.data.AppDatabase
import com.example.apprafal.data.GamePickRepo
import com.example.apprafal.data.GameSessionRepo
import com.example.apprafal.data.PlayerRepo
import kotlinx.coroutines.launch

class GameHistoryActivity : AppCompatActivity() {

    private lateinit var adapter: GamePickListAdapter
    private lateinit var sessionViewModel: GameSessionViewModel
    private var sessionId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_history)

        sessionId = intent.getStringExtra("sessionId")
        Log.d("HISTORY_DEBUG", "🏁 GameHistoryActivity started with sessionId: $sessionId")

        if (sessionId != null) {
            Log.d("HISTORY_DEBUG", "✅ Używam przekazanego sessionId: $sessionId")
            setupHistoryView()
        } else {
            Log.d("HISTORY_DEBUG", "⚠️ Brak sessionId, szukam najnowszej sesji...")
            findLatestSession()
        }
    }

    private fun findLatestSession() {
        lifecycleScope.launch {
            try {
                val database = AppDatabase.getDatabase(this@GameHistoryActivity)
                val sessionDao = database.gameSessionDao()
                val participantDao = database.gameSessionParticipantDao()
                val sessionRepo = GameSessionRepo(sessionDao, participantDao)

                val latestSession = sessionRepo.getLatestSession()

                if (latestSession != null) {
                    sessionId = latestSession.id
                    Log.d("HISTORY_DEBUG", "✅ Znaleziono najnowszą sesję: $sessionId")
                    runOnUiThread {
                        setupHistoryView()
                    }
                } else {
                    Log.d("HISTORY_DEBUG", "❌ Brak sesji w bazie!")
                    runOnUiThread {
                        showNoGamesState()
                    }
                }
            } catch (e: Exception) {
                Log.e("HISTORY_DEBUG", "❌ Błąd podczas szukania sesji: ${e.message}", e)
                runOnUiThread {
                    Toast.makeText(this@GameHistoryActivity, "Błąd: ${e.message}", Toast.LENGTH_SHORT).show()
                    showNoGamesState()
                }
            }
        }
    }

    private fun setupHistoryView() {
        if (sessionId == null) {
            showNoGamesState()
            return
        }

        // Inicjalizacja widoków
        val recyclerView = findViewById<RecyclerView>(R.id.historyRecyclerView)
        val undoButton = findViewById<Button>(R.id.undoLastPickButton)
        val emptyView = findViewById<TextView>(R.id.emptyHistoryText)

        adapter = GamePickListAdapter()
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Inicjalizacja repozytoriów i ViewModel
        val database = AppDatabase.getDatabase(applicationContext)
        val sessionDao = database.gameSessionDao()
        val participantDao = database.gameSessionParticipantDao()
        val sessionRepo = GameSessionRepo(sessionDao, participantDao)
        val pickDao = database.gamePickDao()
        val pickRepo = GamePickRepo(pickDao)
        val playerDao = database.playerDao()
        val playerRepo = PlayerRepo(playerDao)

        val factory = GameSessionViewModelFactory(sessionRepo, playerRepo, pickRepo)
        sessionViewModel = ViewModelProvider(this, factory).get(GameSessionViewModel::class.java)

        Log.d("HISTORY_DEBUG", "🔍 Ładowanie danych dla sesji: $sessionId")

        // Obserwacja wyborów gier - OSTATNIE 5 GIER
        pickRepo.getPicksForSession(sessionId!!).observe(this) { allPicks ->
            Log.d("HISTORY_DEBUG", "📋 Otrzymano ${allPicks.size} wyborów gier")

            if (allPicks.isEmpty()) {
                // BRAK GIER - pokaż komunikat
                showNoGamesState()
                return@observe
            }

            lifecycleScope.launch {
                try {
                    // Weź ostatnie 5 wyborów
                    val last5Picks = allPicks.take(5)

                    Log.d("HISTORY_DEBUG", "🎯 Pokazuję ostatnie ${last5Picks.size} wyborów")

                    val picksWithNames = last5Picks.map { pick ->
                        val player = playerRepo.getById(pick.playerId)
                        GamePickWithPlayerName(
                            playerName = player.name,
                            gameName = pick.gameName,
                            timestamp = pick.timestamp,
                            originalPick = pick
                        )
                    }

                    runOnUiThread {
                        // POKAŻ LISTĘ GIER
                        recyclerView.visibility = View.VISIBLE
                        emptyView.visibility = View.GONE

                        adapter.submitList(picksWithNames)
                        undoButton.isEnabled = picksWithNames.isNotEmpty()
                    }
                } catch (e: Exception) {
                    Log.e("HISTORY_DEBUG", "❌ Błąd podczas mapowania: ${e.message}", e)
                    runOnUiThread {
                        showNoGamesState()
                    }
                }
            }
        }

        // Obsługa przycisku cofania
        undoButton.setOnClickListener {
            showUndoConfirmationDialog()
        }
    }

    // Obsługa braku gier
    private fun showNoGamesState() {
        val recyclerView = findViewById<RecyclerView>(R.id.historyRecyclerView)
        val undoButton = findViewById<Button>(R.id.undoLastPickButton)
        val emptyView = findViewById<TextView>(R.id.emptyHistoryText)

        recyclerView.visibility = View.GONE
        emptyView.visibility = View.VISIBLE
        emptyView.text = "Brak gier w historii"
        undoButton.isEnabled = false
    }

    private fun showUndoConfirmationDialog() {
        lifecycleScope.launch {
            try {
                val sessionDetail = sessionViewModel.getSessionWithDetails(sessionId!!)
                val lastPick = sessionDetail?.picks?.maxByOrNull { it.pickOrder }

                if (lastPick == null) {
                    runOnUiThread {
                        Toast.makeText(this@GameHistoryActivity, "Brak wyborów do cofnięcia", Toast.LENGTH_SHORT).show()
                    }
                    return@launch
                }

                val database = AppDatabase.getDatabase(applicationContext)
                val player = database.playerDao().getById(lastPick.playerId)

                runOnUiThread {
                    AlertDialog.Builder(this@GameHistoryActivity)
                        .setTitle("Cofnij ostatni wybór")
                        .setMessage("Czy na pewno chcesz cofnąć wybór gracza ${player.name}: ${lastPick.gameName}?\n\nSpowoduje to przywrócenie kolejki do stanu z początku sesji.")
                        .setPositiveButton("Cofnij") { _, _ ->
                            performUndo()
                        }
                        .setNegativeButton("Anuluj", null)
                        .show()
                }
            } catch (e: Exception) {
                Log.e("HISTORY_DEBUG", "❌ Błąd podczas pobierania ostatniego wyboru: ${e.message}", e)
                runOnUiThread {
                    Toast.makeText(this@GameHistoryActivity, "Błąd: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun performUndo() {
        lifecycleScope.launch {
            try {
                Log.d("HISTORY_DEBUG", "🔄 Rozpoczęcie cofania wyboru...")

                val success = sessionViewModel.undoLastPick(sessionId!!)

                runOnUiThread {
                    if (success) {
                        Log.d("HISTORY_DEBUG", "✅ Pomyślnie cofnięto wybór i przywrócono kolejkę")
                        Toast.makeText(this@GameHistoryActivity, "Cofnięto ostatni wybór i przywrócono kolejkę", Toast.LENGTH_SHORT).show()
                    } else {
                        Log.e("HISTORY_DEBUG", "❌ Nie udało się cofnąć wyboru")
                        Toast.makeText(this@GameHistoryActivity, "Nie udało się cofnąć wyboru", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Log.e("HISTORY_DEBUG", "❌ Błąd podczas cofania: ${e.message}", e)
                runOnUiThread {
                    Toast.makeText(this@GameHistoryActivity, "Błąd: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}
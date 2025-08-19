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

    private lateinit var sessionViewModel: GameSessionViewModel
    private val playerRepo = PlayerRepo(AppDatabase.getDatabase(this).playerDao())
    private val pickRepo = GamePickRepo(AppDatabase.getDatabase(this).gamePickDao())
    private val database = AppDatabase.getDatabase(this)
    private val sessionDao = database.gameSessionDao()
    private val participantDao = database.gameSessionParticipantDao()
    private val sessionRepo = GameSessionRepo(sessionDao, participantDao)

    private val factory = GameSessionViewModelFactory(sessionRepo, playerRepo, pickRepo)

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
                    Toast.makeText(
                        this@GameHistoryActivity,
                        "Błąd: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    showNoGamesState()
                }
            }
        }
    }

    private fun setupHistoryView() {
        val recyclerView = findViewById<RecyclerView>(R.id.historyRecyclerView)
        val undoButton = findViewById<Button>(R.id.undoLastPickButton)
        val emptyView = findViewById<TextView>(R.id.emptyHistoryText)

        // PROSTY ADAPTER
        val sessionAdapter = GameSessionListAdapter()
        recyclerView.adapter = sessionAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        Log.d("HISTORY_DEBUG", "🔍 Ładowanie 5 ostatnich sesji...")

        // UŻYCIE TWOJEJ METODY getLast5Sessions()
        lifecycleScope.launch {
            try {
                val sessions = sessionRepo.getLast5Sessions()
                Log.d("HISTORY_DEBUG", "📋 Otrzymano ${sessions.size} sesji")

                runOnUiThread {
                    if (sessions.isEmpty()) {
                        showNoGamesState()
                    } else {
                        recyclerView.visibility = View.VISIBLE
                        emptyView.visibility = View.GONE

                        // Po prostu przekaż List<GameSession> do adaptera
                        sessionAdapter.submitList(sessions)
                        undoButton.isEnabled = sessions.isNotEmpty()

                        // Ustaw najnowszą sesję dla undo
                        sessionId = sessions.firstOrNull()?.id

                        Log.d("HISTORY_DEBUG", "✅ Wyświetlono ${sessions.size} sesji")
                    }
                }
            } catch (e: Exception) {
                Log.e("HISTORY_DEBUG", "❌ Błąd podczas ładowania sesji: ${e.message}", e)
                runOnUiThread {
                    showNoGamesState()
                }
            }
        }

        // Obsługa przycisku cofania (dla najnowszej sesji)
        undoButton.setOnClickListener {
            showUndoConfirmationDialog()
        }
    }


    // Obsługa braku gier
    private fun showNoGamesState() {
        val recyclerView = findViewById<RecyclerView>(R.id.historyRecyclerView)
        val emptyView = findViewById<TextView>(R.id.emptyHistoryText)

        recyclerView.visibility = View.GONE
        emptyView.visibility = View.VISIBLE
        emptyView.text = "Brak gier w historii"
    }

    private fun showUndoConfirmationDialog() {
        Log.d("HISTORY_DEBUG", "showUndoConfirmationDialog wywołane!")

        sessionViewModel = ViewModelProvider(this, factory)[GameSessionViewModel::class.java]

        if (sessionId == null) {
            Toast.makeText(this, "Brak aktywnej sesji", Toast.LENGTH_SHORT).show()
            return
        }


        lifecycleScope.launch {
            try {
                // Sprawdź czy w tej sesji są jakieś wybory do cofnięcia
                val sessionDetail = sessionViewModel.getSessionWithDetails(sessionId!!)
                val lastPick = sessionDetail?.picks?.maxByOrNull { it.pickOrder }

                if (lastPick == null) {
                    runOnUiThread {
                        Toast.makeText(
                            this@GameHistoryActivity,
                            "Brak wyborów do cofnięcia w tej sesji",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    return@launch
                }

                // Jeśli jest co cofać, pokaż dialog potwierdzenia
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
                Log.e("HISTORY_DEBUG", "Błąd podczas sprawdzania wyborów: ${e.message}", e)
                runOnUiThread {
                    Toast.makeText(
                        this@GameHistoryActivity,
                        "Błąd: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
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
                        Toast.makeText(
                            this@GameHistoryActivity,
                            "Cofnięto ostatni wybór i przywrócono kolejkę",
                            Toast.LENGTH_SHORT
                        ).show()

                        findLatestSession()


                    } else {
                        Log.e("HISTORY_DEBUG", "❌ Nie udało się cofnąć wyboru")
                        Toast.makeText(
                            this@GameHistoryActivity,
                            "Nie udało się cofnąć wyboru",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                Log.e("HISTORY_DEBUG", "❌ Błąd podczas cofania: ${e.message}", e)
                runOnUiThread {
                    Toast.makeText(
                        this@GameHistoryActivity,
                        "Błąd: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }
}
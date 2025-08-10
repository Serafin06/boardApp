package com.example.apprafal.ui

import android.os.Bundle
import android.util.Log
import android.widget.Button
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
    private lateinit var sessionDetailViewModel: SessionDetailViewModel
    private var sessionId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_history)

        sessionId = intent.getStringExtra("sessionId")
        Log.d("HISTORY_DEBUG", "🏁 GameHistoryActivity started with sessionId: $sessionId")

        if (sessionId == null) {
            Log.e("HISTORY_DEBUG", "❌ Brak sessionId!")
            finish()
            return
        }

        // Inicjalizacja widoków
        val recyclerView = findViewById<RecyclerView>(R.id.historyRecyclerView)
        val undoButton = findViewById<Button>(R.id.undoLastPickButton)

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

        val factory = SessionDetailViewModelFactory(sessionRepo, pickRepo)
        sessionDetailViewModel = ViewModelProvider(this, factory).get(SessionDetailViewModel::class.java)

        Log.d("HISTORY_DEBUG", "🔍 Ładowanie danych dla sesji: $sessionId")

        // Obserwacja wyborów gier z automatycznym mapowaniem na imiona
        pickRepo.getPicksForSession(sessionId!!).observe(this) { picks ->
            Log.d("HISTORY_DEBUG", "📋 Otrzymano ${picks.size} wyborów gier")

            lifecycleScope.launch {
                val picksWithNames = picks.map { pick ->
                    Log.d("HISTORY_DEBUG", "🔍 Mapowanie playerId ${pick.playerId}")

                    val player = playerRepo.getById(pick.playerId) // już Int!
                    Log.d("HISTORY_DEBUG", "👤 Znaleziony gracz: ${player.name}")

                    GamePickWithPlayerName(
                        playerName = player.name,
                        gameName = pick.gameName,
                        timestamp = pick.timestamp,
                        originalPick = pick
                    )
                }

                Log.d("HISTORY_DEBUG", "✅ Zmapowane ${picksWithNames.size} wyborów:")
                picksWithNames.forEach { display ->
                    Log.d("HISTORY_DEBUG", "  - ${display.playerName}: ${display.gameName} (order: ${display.originalPick.pickOrder})")
                }

                runOnUiThread {
                    adapter.submitList(picksWithNames)
                    undoButton.isEnabled = picksWithNames.isNotEmpty()
                }
            }
        }

        // Obsługa przycisku cofania
        undoButton.setOnClickListener {
            showUndoConfirmationDialog()
        }
    }

    private fun showUndoConfirmationDialog() {
        lifecycleScope.launch {
            try {
                val sessionDetail = sessionDetailViewModel.getSessionWithDetails(sessionId!!)
                val lastPick = sessionDetail?.picks?.maxByOrNull { it.pickOrder }

                if (lastPick == null) {
                    runOnUiThread {
                        Toast.makeText(this@GameHistoryActivity, "Brak wyborów do cofnięcia", Toast.LENGTH_SHORT).show()
                    }
                    return@launch
                }

                // Pobierz nazwę gracza
                val database = AppDatabase.getDatabase(applicationContext)
                val player = database.playerDao().getById(lastPick.playerId)

                runOnUiThread {
                    AlertDialog.Builder(this@GameHistoryActivity)
                        .setTitle("Cofnij ostatni wybór")
                        .setMessage("Czy na pewno chcesz cofnąć wybór gracza ${player.name}: ${lastPick.gameName}?")
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

                val success = sessionDetailViewModel.undoLastPick(sessionId!!)

                runOnUiThread {
                    if (success) {
                        Log.d("HISTORY_DEBUG", "✅ Pomyślnie cofnięto wybór")
                        Toast.makeText(this@GameHistoryActivity, "Cofnięto ostatni wybór", Toast.LENGTH_SHORT).show()
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
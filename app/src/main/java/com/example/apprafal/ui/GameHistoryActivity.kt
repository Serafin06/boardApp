package com.example.apprafal.ui

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.apprafal.R
import com.example.apprafal.data.AppDatabase
import com.example.apprafal.data.GamePickRepo
import com.example.apprafal.data.GameQueueRepo
import com.example.apprafal.data.PlayerRepo
import kotlinx.coroutines.launch

class GameHistoryActivity : AppCompatActivity() {

    private lateinit var adapter: GamePickListAdapter
    private lateinit var pickRepo: GamePickRepo
    private lateinit var queueRepo: GameQueueRepo
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
        val undoButton = findViewById<Button>(R.id.undoLastPickButton) // Dodaj ten przycisk do layoutu

        adapter = GamePickListAdapter()
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Inicjalizacja repozytoriów
        val database = AppDatabase.getDatabase(applicationContext)
        val pickDao = database.gamePickDao()
        pickRepo = GamePickRepo(pickDao)

        val queueDao = database.gameQueueDao()
        queueRepo = GameQueueRepo(queueDao)

        val playerDao = database.playerDao()
        val playerRepo = PlayerRepo(playerDao)

        Log.d("HISTORY_DEBUG", "🔍 Ładowanie danych dla sesji: $sessionId")

        // Obserwacja danych z mapowaniem na imiona graczy
        pickRepo.getPicksForSession(sessionId!!).observe(this) { picks ->
            Log.d("HISTORY_DEBUG", "📋 Otrzymano ${picks.size} wyborów gier:")

            lifecycleScope.launch {
                val picksWithNames = picks.map { pick ->
                    Log.d("HISTORY_DEBUG", "🔍 Mapowanie playerId ${pick.playerId}")

                    val playerId = pick.playerId.toIntOrNull() ?: 0
                    val player = playerDao.getById(playerId)

                    Log.d("HISTORY_DEBUG", "👤 Znaleziony gracz: ${player?.name ?: "NIEZNANY"}")

                    GamePickWithPlayerName(
                        playerName = player?.name ?: "Gracz #${pick.playerId}",
                        gameName = pick.gameName,
                        timestamp = pick.timestamp,
                        originalPick = pick
                    )
                }

                Log.d("HISTORY_DEBUG", "✅ Zmapowane ${picksWithNames.size} wyborów:")
                picksWithNames.forEach { display ->
                    Log.d("HISTORY_DEBUG", "  - ${display.playerName}: ${display.gameName}")
                }

                runOnUiThread {
                    adapter.submitList(picksWithNames)
                    // Pokaż/ukryj przycisk cofania w zależności od tego czy są wybory
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
            val lastPick = pickRepo.getLastPick(sessionId!!)
            if (lastPick == null) {
                Toast.makeText(this@GameHistoryActivity, "Brak wyborów do cofnięcia", Toast.LENGTH_SHORT).show()
                return@launch
            }

            runOnUiThread {
                AlertDialog.Builder(this@GameHistoryActivity)
                    .setTitle("Cofnij ostatni wybór")
                    .setMessage("Czy na pewno chcesz cofnąć wybór: ${lastPick.gameName}?")
                    .setPositiveButton("Cofnij") { _, _ ->
                        performUndo()
                    }
                    .setNegativeButton("Anuluj", null)
                    .show()
            }
        }
    }

    private fun performUndo() {
        lifecycleScope.launch {
            try {
                val success = pickRepo.undoLastPick(sessionId!!, queueRepo)

                runOnUiThread {
                    if (success) {
                        Toast.makeText(this@GameHistoryActivity, "Cofnięto ostatni wybór", Toast.LENGTH_SHORT).show()
                    } else {
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
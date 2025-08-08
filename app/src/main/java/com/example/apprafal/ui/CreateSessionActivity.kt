package com.example.apprafal.ui

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.DatePicker
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.apprafal.R
import java.util.Calendar
import com.example.apprafal.data.AppDatabase
import com.example.apprafal.data.GameSessionRepo
import com.example.apprafal.data.PlayerRepo
import android.content.Intent
import androidx.lifecycle.lifecycleScope
import com.example.apprafal.data.GamePick
import com.example.apprafal.data.GamePickRepo
import com.example.apprafal.data.GameQueueRepo
import kotlinx.coroutines.launch

class CreateSessionActivity : AppCompatActivity() {

    private lateinit var viewModel: GameSessionViewModel
    private lateinit var playerAdapter: PlayerSelectAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_session)

        val datePicker = findViewById<DatePicker>(R.id.datePicker)
        val recycler = findViewById<RecyclerView>(R.id.playerRecycler)
        val button = findViewById<Button>(R.id.createSessionButton)

        // Inicjalizacja repozytoriów
        val database = AppDatabase.getDatabase(applicationContext)
        val playerDao = database.playerDao()
        val playerRepository = PlayerRepo(playerDao)
        val playerViewModel = PlayerViewModel(playerRepository)

        val sessionDao = database.gameSessionDao()
        val sessionRepo = GameSessionRepo(sessionDao)

        val queueDao = database.gameQueueDao()
        val queueRepo = GameQueueRepo(queueDao)

        val factory = GameSessionViewModelFactory(sessionRepo, queueRepo, playerRepository) // Dodaj PlayerRepo
        viewModel = ViewModelProvider(this, factory).get(GameSessionViewModel::class.java)

        // USUŃ DUPLIKACJĘ - inicjalizuj tylko raz!
        playerAdapter = PlayerSelectAdapter()
        recycler.adapter = playerAdapter
        recycler.layoutManager = LinearLayoutManager(this)

        // Obserwacja graczy
        playerViewModel.allPlayers.observe(this) { players ->
            playerAdapter.submitList(players)
        }

        button.setOnClickListener {
            val calendar = Calendar.getInstance().apply {
                set(datePicker.year, datePicker.month, datePicker.dayOfMonth)
            }
            val timestamp = calendar.timeInMillis
            val selectedPlayers = playerAdapter.getSelectedPlayers()

            if (selectedPlayers.isEmpty()) {
                Toast.makeText(this, "Wybierz graczy.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            Log.d("DEBUG_SESSION", "=== ROZPOCZĘCIE TWORZENIA SESJI ===")
            Log.d("DEBUG_SESSION", "Wybrani gracze: ${selectedPlayers.map { it.name }}")

            lifecycleScope.launch {
                try {
                    // 1. Tworzenie sesji
                    val sessionId = viewModel.createSessionAndReturnId(timestamp, selectedPlayers)
                    Log.d("DEBUG_SESSION", "✅ Utworzono sesję: $sessionId")

                    // 2. Sprawdzenie kolejki
                    val allQueue = queueRepo.getQueue(sessionId)
                    Log.d("DEBUG_SESSION", "📋 Cała kolejka: ${allQueue.map { "ID:${it.playerId}, pos:${it.position}, skip:${it.isSkipped}" }}")

                    val activeQueue = queueRepo.getActiveQueue(sessionId)
                    Log.d("DEBUG_SESSION", "🟢 Aktywna kolejka (tylko obecni): ${activeQueue.map { "ID:${it.playerId}, pos:${it.position}" }}")

                    // 3. Pobranie pierwszego DOSTĘPNEGO gracza
                    val entry = queueRepo.getFirstInQueue(sessionId)
                    Log.d("DEBUG_SESSION", "👤 Pierwszy DOSTĘPNY w kolejce: $entry")

                    if (entry != null) {
                        val playerDao = database.playerDao()
                        val player = playerDao.getById(entry.playerId)
                        Log.d("DEBUG_SESSION", "🎯 Gracz który wybiera: ${player.name}")

                        runOnUiThread {
                            val dialog = AlertDialog.Builder(this@CreateSessionActivity)
                                .setTitle("${player.name} wybiera grę")
                                .setMessage("Ciekawe co wybierze")
                                .setPositiveButton("OK") { _, _ ->
                                    Log.d("DEBUG_SESSION", "=== UŻYTKOWNIK NACISNĄŁ OK ===")

                                    lifecycleScope.launch {
                                        try {
                                            // KROK 1: Zapisz wybór gry
                                            Log.d("DEBUG_SESSION", "📝 Zapisywanie wyboru gry...")

                                            val pickDao = database.gamePickDao()
                                            val pickRepo = GamePickRepo(pickDao)

                                            val gamePick = GamePick(
                                                sessionId = sessionId,
                                                playerId = entry.playerId.toString(),
                                                gameName = "Kotlin",
                                                timestamp = System.currentTimeMillis()
                                            )

                                            pickRepo.insert(gamePick)
                                            Log.d("DEBUG_SESSION", "✅ Zapisano wybór: $gamePick")

                                            // KROK 2: Sprawdź czy wybór się zapisał
                                            val allPicks = pickDao.getPicksForSession(sessionId)
                                            // Nie możemy użyć observe tutaj, więc sprawdzimy inaczej
                                            Log.d("DEBUG_SESSION", "🔍 Sprawdzanie zapisanych wyborów...")

                                            // KROK 3: Przesuń gracza w kolejce
                                            Log.d("DEBUG_SESSION", "🔄 Przesuwanie gracza w kolejce...")
                                            Log.d("DEBUG_SESSION", "📍 Gracz przed przesunięciem: pos=${entry.position}")

                                            val maxPos = queueDao.getMaxPosition(sessionId) ?: 0
                                            Log.d("DEBUG_SESSION", "📊 Max pozycja przed: $maxPos")

                                            queueRepo.moveToEnd(sessionId, entry)

                                            // SPRAWDŹ REZULTAT
                                            val queueAfter = queueRepo.getQueue(sessionId)
                                            Log.d("DEBUG_SESSION", "📋 Kolejka po przesunięciu: ${queueAfter.map { "ID:${it.playerId}, pos:${it.position}" }}")

                                            val newMaxPos = queueDao.getMaxPosition(sessionId) ?: 0
                                            Log.d("DEBUG_SESSION", "📊 Max pozycja po: $newMaxPos")

                                            Log.d("DEBUG_SESSION", "✅ Operacje zakończone, przechodzę do historii...")

                                            // KROK 4: Przejście do historii
                                            runOnUiThread {
                                                val intent = Intent(
                                                    this@CreateSessionActivity,
                                                    GameHistoryActivity::class.java
                                                )
                                                intent.putExtra("sessionId", sessionId)
                                                startActivity(intent)
                                                finish()
                                            }

                                        } catch (e: Exception) {
                                            Log.e("DEBUG_SESSION", "❌ Błąd w operacjach: ${e.message}", e)
                                            runOnUiThread {
                                                Toast.makeText(
                                                    this@CreateSessionActivity,
                                                    "Błąd: ${e.message}",
                                                    Toast.LENGTH_LONG
                                                ).show()
                                            }
                                        }
                                    }
                                }
                                .setCancelable(false)
                                .create()

                            dialog.show()
                        }
                    } else {
                        Log.e("DEBUG_SESSION", "❌ Kolejka jest pusta!")
                        runOnUiThread {
                            Toast.makeText(
                                this@CreateSessionActivity,
                                "Błąd: pusta kolejka!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                } catch (e: Exception) {
                    Log.e("DEBUG_SESSION", "❌ Błąd tworzenia sesji: ${e.message}", e)
                    runOnUiThread {
                        Toast.makeText(
                            this@CreateSessionActivity,
                            "Błąd tworzenia sesji: ${e.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }
    }
}
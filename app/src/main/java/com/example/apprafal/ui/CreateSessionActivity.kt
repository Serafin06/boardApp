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
import com.example.apprafal.data.GamePickRepo
import android.content.Intent
import android.view.View
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import kotlin.collections.map


class CreateSessionActivity : AppCompatActivity() {

    private lateinit var sessionViewModel: GameSessionViewModel
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
        val participantDao = database.gameSessionParticipantDao() // NOWE
        val sessionRepo = GameSessionRepo(sessionDao, participantDao) // ZMIENIONE

        val pickDao = database.gamePickDao()
        val pickRepo = GamePickRepo(pickDao)

        // ViewModels
        val sessionFactory = GameSessionViewModelFactory(sessionRepo, playerRepository, pickRepo)
        sessionViewModel =
            ViewModelProvider(this, sessionFactory).get(GameSessionViewModel::class.java)


        // RecyclerView setup
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

            lifecycleScope.launch {
                try {
                    // 1. Tworzenie sesji z uczestnikami
                    val sessionId =
                        sessionViewModel.createSessionAndReturnId(timestamp, selectedPlayers)

                    // 3. Pobranie pierwszego dostępnego gracza
                    val picker = sessionViewModel.getFirstAvailablePicker(sessionId)
                    if (picker == null) {
                        Toast.makeText(
                            this@CreateSessionActivity,
                            "Brak graczy do wybierania",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@launch
                    }

                    val player = playerRepository.getById(picker.playerId)

                    runOnUiThread {
                        val dialog = AlertDialog.Builder(this@CreateSessionActivity)
                            .setTitle("${player.name} wybiera grę")
                            .setMessage("Ciekawe co wybierze")
                            .setPositiveButton("OK") { _, _ ->

                                lifecycleScope.launch {
                                    try {
                                        // KROK 1: Zapisz wybór gry i automatycznie przesuń gracza

                                        val success = sessionViewModel.makeGamePick(
                                            sessionId = sessionId,
                                            playerId = picker.playerId,
                                            gameName = "Kotlin"
                                        )

                                        sessionViewModel.changeQueue(picker.playerId)


                                        if (success) {
                                            runOnUiThread {
                                                val intent = Intent(
                                                    this@CreateSessionActivity,
                                                    GameHistoryActivity::class.java
                                                )
                                                intent.putExtra("sessionId", sessionId)
                                                startActivity(intent)
                                                finish()
                                            }
                                        } else {
                                            runOnUiThread {
                                                Toast.makeText(
                                                    this@CreateSessionActivity,
                                                    "Błąd podczas zapisywania wyboru",
                                                    Toast.LENGTH_LONG
                                                ).show()
                                            }
                                        }

                                    } catch (e: Exception) {
                                        Log.e(
                                            "DEBUG_SESSION",
                                            "❌ Błąd w operacjach: ${e.message}",
                                            e
                                        )
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
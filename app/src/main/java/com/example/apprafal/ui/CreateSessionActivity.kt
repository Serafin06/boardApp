package com.example.apprafal.ui

import android.content.Context
import android.os.Bundle
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

        val playerDao = AppDatabase.getDatabase(applicationContext).playerDao()
        val playerRepository = PlayerRepo(playerDao)
        val playerViewModel = PlayerViewModel(playerRepository)

        playerAdapter = PlayerSelectAdapter()
        recycler.adapter = playerAdapter
        recycler.layoutManager = LinearLayoutManager(this)

        playerViewModel.allPlayers.observe(this) { players ->
            playerAdapter.submitList(players)
        }

        val sessionDao = AppDatabase.getDatabase(applicationContext).gameSessionDao()
        val sessionRepo = GameSessionRepo(sessionDao)

        val queueDao = AppDatabase.getDatabase(applicationContext).gameQueueDao()
        val queueRepo = GameQueueRepo(queueDao)

        val factory = GameSessionViewModelFactory(sessionRepo, queueRepo)

        // Utwórz ViewModel za pomocą fabryki
        viewModel = ViewModelProvider(this, factory).get(GameSessionViewModel::class.java)

        playerAdapter = PlayerSelectAdapter()
        recycler.adapter = playerAdapter
        recycler.layoutManager = LinearLayoutManager(this)

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
                val sessionId = viewModel.createSessionAndReturnId(timestamp, selectedPlayers)

                val queueDao = AppDatabase.getDatabase(applicationContext).gameQueueDao()
                val queueRepo = GameQueueRepo(queueDao)
                val entry = queueRepo.getFirstInQueue(sessionId)

                if (entry != null) {
                    val playerDao = AppDatabase.getDatabase(applicationContext).playerDao()
                    val player = playerDao.getById(entry.playerId)

                    runOnUiThread {
                        val dialog = AlertDialog.Builder(this@CreateSessionActivity)
                            .setTitle("${player.name} wybiera grę")
                            .setMessage("Kliknij OK, aby kontynuować")
                            .setPositiveButton("OK") { _, _ ->
                                lifecycleScope.launch {
                                    queueRepo.moveToEnd(sessionId, entry)

                                    val intent = Intent(
                                        this@CreateSessionActivity,
                                        GameHistoryActivity::class.java
                                    )
                                    intent.putExtra("sessionId", sessionId)
                                    startActivity(intent)
                                    finish()
                                }
                            }
                            .setCancelable(false)
                            .create()

                        dialog.show()
                    }
                } else {
                    Toast.makeText(
                        this@CreateSessionActivity,
                        "Błąd: pusta kolejka!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

}




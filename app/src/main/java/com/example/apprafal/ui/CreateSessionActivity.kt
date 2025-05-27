package com.example.apprafal.ui

import android.os.Bundle
import android.widget.Button
import android.widget.DatePicker
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.apprafal.R
import java.util.Calendar
import com.example.apprafal.ui.*
import com.example.apprafal.data.AppDatabase
import com.example.apprafal.data.GameSessionRepo


class CreateSessionActivity : AppCompatActivity() {

    private lateinit var viewModel: GameSessionViewModel
    private lateinit var playerAdapter: PlayerSelectAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_session)

        val datePicker = findViewById<DatePicker>(R.id.datePicker)
        val recycler = findViewById<RecyclerView>(R.id.playerRecycler)
        val button = findViewById<Button>(R.id.createSessionButton)


        val dao = AppDatabase.getDatabase(applicationContext).gameSessionDao()
        val repository = GameSessionRepo(dao)

       // Utwórz fabrykę ViewModel
        val factory = GameSessionViewModelFactory(repository)

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

            viewModel.createSession(timestamp, selectedPlayers)

            Toast.makeText(this, "Session created!", Toast.LENGTH_SHORT).show()
            finish() // Zamknij aktywność po utworzeniu sesji
        }
    }
}



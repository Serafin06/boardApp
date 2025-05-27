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


class CreateSessionActivity : AppCompatActivity() {

    private lateinit var viewModel: GameSessionViewModel
    private lateinit var playerAdapter: PlayerSelectAdapter // tworzysz adapter z checkboxami

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_session)

        val datePicker = findViewById<DatePicker>(R.id.datePicker)
        val recycler = findViewById<RecyclerView>(R.id.playerRecycler)
        val button = findViewById<Button>(R.id.createSessionButton)

        viewModel = ViewModelProvider(this)[GameSessionViewModel::class.java]
        playerAdapter = PlayerSelectAdapter()

        recycler.adapter = playerAdapter
        recycler.layoutManager = LinearLayoutManager(this)

        // załaduj graczy z bazy (możesz użyć PlayerViewModel)
        // playerAdapter.submitList(players)

        button.setOnClickListener {
            val calendar = Calendar.getInstance().apply {
                set(datePicker.year, datePicker.month, datePicker.dayOfMonth)
            }
            val timestamp = calendar.timeInMillis

            val selectedPlayers = playerAdapter.getSelectedPlayers()
            viewModel.createSession(timestamp, selectedPlayers)

            Toast.makeText(this, "Session created!", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}


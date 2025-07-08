package com.example.apprafal.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.apprafal.data.*
import com.example.apprafal.R

class GameHistoryActivity : AppCompatActivity() {

    private lateinit var viewModel: GamePickViewModel
    private lateinit var adapter: GamePickListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_history)

        val dao = AppDatabase.getDatabase(applicationContext).gamePickDao()
        val repo = GamePickRepo(dao)
        val factory = GamePickViewModelFactory(repo)
        viewModel = ViewModelProvider(this, factory)[GamePickViewModel::class.java]

        adapter = GamePickListAdapter()

        findViewById<RecyclerView>(R.id.historyRecyclerView).apply {
            layoutManager = LinearLayoutManager(this@GameHistoryActivity)
            this.adapter = adapter
        }

        // dla uproszczenia ładujemy wszystkie picki (możesz potem filtrować po sessionId)
        viewModel.allPicks.observe(this) {
            adapter.submitList(it)
        }
    }
}
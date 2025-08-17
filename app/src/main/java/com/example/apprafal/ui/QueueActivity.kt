package com.example.apprafal.ui

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.apprafal.R
import com.example.apprafal.data.AppDatabase
import com.example.apprafal.data.PlayerRepo

class QueueActivity : AppCompatActivity() {

    private val viewModel: PlayerViewModel by viewModels {
        PlayerViewModelFactory(PlayerRepo(AppDatabase.getDatabase(this).playerDao()))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_queue)

        val recyclerView = findViewById<RecyclerView>(R.id.queueRecyclerView)
        val recyclerView2 = findViewById<RecyclerView>(R.id.queueRecyclerView2)

        // Pierwszy adapter - pozycje względem minPosition
        val adapter = GameQueueAdapter(GameQueueAdapter.DisplayMode.RELATIVE_TO_MIN)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Drugi adapter - pozycje kolejne (1, 2, 3...)
        val adapter2 = GameQueueAdapter(GameQueueAdapter.DisplayMode.SEQUENTIAL)
        recyclerView2.adapter = adapter2
        recyclerView2.layoutManager = LinearLayoutManager(this)

        viewModel.gameQueue.observe(this) { players ->
            adapter.submitList(players)
            adapter2.submitList(players)
        }
    }
}
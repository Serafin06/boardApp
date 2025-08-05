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
        val adapter = GameQueueAdapter()
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        viewModel.gameQueue.observe(this) { players ->
            adapter.submitList(players)
        }
    }
}

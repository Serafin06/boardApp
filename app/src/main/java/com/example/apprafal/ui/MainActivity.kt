package com.example.apprafal.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.apprafal.R
import com.example.apprafal.data.AppDatabase
import com.example.apprafal.data.PlayerRepo
import com.example.apprafal.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private val viewModel: PlayerViewModel by viewModels {
        PlayerViewModelFactory(PlayerRepo(AppDatabase.getDatabase(this).playerDao()))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val adapter = PlayerListAdapter()
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(this)

        viewModel.players.observe(this, Observer {
            adapter.submitList(it)
        })

        binding.buttonAdd.setOnClickListener {
            val name = binding.editTextPlayerName.text.toString()
            if (name.isNotBlank()) {
                viewModel.addPlayer(name)
                binding.editTextPlayerName.text.clear()
            }
        }
        val createSessionButton = findViewById<Button>(R.id.createSessionButton)

        createSessionButton.setOnClickListener {
            val intent = Intent(this, CreateSessionActivity::class.java)
            startActivity(intent)
        }
    }
}
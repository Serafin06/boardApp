package com.example.apprafal.ui

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.widget.Button
import android.widget.EditText
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

        viewModel.allPlayers.observe(this, Observer {
            adapter.submitList(it)
        })
        val checkboxCanChoose = binding.checkboxCanChoose

        binding.buttonAdd.setOnClickListener {
            val name = binding.editTextPlayerName.text.toString()
            val canChoose = binding.checkboxCanChoose.isChecked

            if (name.isNotBlank()) {
                if (canChoose) {
                    // Pokazujemy popup tylko jeśli gracz może wybierać
                    val builder = AlertDialog.Builder(this)
                    builder.setTitle("Podaj miejsce w kolejce")

                    val input = EditText(this)
                    input.inputType = InputType.TYPE_CLASS_NUMBER
                    builder.setView(input)

                    builder.setPositiveButton("OK") { _, _ ->
                        val position = input.text.toString().toIntOrNull()
                        if (position != null) {
                            viewModel.addPlayer(name, position, canChoose)
                            binding.editTextPlayerName.text.clear()
                        }
                    }

                    builder.setNegativeButton("Anuluj") { dialog, _ -> dialog.cancel() }
                    builder.show()
                } else {
                    // Gracz nie może wybierać, więc nie przypisujemy pozycji
                    viewModel.addPlayer(name, queuePosition = -1, canChooseGame = false)
                    binding.editTextPlayerName.text.clear()
                }
            }
        }

        val createSessionButton = findViewById<Button>(R.id.createSessionButton)

        createSessionButton.setOnClickListener {
            val intent = Intent(this, CreateSessionActivity::class.java)
            startActivity(intent)
        }
        val historyButton = findViewById<Button>(R.id.buttonHistory)
        historyButton.setOnClickListener {
            startActivity(Intent(this, GameHistoryActivity::class.java))
        }
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Podaj miejsce w kolejce")

        val input = EditText(this)
        input.inputType = InputType.TYPE_CLASS_NUMBER
        builder.setView(input)

        builder.setPositiveButton("OK") { _, _ ->
            val name = binding.editTextPlayerName.text.toString()
            val position = input.text.toString().toIntOrNull()

            if (name.isNotBlank() && position != null) {
                viewModel.addPlayer(name, position, canChooseGame = true)
                binding.editTextPlayerName.text.clear()
            }
        }

        builder.setNegativeButton("Anuluj") { dialog, _ -> dialog.cancel() }

    }
}
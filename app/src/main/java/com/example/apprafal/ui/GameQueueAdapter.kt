package com.example.apprafal.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.apprafal.data.Player

class GameQueueAdapter(private val displayMode: DisplayMode = DisplayMode.RELATIVE_TO_MIN) : RecyclerView.Adapter<GameQueueAdapter.QueueViewHolder>() {

    enum class DisplayMode {
        RELATIVE_TO_MIN,  // Pozycje względem minPosition (zachowuje odstępy)
        SEQUENTIAL       // Pozycje kolejne 1, 2, 3...
    }

    private var players: List<Player> = emptyList()
    private var minPosition = 0

    fun submitList(list: List<Player>) {
        players = list
        // Oblicz minPosition raz dla całej listy (tylko dla RELATIVE_TO_MIN)
        if (displayMode == DisplayMode.RELATIVE_TO_MIN) {
            minPosition = players.mapNotNull { it.queuePosition }.minOrNull() ?: 0
        }
        notifyDataSetChanged()
    }

    class QueueViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nameText: TextView = view.findViewById(android.R.id.text1)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QueueViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_1, parent, false)
        return QueueViewHolder(view)
    }

    override fun onBindViewHolder(holder: QueueViewHolder, position: Int) {
        val player = players[position]

        val displayPosition = when (displayMode) {
            DisplayMode.RELATIVE_TO_MIN -> {
                // Pozycje względem minPosition (zachowuje odstępy między pozycjami)
                (player.queuePosition ?: 0) - minPosition + 1
            }
            DisplayMode.SEQUENTIAL -> {
                // Pozycje kolejne 1, 2, 3...
                position + 1
            }
        }

        holder.nameText.text = "${displayPosition}. ${player.name}"
    }

    override fun getItemCount(): Int = players.size
}
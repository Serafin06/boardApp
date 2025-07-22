package com.example.apprafal.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.apprafal.data.Player

class GameQueueAdapter : RecyclerView.Adapter<GameQueueAdapter.QueueViewHolder>() {

    private var players: List<Player> = emptyList()

    fun submitList(list: List<Player>) {
        players = list
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
        holder.nameText.text = "${player.queuePosition}. ${player.name}"
    }

    override fun getItemCount(): Int = players.size
}

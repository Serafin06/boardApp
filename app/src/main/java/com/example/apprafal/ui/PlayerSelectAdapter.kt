package com.example.apprafal.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.apprafal.R
import com.example.apprafal.data.Player

class PlayerSelectAdapter : RecyclerView.Adapter<PlayerSelectAdapter.PlayerViewHolder>() {

    private var players: List<Player> = emptyList()
    private val selectedPlayers = mutableSetOf<Player>()

    fun submitList(newList: List<Player>) {
        players = newList
        selectedPlayers.clear()
        notifyDataSetChanged()
    }

    fun getSelectedPlayers(): List<Player> = selectedPlayers.toList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlayerViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.player_select_item, parent, false)
        return PlayerViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlayerViewHolder, position: Int) {
        val player = players[position]
        holder.bind(player)

    }

    override fun getItemCount(): Int = players.size

    inner class PlayerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val checkBox: CheckBox = itemView.findViewById(R.id.playerCheckbox)

        fun bind(player: Player) {
            checkBox.text = player.name
            checkBox.isChecked = selectedPlayers.contains(player)

            if (player.canChooseGame) {
                checkBox.setTextColor(itemView.context.getColor(R.color.authorized_player))
            } else {
                checkBox.setTextColor(itemView.context.getColor(R.color.default_player))
            }

            checkBox.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    selectedPlayers.add(player)
                } else {
                    selectedPlayers.remove(player)
                }
            }
        }
    }
}

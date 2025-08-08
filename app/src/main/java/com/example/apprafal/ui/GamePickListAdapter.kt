package com.example.apprafal.ui

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.apprafal.R
import com.example.apprafal.data.GamePick

// Data class dla wyświetlania z imionami graczy
data class GamePickWithPlayerName(
    val playerName: String,
    val gameName: String,
    val timestamp: Long,
    val originalPick: GamePick
)

class GamePickListAdapter :
    androidx.recyclerview.widget.ListAdapter<GamePickWithPlayerName, GamePickListAdapter.PickViewHolder>(
        DiffCallback()
    ) {

    inner class PickViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(item: GamePickWithPlayerName) {
            Log.d("ADAPTER_DEBUG", "🔗 Displaying: ${item.playerName} -> ${item.gameName}")

            // Używaj istniejącego TextView
            itemView.findViewById<TextView>(R.id.pickText).text =
                "${item.playerName} wybrał grę: ${item.gameName}"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PickViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.pick_item, parent, false)
        return PickViewHolder(view)
    }

    override fun onBindViewHolder(holder: PickViewHolder, position: Int) {
        val item = getItem(position)
        Log.d("ADAPTER_DEBUG", "📱 Binding item $position: ${item.playerName}")
        holder.bind(item)
    }

    class DiffCallback : DiffUtil.ItemCallback<GamePickWithPlayerName>() {
        override fun areItemsTheSame(oldItem: GamePickWithPlayerName, newItem: GamePickWithPlayerName) =
            oldItem.originalPick.id == newItem.originalPick.id

        override fun areContentsTheSame(oldItem: GamePickWithPlayerName, newItem: GamePickWithPlayerName) =
            oldItem == newItem
    }
}
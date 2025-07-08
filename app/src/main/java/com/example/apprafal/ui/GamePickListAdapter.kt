package com.example.apprafal.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.apprafal.R


import com.example.apprafal.data.GamePick

class GamePickListAdapter : androidx.recyclerview.widget.ListAdapter<GamePick, GamePickListAdapter.PickViewHolder>(DiffCallback()){

    inner class PickViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(item: GamePick) {
            itemView.findViewById<TextView>(R.id.pickText).text =
                "Gracz: ${item.playerId} wybrał grę: ${item.gameName}"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PickViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.pick_item, parent, false)
        return PickViewHolder(view)
    }

    override fun onBindViewHolder(holder: PickViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DiffCallback : DiffUtil.ItemCallback<GamePick>() {
        override fun areItemsTheSame(oldItem: GamePick, newItem: GamePick) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: GamePick, newItem: GamePick) = oldItem == newItem
    }
}